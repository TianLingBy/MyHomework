package com.foodboard.controller;

import com.foodboard.entity.Message;
import com.foodboard.service.CommentService;
import com.foodboard.service.MessageService;
import com.foodboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final CommentService commentService;
    private final UserService userService;

    public MessageController(MessageService messageService, CommentService commentService, UserService userService) {
        this.messageService = messageService;
        this.commentService = commentService;
        this.userService = userService;
    }

    /** 获取留言列表（带评论数，支持 ?today=true 只返回今日） */
    @GetMapping
    public List<Map<String, Object>> getAll(@RequestParam(required = false) Boolean today) {
        List<Message> messages = Boolean.TRUE.equals(today)
                ? messageService.getTodayMessages()
                : messageService.getAllMessages();
        return messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("author", msg.getAuthor());
            map.put("content", msg.getContent());
            map.put("likes", msg.getLikes());
            map.put("viewCount", msg.getViewCount());
            map.put("createdAt", msg.getCreatedAt());
            map.put("updatedAt", msg.getUpdatedAt());
            map.put("commentCount", commentService.countByMessageId(msg.getId()));
            map.put("avatar", userService.getAvatarByUsername(msg.getAuthor()));
            map.put("isAdmin", userService.isAdminByUsername(msg.getAuthor()));
            return map;
        }).collect(Collectors.toList());
    }

    /** 获取每日最高点赞留言 */
    @GetMapping("/daily-top")
    public List<Map<String, Object>> getDailyTop() {
        List<Message> messages = messageService.getDailyTopMessages();
        return messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("author", msg.getAuthor());
            map.put("content", msg.getContent());
            map.put("likes", msg.getLikes());
            map.put("viewCount", msg.getViewCount());
            map.put("createdAt", msg.getCreatedAt());
            map.put("updatedAt", msg.getUpdatedAt());
            map.put("commentCount", commentService.countByMessageId(msg.getId()));
            map.put("avatar", userService.getAvatarByUsername(msg.getAuthor()));
            map.put("isAdmin", userService.isAdminByUsername(msg.getAuthor()));
            return map;
        }).collect(Collectors.toList());
    }

    /** 发布新留言 */
    @PostMapping
    public Message create(@RequestBody Message message,
                          @RequestAttribute(required = false) String currentUser) {
        return messageService.createMessage(message, currentUser);
    }

    /** 点赞 */
    @PutMapping("/{id}/like")
    public ResponseEntity<Message> like(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.likeMessage(id));
    }

    /** 取消点赞 */
    @PutMapping("/{id}/unlike")
    public ResponseEntity<Message> unlike(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.unlikeMessage(id));
    }

    /** 获取单条留言详情（浏览量+1，带评论数） */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Message msg = messageService.incrementViewCount(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", msg.getId());
        map.put("author", msg.getAuthor());
        map.put("content", msg.getContent());
        map.put("likes", msg.getLikes());
        map.put("viewCount", msg.getViewCount());
        map.put("createdAt", msg.getCreatedAt());
        map.put("updatedAt", msg.getUpdatedAt());
        map.put("commentCount", commentService.countByMessageId(msg.getId()));
        map.put("avatar", userService.getAvatarByUsername(msg.getAuthor()));
        map.put("isAdmin", userService.isAdminByUsername(msg.getAuthor()));
        return ResponseEntity.ok(map);
    }

    /** 删除留言（管理员或作者本人） */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestAttribute(required = false) Boolean isAdmin,
                                    @RequestAttribute(required = false) String currentUser) {
        Message msg = messageService.getMessageById(id);
        boolean admin = Boolean.TRUE.equals(isAdmin);
        boolean owner = currentUser != null && currentUser.equals(msg.getAuthor());
        if (!admin && !owner) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限或是作者本人"));
        }
        messageService.deleteMessage(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
