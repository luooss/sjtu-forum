package xyz.ls.sjtuforum.listener.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class SubjectRateLimiterEvent extends ApplicationEvent {
    private Long userId;

    public SubjectRateLimiterEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}
