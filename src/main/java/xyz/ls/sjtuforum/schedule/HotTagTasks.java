package xyz.ls.sjtuforum.schedule;

import xyz.ls.sjtuforum.cache.HotTagCache;
import xyz.ls.sjtuforum.mapper.PostMapper;
import xyz.ls.sjtuforum.model.Post;
import xyz.ls.sjtuforum.model.PostExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class HotTagTasks {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private HotTagCache hotTagCache;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
    public void hotTagSchedule() {
        int offset = 0;
        int limit = 20;
        log.info("hotTagSchedule start {}", new Date());
        List<Post> list = new ArrayList<>();

        Map<String, Integer> priorities = new HashMap<>();
        while (offset == 0 || list.size() == limit) {
            list = postMapper.selectByExampleWithRowbounds(new PostExample(), new RowBounds(offset, limit));
            for (Post post : list) {
                String[] tags = StringUtils.split(post.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = priorities.get(tag);
                    if (priority != null) {
                        priorities.put(tag, priority + 5 + post.getCommentCount());
                    } else {
                        priorities.put(tag, 5 + post.getCommentCount());
                    }
                }
            }
            offset += limit;
        }
        hotTagCache.updateTags(priorities);
        log.info("hotTagSchedule stop {}", new Date());
    }
}
