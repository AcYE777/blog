package com.gy.service.Impl;

import com.gy.constants.LikedStatusEnum;
import com.gy.dao.LikeDao;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LikedServiceImpl implements LikedService {

    @Autowired
    LikeDao likeDao;

    @Autowired
    RedisService redisService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    @Transactional
    public void save(Like like) {
        likeDao.save(like);
    }

    @Override
    @Transactional
    public List<Like> saveAll(List<Like> list) {
        return likeDao.saveAll(list);
    }

    @Override
    public List<Like> getLikedListByLikedUserId(String blogId) {
        return likeDao.findByBlogIddAndStatus(Long.parseLong(blogId), LikedStatusEnum.LIKE.getCode());
    }

    @Override
    public Like getByLikedUserIdAndLikedPostId(String userId, String blogId) {
        return likeDao.findByUserIdAndBlogId(Long.parseLong(userId), Long.parseLong(blogId));
    }

    @Override
    @Transactional
    public void transLikedFromRedis2DB() {
        List<Like> list = redisService.getLikedDataFromRedis();
        if (list != null) {
            for (Like like : list) {
                Like ul = getByLikedUserIdAndLikedPostId(String.valueOf(like.getUserId()), String.valueOf(like.getBlogId()));
                if (ul == null){
                    //没有记录，直接存入
                    Object likeCount = redisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, String.valueOf(like.getBlogId()));
                    like.setLikeCount((Integer) likeCount);
                    save(like);
                    System.out.println("------------不存在记录进行插入------------");
                    deleteCacheData();
                }else{
                    //有记录，需要更新
                    ul.setStatus(like.getStatus());
                    likeDao.updateStatusByBlogId(ul.getBlogId(), ul.getStatus());
                    System.out.println("------------存在记录进行更新------------");
                    deleteCacheData();
                }
            }
        }
    }

    @Override
    @Transactional
    public void transLikedCountFromRedis2DB() {
        List<LikedCountDTO> list = redisService.getLikedCountFromRedis();
        for (LikedCountDTO dto : list) {
            Like like = likeDao.selectLikeByBlogId(dto.getBlogId());
            //点赞数量属于无关紧要的操作，出错无需抛异常
            if (like != null) {
                System.out.println(like.getLikeCount() + "================点赞数==================");
                Integer likeNum = like.getLikeCount() + dto.getLikeCount();
                like.setLikeCount(likeNum);
                //更新点赞数量
                likeDao.updateLikeCountByBlogId(like.getBlogId(), like.getLikeCount());
                System.out.println("-----------------------从缓存更新点赞数--------------------------");
                deleteCacheCount();
            }
        }
    }

    public void deleteCacheData() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED, ScanOptions.NONE);
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();
            String key = (String) entry.getKey();
            //存到 list 后从 Redis 中删除
            redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED, key);
        }
    }

    public void deleteCacheCount() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, ScanOptions.NONE);
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            String key = (String) map.getKey();
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, key);
        }
    }
    @Override
    public List<LikedCountDTO> getAllLike() {
        return likeDao.getAllLike();
    }
}