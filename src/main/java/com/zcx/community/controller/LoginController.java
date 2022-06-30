package com.zcx.community.controller;

import com.google.code.kaptcha.Producer;
import com.zcx.community.entity.User;
import com.zcx.community.service.UserService;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.CookieUtil;
import com.zcx.community.util.RedisKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstants {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping(value = "/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/forget")
    public String getForgetPage() {
        return "/site/forget";
    }

    @PostMapping(value = "/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功！我们已经向您的邮箱发送了一份激活邮件，请在1分钟内激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功！您现在就可以使用账号登录了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "请勿重复激活！");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_TIMEOUT) {
            model.addAttribute("msg", "激活失败！验证超时，请重新注册账号！");
            model.addAttribute("target", "/index");
        }
        else {
            model.addAttribute("msg", "激活失败！请提供正确激活码！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
//        session.setAttribute("kaptcha", text);
        String kaptchaOwner = CommunityUtils.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String redisKey = RedisKeyUtils.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException("读取验证码图片失败，服务器发生异常", e);
        }
    }

    @PostMapping(value = "/forget")
    public String forget(String email, String code, String password, HttpSession session, Model model) {
        if (StringUtils.isBlank(password)) {
            model.addAttribute("passwordMsg", "密码不能为空！");
            return "/site/forget";
        }
        String code1 = (String) session.getAttribute("code");
        String email1 = (String) session.getAttribute("email");
        if (StringUtils.isBlank(code1) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(code1)) {
            model.addAttribute("codeMsg", "验证码错误！");
            return "/site/forget";
        }
        if (email1.equals(email)) {
            if (userService.resetPassword(email1, password)) {
                session.removeAttribute("code");
                return "forward:/logout";
            } else {
                model.addAttribute("emailMsg", "邮箱对应账号不存在！");
                return "/site/forget";
            }
        } else {
            model.addAttribute("emailMsg", "邮箱不一致！");
            return "/site/forget";
        }
    }

    @GetMapping("/code")
    public String getCode(String email, HttpSession session, Model model) {
        Map<String, Object> map = userService.getCode(email);
        if (map.containsKey("code")) {
            session.setAttribute("code", map.get("code"));
            session.setAttribute("email", email);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    session.removeAttribute("code");
                }
            });
            thread.start();
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
        }
        return "/site/forget";
    }

    @PostMapping(value = "/login")
    public String login(String username, String password, String code, boolean rememberMe, Model model, HttpSession session, HttpServletResponse response, @CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner) {
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtils.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误！");
            return "/site/login";
        }
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            userService.logout(ticket);
        }
        return "redirect:/index";
    }
}
