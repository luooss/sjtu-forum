package xyz.ls.sjtuforum.dto;

import xyz.ls.sjtuforum.model.User;
import lombok.Data;

@Data
public class SubjectDTO {
    private Long id;
    private String title;
    private String description;
    private String tag;
    private Long gmtCreate;
    private Long gmtModified;
    private Long creator;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private User user;
    private Integer sticky;
}
