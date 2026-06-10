package com.foodboard.service;

import com.foodboard.entity.Comment;
import com.foodboard.repository.CommentRepository;
import com.foodboard.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;

    public CommentService(CommentRepository commentRepository, MessageRepository messageRepository) {
        this.commentRepository = commentRepository;
        this.messageRepository = messageRepository;
    }

    /** 获取某条留言的所有评论 */
    public List<Comment> getCommentsByMessageId(Long messageId) {
        return commentRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
    }

    /** 添加评论 */
    public Comment addComment(Long messageId, String content, String currentUser) {
        if (!messageRepository.existsById(messageId)) {
            throw new RuntimeException("留言不存在，ID: " + messageId);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        Comment comment = new Comment();
        comment.setMessageId(messageId);
        comment.setContent(content.trim());
        comment.setAuthor(currentUser != null && !currentUser.isEmpty() ? currentUser : "匿名吃货");
        return commentRepository.save(comment);
    }

    /** 根据ID获取评论 */
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在，ID: " + commentId));
    }

    /** 删除评论（管理员） */
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("评论不存在，ID: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    /** 统计某条留言的评论数 */
    public long countByMessageId(Long messageId) {
        return commentRepository.countByMessageId(messageId);
    }
}
