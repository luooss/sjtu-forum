package xyz.ls.sjtuforum.dto;

import lombok.Data;

@Data
public class PostQueryDTO {
    private String search;
    private String sort;
    private Long time;
    private String tag;
    private Integer page;
    private Integer size;
}
