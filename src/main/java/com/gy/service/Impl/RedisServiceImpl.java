package com.gy.service.Impl;

import com.gy.constants.LikedStatusEnum;
import com.gy.entity.Like;
import com.gy.entity.vo.LikedCountDTO;
import com.gy.service.LikedService;
import com.gy.service.RedisService;
import com.gy.util.RedisKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    LikedService likedService;

    @Override
    public void saveLiked2Redis(String userId, String blogId) {
        String key = RedisKeyUtils.getLikedKey(userId, blogId);
        redisTemplate.opsForHash().put(RedisKeyUtils.MAP_KEY_USER_LIKED, key, LikedStatusEnum.LIKE.getCode());
    }

    @Override
    public void unlikeFromRedis(String userId, String blogId) {
        String key = RedisKeyUtils.getLikedKey(userId, blogId);
        redisTemplate.opsForHash().put(RedisKeyUtils.MAP_KEY_USER_LIKED, key, LikedStatusEnum.UNLIKE.getCode());
    }

    @Override
    public void deleteLikedFromRedis(String userId, String blogId) {
        String key = RedisKeyUtils.getLikedKey(userId, blogId);
        redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED, key);
    }

    @Override
    public void incrementLikedCount(String blogId) {
        redisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, blogId, 1);
    }

    @Override
    public void decrementLikedCount(String blogId) {
        redisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, blogId, -1);
    }

    @Override
    public List<Like> getLikedDataFromRedis() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED, ScanOptions.NONE);
        List<Like> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();
            String key = (String) entry.getKey();
            //分离出 userId，blogId
            String[] split = key.split("::");
            String userId = split[0];
            String blogId = split[1];
            Integer status = (Integer) entry.getValue(); // 点赞状态
            //组装成 Like 对象
            Like like = new Like(Long.parseLong(blogId), Long.parseLong(userId), status);
            like.setCreateTime(new Date());
            like.setUpdateTime(new Date());
            list.add(like);
            //存到 list 后从 Redis 中删除
            // redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED, key);
        }
        return list;
    }
    @Override
    public List<LikedCountDTO> getLikedCountFromRedis() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, ScanOptions.NONE);
        List<LikedCountDTO> list = new ArrayList<>();
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            //将点赞数量存储在 LikedCountDT
            String key = (String) map.getKey();
            //参数为博客Id 和 该博客对应的点赞数
            LikedCountDTO dto = new LikedCountDTO(Long.parseLong(key), (Integer) map.getValue());
            list.add(dto);
            //从Redis中删除这条记录
            // redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, key);
        }
        return list;
    }
}