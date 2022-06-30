package com.zcx.community;

import com.zcx.community.dao.DiscussPostMapper;
import com.zcx.community.dao.LoginTicketMapper;
import com.zcx.community.dao.MessageMapper;
import com.zcx.community.dao.UserMapper;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.LoginTicket;
import com.zcx.community.entity.Message;
import com.zcx.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MapperTests {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectUser() {
        System.out.println(userMapper.selectById(101));
        System.out.println(userMapper.selectByName("liubei"));
        System.out.println(userMapper.selectByEmail("nowcoder101@sina.com"));
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.test/com/101.png");
        user.setCreateTime(new Date());
        user.setActivationCode("fuck");
        user.setType(0);
        user.setStatus(0);
        System.out.println(userMapper.insertUser(user));
    }

    @Test
    public void testUpdateUser() {
        System.out.println(userMapper.updateStatus(155, 1));
        System.out.println(userMapper.updatePassword(155, "666666"));
        System.out.println(userMapper.updateHeader(155, "http://www.test/com/6666.png"));
    }

    @Test
    public void testSelectDiscussPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        System.out.println(discussPostMapper.selectDiscussPostRows(149));
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        LoginTicket loginTicket1 = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket1);
    }

    @Test
    public void testUpdatePreviousLoginTicket() {
        loginTicketMapper.updatePreviousLoginTicket("5bcd23c12bfa4c739b202e3661afdbb9", 180);
    }

    @Test
    public void testInsertDiscussPost() {
        DiscussPost discussPost = new DiscussPost(1, 1, "fuck", "shit", 1, 1, new Date(), 1, 99.5);
        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Test
    public void testSelectDiscussPostById() {
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(180);
        System.out.println(discussPost);
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectConversationCount(111));
        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages1) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(131, "111_131"));
    }
}
