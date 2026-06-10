package com.foodboard.repository;

import com.foodboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据用户名查找用户 */
    Optional<User> findByUsername(String username);

    /** 检查用户名是否已存在 */
    boolean existsByUsername(String username);

    /** 按注册时间倒序查询所有用户 */
    List<User> findAllByOrderByCreatedAtDesc();

    /** 按关键词搜索用户名或昵称 */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% ORDER BY u.createdAt DESC")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    /** 统计今日新增用户数 */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);
}
