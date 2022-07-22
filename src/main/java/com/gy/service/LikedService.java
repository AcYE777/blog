package com.gy.service;

import com.gy.entity.Like;
import com.gy.entity.vo.LikedCountDTO;

import java.util.List;

public interface LikedService {

    /**
     * 保存点赞记录
     * @return
     */
    void save(Like like);

    /**
     * 批量保存或修改
     * @param list
     */
    List<Like> saveAll(List<Like> list);


    /**
     * 根据被点赞人的id查询点赞列表（即查询都谁给这个人点赞过）
     * @return
     */
    List<Like> getLikedListByLikedUserId(String blogId);

    /**
     * 通过被点赞博客和点赞人id查询是否存在点赞记录
     * @return
     */
    Like getByLikedUserIdAndLikedPostId(String userId, String blogId);

    /**
     * 将Redis里的点赞数据存入数据库中
     */
    void transLikedFromRedis2DB();

    /**
     * 将Redis中的点赞数量数据存入数据库
     */
    void transLikedCountFromRedis2DB();

    List<LikedCountDTO> getAllLike();
}