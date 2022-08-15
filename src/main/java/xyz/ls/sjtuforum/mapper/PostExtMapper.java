package xyz.ls.sjtuforum.mapper;

import xyz.ls.sjtuforum.dto.PostQueryDTO;
import xyz.ls.sjtuforum.model.Post;

import java.util.List;

public interface PostExtMapper {
    int incView(Post record);

    int incCommentCount(Post record);

    List<Post> selectRelated(Post post);

    Integer countBySearch(PostQueryDTO postQueryDTO);

    List<Post> selectBySearch(PostQueryDTO postQueryDTO);

    List<Post> selectSticky();
}