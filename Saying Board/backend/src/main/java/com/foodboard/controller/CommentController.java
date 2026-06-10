package com.foodboard.controller;

import com.foodboard.entity.Comment;
import com.foodboard.entity.User;
import com.foodboard.repository.UserRepository;
import com.foodboard.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages/{messageId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    /** 获取某条留言的所有评论 */
    @GetMapping
    public List<Map<String, Object>> getComments(@PathVariable Long messageId) {
        List<Comment> comments = commentService.getCommentsByMessageId(messageId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("messageId", c.getMessageId());
            map.put("author", c.getAuthor());
            map.put("content", c.getContent());
            map.put("createdAt", c.getCreatedAt());
            // 查找用户头像和管理员状态
            User user = userRepository.findByUsername(c.getAuthor()).orElse(null);
            map.put("avatar", user != null && user.getAvatar() != null ? user.getAvatar() : "");
            map.put("isAdmin", user != null && Boolean.TRUE.equals(user.getIsAdmin()));
            result.add(map);
        }
        return result;
    }

    /** 添加评论 */
    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long messageId,
                                        @RequestBody Map<String, String> body,
                                        @RequestAttribute(required = false) String currentUser) {
        try {
            Comment comment = commentService.addComment(messageId, body.get("content"), currentUser);
            Map<String, Object> result = new HashMap<>();
            result.put("id", comment.getId());
            result.put("messageId", comment.getMessageId());
            result.put("author", comment.getAuthor());
            result.put("content", comment.getContent());
            result.put("createdAt", comment.getCreatedAt());
            User user = userRepository.findByUsername(comment.getAuthor()).orElse(null);
            result.put("avatar", user != null && user.getAvatar() != null ? user.getAvatar() : "");
            result.put("isAdmin", user != null && Boolean.TRUE.equals(user.getIsAdmin()));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 删除评论（管理员或作者本人） */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long messageId,
                                           @PathVariable Long commentId,
                                           @RequestAttribute(required = false) Boolean isAdmin,
                                           @RequestAttribute(required = false) String currentUser) {
        Comment comment = commentService.getCommentById(commentId);
        boolean admin = Boolean.TRUE.equals(isAdmin);
        boolean owner = currentUser != null && currentUser.equals(comment.getAuthor());
        if (!admin && !owner) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限或是评论作者"));
        }
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
