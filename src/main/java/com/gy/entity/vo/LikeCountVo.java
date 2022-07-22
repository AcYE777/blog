package com.gy.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class LikeCountVo {
    private Integer likeCount;
}
