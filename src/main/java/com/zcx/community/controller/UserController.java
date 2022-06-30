package com.zcx.community.controller;

import com.zcx.community.annotation.LoginRequired;
import com.zcx.community.entity.Comment;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.Page;
import com.zcx.community.entity.User;
import com.zcx.community.service.*;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstants {

    @Value(("${community.path.upload}"))
    private String uploadPath;

    @Value(("${community.path.domain}"))
    private String domainPath;

    @Value(("${server.servlet.context-path}"))
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        User user = hostHolder.getUser();
        if (user == null) {
            model.addAttribute("error", "未登录，无法上传");
            return "/site/setting";
        }
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            model.addAttribute("error", "文件名不能为空");
            return "/site/setting";
        }
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            model.addAttribute("error", "文件后缀不能为空");
            return "/site/setting";
        }
        String suffix = filename.substring(index);
        if (StringUtils.isBlank(suffix) || (!".png".equals(suffix) && !".jpg".equals(suffix) && !".jpeg".equals(suffix))) {
            model.addAttribute("error", "文件格式不正确（仅支持.png.jpg.jpeg）");
            return "/site/setting";
        }
        filename = CommunityUtils.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        String headerUrl = domainPath + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        fileName = uploadPath + "/" + fileName;
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return;
        }
        String suffix = fileName.substring(index + 1);
        if (StringUtils.isBlank(suffix) || (!"png".equals(suffix) && !"jpg".equals(suffix) && !"jpeg".equals(suffix))) {
            return;
        }
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream outputStream = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取头像失败，服务器发生异常", e);
        }
    }

    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String confirmPassword, String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword, confirmPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    @GetMapping("/myPosts")
    public String getMyPosts(int userId, Model model, Page page) {
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setPath("/user/myPosts?userId=" + userId);
        User user = userService.findUserById(userId);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post: list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("counts", page.getRows());
        model.addAttribute("user", user);
        return "/site/my-post";
    }

    @GetMapping("/myReplies")
    public String getMyReplies(int userId, Model model, Page page) {
        page.setLimit(5);
        page.setRows(commentService.findCommentCountByUserId(ENTITY_TYPE_POST, userId));
        page.setPath("/user/myReplies?userId=" + userId);
        User user = userService.findUserById(userId);
        List<Comment> list = commentService.findCommentsByUserId(ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                map.put("title", discussPostService.findDiscussPostById(comment.getEntityId()).getTitle());
                comments.add(map);
            }
        }
        model.addAttribute("comments", comments);
        model.addAttribute("counts", page.getRows());
        model.addAttribute("user", user);
        return "/site/my-reply";
    }
}
