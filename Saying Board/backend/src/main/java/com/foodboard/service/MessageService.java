package com.foodboard.service;

import com.foodboard.entity.Message;
import com.foodboard.repository.MessageRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Service
public class MessageService {

    private final MessageRepository repository;
    private final Random random = new Random();

    // "XX的XX" 昵称素材
    private static final String[] PREFIX = {
        "快乐", "干饭", "觅食", "贪吃", "佛系", "养生", "暴走", "摸鱼",
        "躺平", "卷王", "社恐", "显眼", "脆皮", "摆烂", "发疯", "整活"
    };
    private static final String[] SUFFIX = {
        "吃货", "小猫", "小狗", "熊猫", "仓鼠", "企鹅", "水豚", "兔兔",
        "干饭人", "美食家", "探店王", "外卖侠", "食堂人", "夜宵党", "奶茶精", "火锅魂"
    };

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    /** 生成 "XX的XX" 格式昵称 */
    private String generateNickname() {
        String pre = PREFIX[random.nextInt(PREFIX.length)];
        String suf = SUFFIX[random.nextInt(SUFFIX.length)];
        return pre + "的" + suf;
    }

    /** 获取所有留言 */
    public List<Message> getAllMessages() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /** 获取今日留言 */
    public List<Message> getTodayMessages() {
        LocalDateTime todayStart = LocalDate.now(ZoneId.of("Asia/Shanghai")).atStartOfDay();
        return repository.findTodayMessages(todayStart);
    }

    /** 发布新留言（已登录用用户名，未登录随机昵称） */
    public Message createMessage(Message message, String currentUser) {
        if (currentUser != null && !currentUser.isEmpty()) {
            message.setAuthor(currentUser);
        } else {
            message.setAuthor(generateNickname());
        }
        if (message.getLikes() == null) {
            message.setLikes(0);
        }
        if (message.getViewCount() == null) {
            message.setViewCount(0);
        }
        return repository.save(message);
    }

    /** 点赞 +1 */
    public Message likeMessage(Long id) {
        Message message = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在，ID: " + id));
        message.setLikes(message.getLikes() + 1);
        return repository.save(message);
    }

    /** 取消点赞 -1 */
    public Message unlikeMessage(Long id) {
        Message message = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在，ID: " + id));
        message.setLikes(Math.max(0, message.getLikes() - 1));
        return repository.save(message);
    }

    /** 根据ID获取留言 */
    public Message getMessageById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在，ID: " + id));
    }

    /** 删除留言（管理员） */
    public void deleteMessage(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("留言不存在，ID: " + id);
        }
        repository.deleteById(id);
    }

    /** 获取每一天点赞数最高的留言 */
    public List<Message> getDailyTopMessages() {
        return repository.findDailyTopMessages();
    }

    /** 浏览量 +1 */
    public Message incrementViewCount(Long id) {
        Message message = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在，ID: " + id));
        int current = message.getViewCount() != null ? message.getViewCount() : 0;
        message.setViewCount(current + 1);
        return repository.save(message);
    }
}
