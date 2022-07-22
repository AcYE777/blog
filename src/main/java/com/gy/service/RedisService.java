package com.gy.service;

import com.gy.entity.Like;
import com.gy.entity.vo.LikedCountDTO;

import java.util.List;

public interface RedisService {

    /**
     * 点赞。状态为1
     */void saveLiked2Redis(String userId, String blogId);

    /**
     * 取消点赞。将状态改变为0
     */void unlikeFromRedis(String userId, String blogId);

    /**
     * 从Redis中删除一条点赞数据
     */void deleteLikedFromRedis(String userId, String blogId);

    /**
     * 该博客的点赞数加1
     */void incrementLikedCount(String blogId);

    /**
     * 该博客的点赞数减1
     */void decrementLikedCount(String blogId);

    /**
     * 获取Redis中存储的所有点赞数据
     * @return
     */List<Like> getLikedDataFromRedis();

    /**
     * 获取Redis中存储的所有点赞数量
     * @return
     */List<LikedCountDTO> getLikedCountFromRedis();
}