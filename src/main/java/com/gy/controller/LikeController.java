package com.gy.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gy.entity.vo.LikedCountDTO;
import com.gy.queryvo.FirstPageBlog;
import com.gy.queryvo.RecommendBlog;
import com.gy.service.BlogService;
import com.gy.service.LikedService;
import com.gy.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 实现对文章的点赞功能
 */
@Controller
public class LikeController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Autowired
    private LikedService likedService;

    @GetMapping("/like")
    public String like(@RequestParam("id") Long id, @RequestParam("pageNum") Integer pageNum, Model model) throws ExecutionException, InterruptedException {
        redisService.saveLiked2Redis("1", String.valueOf(id));
        redisService.incrementLikedCount(String.valueOf(id));
        List<LikedCountDTO> list = redisService.getLikedCountFromRedis();
        /**
         * 使用异步编排进行优化代码
         */
        CompletableFuture<List<FirstPageBlog>> task1 = CompletableFuture.supplyAsync(() -> {
            PageHelper.startPage(pageNum, 5);
            List<FirstPageBlog> allFirstPageBlog = blogService.getAllFirstPageBlog();
            if (list == null) {
                // 若缓存为空查询数据库
                List<LikedCountDTO> listByDB = likedService.getAllLike();
                for (LikedCountDTO dto : listByDB) {
                    for (FirstPageBlog firstPageBlog : allFirstPageBlog) {
                        if (dto.getBlogId() == firstPageBlog.getId()) {
                            firstPageBlog.setLikeCount(dto.getLikeCount());
                            break;
                        }
                    }
                }
            } else {
                for (LikedCountDTO dto : list) {
                    for (FirstPageBlog firstPageBlog : allFirstPageBlog) {
                        if (dto != null && (dto.getBlogId() == firstPageBlog.getId())) {
                            firstPageBlog.setLikeCount(dto.getLikeCount() + firstPageBlog.getLikeCount());
                            break;
                        }
                    }
                }
            }
            return allFirstPageBlog;
        }, threadPool);
        //返回最新推荐的博客取出前4条
        CompletableFuture<List<RecommendBlog>> task2 = CompletableFuture.supplyAsync(() -> {
            List<RecommendBlog> recommendedBlog = blogService.getRecommendedBlog();
            return recommendedBlog;
        }, threadPool);
        CompletableFuture.allOf(task1, task2).get();
        PageInfo<FirstPageBlog> pageInfo = new PageInfo<>(task1.get());
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("recommendedBlogs", task2.get());
        model.addAttribute("n", 1);
        return "index::blogmessage";
    }
}
