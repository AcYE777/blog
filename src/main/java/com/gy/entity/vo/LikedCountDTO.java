package com.gy.entity.vo;

import lombok.Data;

@Data
public class LikedCountDTO {

    private Long blogId; // 博客Id

    private Integer likeCount; // 点赞数

    public LikedCountDTO(Long key, Integer likeCount) {
        this.blogId = key;
        this.likeCount = likeCount;
    }
}
