package com.foodboard.service;

import com.foodboard.entity.User;
import com.foodboard.repository.MessageRepository;
import com.foodboard.repository.UserRepository;
import com.foodboard.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foodboard.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       MessageRepository messageRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** 注册新用户 */
    public Map<String, Object> register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.length() < 4) {
            throw new RuntimeException("密码长度不能少于4位");
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username.trim());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("isAdmin", false);
        return result;
    }

    /** 用户登录 */
    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        return result;
    }

    /** 根据 token 获取用户信息 */
    public Map<String, Object> getUserByToken(String token) {
        String username = jwtUtil.parseToken(token);
        if (username == null) {
            throw new RuntimeException("token无效或已过期");
        }
        return getUserByUsername(username);
    }

    /** 根据用户名获取用户信息（供 /users/me 直接调用） */
    public Map<String, Object> getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        return result;
    }

    /** 获取所有用户及其统计信息（管理员用） */
    public List<Map<String, Object>> getAllUsersWithStats() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
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

    /** 根据用户名获取头像（用于留言/评论展示） */
    public String getAvatarByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getAvatar() != null ? u.getAvatar() : "")
                .orElse("");
    }

    /** 根据用户名判断是否管理员 */
    public boolean isAdminByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> Boolean.TRUE.equals(u.getIsAdmin()))
                .orElse(false);
    }

    /** 获取用户公开资料（含留言列表） */
    public Map<String, Object> getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        profile.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        profile.put("createdAt", user.getCreatedAt());
        profile.put("messageCount", messageRepository.countByAuthor(user.getUsername()));

        // 该用户的留言列表
        List<Message> messages = messageRepository.findByAuthorOrderByCreatedAtDesc(username);
        List<Map<String, Object>> msgList = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("author", msg.getAuthor());
            m.put("content", msg.getContent());
            m.put("likes", msg.getLikes());
            m.put("viewCount", msg.getViewCount());
            m.put("createdAt", msg.getCreatedAt());
            msgList.add(m);
        }
        profile.put("messages", msgList);

        return profile;
    }

    /** 更新当前用户的昵称和头像 */
    public Map<String, Object> updateProfile(String currentUser, String nickname, String avatar) {
        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (nickname != null && !nickname.trim().isEmpty()) {
            if (nickname.trim().length() > 30) {
                throw new RuntimeException("昵称不能超过30个字符");
            }
            user.setNickname(nickname.trim());
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        return result;
    }
}
