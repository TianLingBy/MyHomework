package com.foodboard.controller;

import com.foodboard.service.AdminService;
import com.foodboard.service.MessageService;
import com.foodboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final MessageService messageService;
    private final AdminService adminService;

    public AdminController(UserService userService,
                           MessageService messageService,
                           AdminService adminService) {
        this.userService = userService;
        this.messageService = messageService;
        this.adminService = adminService;
    }

    // ===== 权限校验辅助方法 =====

    private boolean checkAdmin(Boolean isAdmin) {
        return Boolean.TRUE.equals(isAdmin);
    }

    // ===== 数据统计 =====

    /** 获取仪表盘统计数据 */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        return ResponseEntity.ok(adminService.getStats());
    }

    // ===== 每日热评 =====

    /** 获取每日最高点赞留言 */
    @GetMapping("/daily-top")
    public ResponseEntity<?> getDailyTop(@RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        return ResponseEntity.ok(adminService.getDailyTopMessages());
    }

    // ===== 用户管理 =====

    /** 获取所有用户统计信息（支持搜索） */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestAttribute(required = false) Boolean isAdmin,
                                      @RequestParam(required = false) String keyword) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        return ResponseEntity.ok(adminService.searchUsers(keyword));
    }

    /** 设置/取消管理员 */
    @PutMapping("/users/{id}/admin")
    public ResponseEntity<?> toggleAdmin(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        boolean newAdmin = Boolean.TRUE.equals(body.get("isAdmin"));
        return ResponseEntity.ok(adminService.toggleAdmin(id, newAdmin));
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "用户删除成功"));
    }

    // ===== 留言管理 =====

    /** 搜索/获取所有留言 */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestAttribute(required = false) Boolean isAdmin,
                                         @RequestParam(required = false) String keyword) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        return ResponseEntity.ok(adminService.searchMessages(keyword));
    }

    /** 编辑留言内容 */
    @PutMapping("/messages/{id}")
    public ResponseEntity<?> editMessage(@PathVariable Long id,
                                         @RequestBody Map<String, String> body,
                                         @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "留言内容不能为空"));
        }
        return ResponseEntity.ok(adminService.editMessage(id, content));
    }

    /** 删除留言（管理员） */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id,
                                           @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        messageService.deleteMessage(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    /** 批量删除留言 */
    @DeleteMapping("/messages/batch")
    public ResponseEntity<?> batchDeleteMessages(@RequestBody Map<String, List<Long>> body,
                                                  @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择要删除的留言"));
        }
        int count = adminService.batchDeleteMessages(ids);
        return ResponseEntity.ok(Map.of("message", "成功删除 " + count + " 条留言"));
    }

    // ===== 评论管理 =====

    /** 获取所有评论（支持按留言ID筛选和关键词搜索） */
    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestAttribute(required = false) Boolean isAdmin,
                                         @RequestParam(required = false) Long messageId,
                                         @RequestParam(required = false) String keyword) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        return ResponseEntity.ok(adminService.getComments(messageId, keyword));
    }

    /** 删除评论 */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                           @RequestAttribute(required = false) Boolean isAdmin) {
        if (!checkAdmin(isAdmin)) {
            return ResponseEntity.status(403).body(Map.of("error", "需要管理员权限"));
        }
        adminService.deleteComment(id);
        return ResponseEntity.ok(Map.of("message", "评论删除成功"));
    }
}
