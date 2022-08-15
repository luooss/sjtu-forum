package xyz.ls.sjtuforum.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import xyz.ls.sjtuforum.dto.PostDTO;
import xyz.ls.sjtuforum.mapper.PostExtMapper;
import xyz.ls.sjtuforum.mapper.UserMapper;
import xyz.ls.sjtuforum.model.Post;
import xyz.ls.sjtuforum.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PostCache {
    @Autowired
    private PostExtMapper postExtMapper;
    @Autowired
    private UserMapper userMapper;

    private static Cache<String, List<PostDTO>> cacheQuestions = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(entity -> log.info("QUESTIONS_CACHE_REMOVE:{}", entity.getKey()))
            .build();

    public List<PostDTO> getStickies() {
        List<PostDTO> stickies;
        try {
            stickies = cacheQuestions.get("sticky", () -> {
                List<Post> posts = postExtMapper.selectSticky();
                if (posts != null && posts.size() != 0) {
                    List<PostDTO> postDTOS = new ArrayList<>();
                    for (Post post : posts) {
                        User user = userMapper.selectByPrimaryKey(post.getCreator());
                        PostDTO postDTO = new PostDTO();
                        BeanUtils.copyProperties(post, postDTO);
                        postDTO.setUser(user);
                        postDTO.setDescription("");
                        postDTOS.add(postDTO);
                    }
                    return postDTOS;
                } else {
                    return Lists.newArrayList();
                }
            });
        } catch (Exception e) {
            log.error("getStickies error", e);
            return Lists.newArrayList();
        }
        return stickies;
    }
}
