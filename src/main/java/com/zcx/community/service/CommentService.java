package com.zcx.community.service;

import com.zcx.community.dao.CommentMapper;
import com.zcx.community.entity.Comment;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstants {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    public List<Comment> findCommentsByUserId(int entityType, int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUserId(entityType, userId, offset, limit);
    }

    public int findCommentCountByUserId(int entityType, int userId) {
        return commentMapper.selectCountByUserId(entityType, userId);
    }

    //删除评论的逻辑过于复杂：涉及redis和mysql很多字段的增删
    //这里只简单通过id删除评论（把comment的status置为1）并修改帖子的评论数
    public int removeCommentById(int discussPostId, int CommentId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        if (discussPost == null) {
            return 0;
        }
        commentMapper.deleteCommentById(CommentId);
        return discussPostService.updateCommentCount(discussPostId, discussPost.getCommentCount() == 0 ? 0 : discussPost.getCommentCount() - 1);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
