package com.foodboard.repository;

import com.foodboard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 查询某条留言的所有评论，按时间正序 */
    List<Comment> findByMessageIdOrderByCreatedAtAsc(Long messageId);

    /** 统计某条留言的评论数 */
    long countByMessageId(Long messageId);

    /** 按时间倒序查询所有评论 */
    List<Comment> findAllByOrderByCreatedAtDesc();

    /** 按关键词搜索评论内容或作者 */
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% OR c.author LIKE %:keyword% ORDER BY c.createdAt DESC")
    List<Comment> searchByKeyword(@Param("keyword") String keyword);

    /** 统计今日新增评论数 */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);
}
