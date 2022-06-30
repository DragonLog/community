package com.zcx.community.dao;

import com.zcx.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    List<Comment> selectCommentsByUserId(@Param("entityType") int entityType, @Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByUserId(@Param("entityType") int entityType, @Param("userId") int userId);

    int insertComment(Comment comment);

    int deleteCommentById(@Param("id") int id);

    Comment selectCommentById(@Param("id") int id);
}
