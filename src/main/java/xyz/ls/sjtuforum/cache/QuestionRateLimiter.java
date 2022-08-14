package xyz.ls.sjtuforum.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import xyz.ls.sjtuforum.dto.QuestionDTO;
import xyz.ls.sjtuforum.listener.event.SubjectRateLimiterEvent;
import xyz.ls.sjtuforum.mapper.SubjectExtMapper;
import xyz.ls.sjtuforum.mapper.UserMapper;
import xyz.ls.sjtuforum.model.Subject;
import xyz.ls.sjtuforum.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QuestionRateLimiter {

    @Autowired
    private ApplicationContext applicationContext;

    private static Cache<Long, Integer> userLimiter = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .removalListener(entity -> log.info("QUESTIONS_RATE_LIMITER_REMOVE:{}", entity.getKey()))
            .build();

    public boolean reachLimit(Long userId) {
        try {
            Integer limit = userLimiter.get(userId, () -> 0);
            userLimiter.put(userId, limit + 1);
            log.info("user : {} post count : {}", userId, limit);
            boolean isReachLimited = limit >= 2;
            if (isReachLimited) {
                applicationContext.publishEvent(new SubjectRateLimiterEvent(this, userId));
            }
            return isReachLimited;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
