package com.foodboard.repository;

import com.foodboard.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** 按创建时间倒序查询所有留言 */
    List<Message> findAllByOrderByCreatedAtDesc();

    /** 统计某用户的留言数 */
    long countByAuthor(String author);

    /** 按作者查询留言，按时间倒序 */
    List<Message> findByAuthorOrderByCreatedAtDesc(String author);

    /** 按关键词搜索留言内容或作者 */
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword% OR m.author LIKE %:keyword% ORDER BY m.createdAt DESC")
    List<Message> searchByKeyword(@Param("keyword") String keyword);

    /** 统计今日新增留言数 */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);

    /** 查询今日留言 */
    @Query("SELECT m FROM Message m WHERE m.createdAt >= :since ORDER BY m.createdAt DESC")
    List<Message> findTodayMessages(@Param("since") LocalDateTime since);

    /** 获取每一天点赞数最高的留言（使用窗口函数） */
    @Query(value = "SELECT * FROM (" +
            "SELECT *, ROW_NUMBER() OVER (PARTITION BY DATE(created_at) ORDER BY likes DESC, created_at DESC) AS rn " +
            "FROM message " +
            ") ranked WHERE rn = 1 ORDER BY created_at DESC", nativeQuery = true)
    List<Message> findDailyTopMessages();
}
