package com.foodboard.service;

import com.foodboard.entity.Comment;
import com.foodboard.entity.Message;
import com.foodboard.entity.User;
import com.foodboard.repository.CommentRepository;
import com.foodboard.repository.MessageRepository;
import com.foodboard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final CommentRepository commentRepository;

    public AdminService(UserRepository userRepository,
                        MessageRepository messageRepository,
                        CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.commentRepository = commentRepository;
    }

    /** 获取仪表盘统计数据 */
    public Map<String, Object> getStats() {
        LocalDateTime todayStart = LocalDate.now(ZoneId.of("Asia/Shanghai")).atStartOfDay();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("todayUsers", userRepository.countSince(todayStart));
        stats.put("todayMessages", messageRepository.countSince(todayStart));
        stats.put("todayComments", commentRepository.countSince(todayStart));
        return stats;
    }

    /** 获取每日最高点赞留言（带评论数） */
    public List<Map<String, Object>> getDailyTopMessages() {
        List<Message> messages = messageRepository.findDailyTopMessages();
        return messages.stream().map(msg -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("author", msg.getAuthor());
            m.put("content", msg.getContent());
            m.put("likes", msg.getLikes());
            m.put("viewCount", msg.getViewCount());
            m.put("createdAt", msg.getCreatedAt());
            m.put("commentCount", commentRepository.countByMessageId(msg.getId()));
            return m;
        }).collect(Collectors.toList());
    }

    /** 搜索留言 */
    public List<Map<String, Object>> searchMessages(String keyword) {
        List<Message> messages;
        if (keyword != null && !keyword.trim().isEmpty()) {
            messages = messageRepository.searchByKeyword(keyword.trim());
        } else {
            messages = messageRepository.findAllByOrderByCreatedAtDesc();
        }
        return messages.stream().map(msg -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("author", msg.getAuthor());
            m.put("content", msg.getContent());
            m.put("likes", msg.getLikes());
            m.put("viewCount", msg.getViewCount());
            m.put("createdAt", msg.getCreatedAt());
            m.put("commentCount", commentRepository.countByMessageId(msg.getId()));
            return m;
        }).toList();
    }

    /** 编辑留言内容 */
    public Map<String, Object> editMessage(Long id, String content) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在，ID: " + id));
        message.setContent(content);
        messageRepository.save(message);

        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("author", message.getAuthor());
        result.put("content", message.getContent());
        result.put("likes", message.getLikes());
        result.put("viewCount", message.getViewCount());
        result.put("createdAt", message.getCreatedAt());
        return result;
    }

    /** 批量删除留言 */
    public int batchDeleteMessages(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (messageRepository.existsById(id)) {
                messageRepository.deleteById(id);
                count++;
            }
        }
        return count;
    }

    /** 搜索用户 */
    public List<Map<String, Object>> searchUsers(String keyword) {
        List<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchByKeyword(keyword.trim());
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", user.getId());
            info.put("username", user.getUsername());
            info.put("nickname", user.getNickname());
            info.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
            info.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
            info.put("createdAt", user.getCreatedAt());
            info.put("messageCount", messageRepository.countByAuthor(user.getUsername()));
            result.add(info);
        }
        return result;
    }

    /** 设置/取消管理员 */
    public Map<String, Object> toggleAdmin(Long userId, boolean isAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userId));
        user.setIsAdmin(isAdmin);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        return result;
    }

    /** 删除用户 */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在，ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /** 获取所有评论（可按留言ID筛选） */
    public List<Map<String, Object>> getComments(Long messageId, String keyword) {
        List<Comment> comments;
        if (messageId != null) {
            comments = commentRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            comments = commentRepository.searchByKeyword(keyword.trim());
        } else {
            comments = commentRepository.findAllByOrderByCreatedAtDesc();
        }
        return comments.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("messageId", c.getMessageId());
            m.put("author", c.getAuthor());
            m.put("content", c.getContent());
            m.put("createdAt", c.getCreatedAt());
            return m;
        }).toList();
    }

    /** 删除评论 */
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("评论不存在，ID: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
}
