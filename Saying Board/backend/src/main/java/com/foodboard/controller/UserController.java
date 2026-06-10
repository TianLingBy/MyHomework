package com.foodboard.controller;

import com.foodboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 用户注册 */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            Map<String, Object> result = userService.register(username, password);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 用户登录 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            Map<String, Object> result = userService.login(username, password);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestAttribute(required = false) String currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        try {
            Map<String, Object> result = userService.getUserByUsername(currentUser);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** 获取用户公开资料 */
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        try {
            Map<String, Object> result = userService.getUserProfile(username);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 更新当前用户的昵称和头像 */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body,
                                           @RequestAttribute(required = false) String currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        try {
            Map<String, Object> result = userService.updateProfile(
                    currentUser, body.get("nickname"), body.get("avatar"));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
