package xyz.ls.sjtuforum.mapper;

import xyz.ls.sjtuforum.model.Comment;

public interface CommentExtMapper {
    int incCommentCount(Comment comment);
}