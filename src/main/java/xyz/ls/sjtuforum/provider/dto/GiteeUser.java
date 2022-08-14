package xyz.ls.sjtuforum.provider.dto;

import lombok.Data;

@Data
public class GiteeUser {
    private String name;
    private Long id;
    private String bio;
    private String avatarUrl;
}
