package com.gy.entity;

import com.gy.constants.LikedStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class Like {
   private Integer id;
   private Long blogId; // 被点赞的博客
   private Long userId; // 点赞的用户Id
   private Integer status = LikedStatusEnum.UNLIKE.getCode();; // 0代表该博客没有被点赞，1代表点赞
   private Date createTime;
   private Date updateTime;
   private Integer likeCount;// 点赞数量

   public Like() {}

   public Like(Long blogId, Long userId, Integer status) {
      this.blogId = blogId;
      this.userId = userId;
      this.status = status;
   }

}
