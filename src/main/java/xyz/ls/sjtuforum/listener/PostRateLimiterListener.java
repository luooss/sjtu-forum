package xyz.ls.sjtuforum.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import xyz.ls.sjtuforum.listener.event.PostRateLimiterEvent;
import xyz.ls.sjtuforum.mapper.PostMapper;
import xyz.ls.sjtuforum.mapper.UserMapper;
import xyz.ls.sjtuforum.model.Post;
import xyz.ls.sjtuforum.model.PostExample;
import xyz.ls.sjtuforum.model.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PostRateLimiterListener implements ApplicationListener<PostRateLimiterEvent> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    private static Cache<Long, Integer> disableUsers = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @SneakyThrows
    @Override
    public void onApplicationEvent(PostRateLimiterEvent event) {
        Integer count = disableUsers.get(event.getUserId(), () -> 0);
        disableUsers.put(event.getUserId(), count + 1);
        log.info("receive rate limit event : {}, count : {}", event.getUserId(), count);
        if (count >= 60) {
            User user = userMapper.selectByPrimaryKey(event.getUserId());
            if (user != null) {
                user.setDisable(1);
                log.info("disable user {}", event.getUserId());
                userMapper.updateByPrimaryKey(user);
            }
            PostExample example = new PostExample();
            example.createCriteria().andCreatorEqualTo(event.getUserId());
            List<Post> posts = postMapper.selectByExample(example);
            if (posts != null && posts.size() != 0) {
                for (Post post : posts) {
                    log.info("disable user {} and delete posts {}", event.getUserId(), post.getId());
                    postMapper.deleteByPrimaryKey(post.getId());
                }
            }
        }
    }
}
