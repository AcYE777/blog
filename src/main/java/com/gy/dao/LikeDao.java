package com.gy.dao;

import com.gy.entity.Blog;
import com.gy.entity.Like;
import com.gy.entity.vo.LikedCountDTO;
import com.gy.queryvo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface LikeDao {

    Like selectLikeByBlogId(Long id);

    void save(Like like);

    List<Like> saveAll(List<Like> list);

    List<Like> findByBlogIddAndStatus(@Param("blogId") Long blogId, @Param("code") Integer code);

    Like findByUserIdAndBlogId(@Param("userId") Long userId, @Param("blogId") Long blogId);

    void updateLikeCountByBlogId(@Param("blogId") Long id, @Param("likeCount") Integer likeCount);

    List<LikedCountDTO> getAllLike();

    void updateStatusByBlogId(Long blogId);

    void updateStatusByBlogId(@Param("blogId") Long blogId, @Param("status") Integer status);
}