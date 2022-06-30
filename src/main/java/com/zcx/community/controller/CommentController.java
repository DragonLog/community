package com.zcx.community.controller;

import com.zcx.community.annotation.LoginRequired;
import com.zcx.community.entity.Comment;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.Event;
import com.zcx.community.event.EventProducer;
import com.zcx.community.service.CommentService;
import com.zcx.community.service.DiscussPostService;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstants {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @PostMapping("/remove/{discussPostId}/{commentId}")
    @ResponseBody
    public String removeComment(@PathVariable("commentId") int commentId, @PathVariable("discussPostId") int discussPostId) {
        if (hostHolder.getUser() == null) {
            return CommunityUtils.getJSONString(-1, "未登录", null);
        }
        Comment comment = commentService.findCommentById(commentId);
        if (comment != null && comment.getUserId() != hostHolder.getUser().getId()) {
            return CommunityUtils.getJSONString(-1, "无法删除他人评论", null);
        }
        commentService.removeCommentById(discussPostId, commentId);
        Event event = new Event();
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event.setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }
        return CommunityUtils.getJSONString(0, "删除成功", null);
    }

    @PostMapping("/add/{discussPostId}")
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        if (StringUtils.isBlank(comment.getContent())) {
            return "redirect:/discuss/detail/" + discussPostId;
        }
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        Event event = new Event();
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event.setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }
        event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            if (comment.getUserId() == target.getUserId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            if (comment.getUserId() == comment.getTargetId() || comment.getUserId() == target.getUserId()) {
                return "redirect:/discuss/detail/" + discussPostId;
            }
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        return "redirect:/discuss/detail/" + discussPostId;
    }

}
