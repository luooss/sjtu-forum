package xyz.ls.sjtuforum.listener.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class PostRateLimiterEvent extends ApplicationEvent {
    private Long userId;

    public PostRateLimiterEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}
