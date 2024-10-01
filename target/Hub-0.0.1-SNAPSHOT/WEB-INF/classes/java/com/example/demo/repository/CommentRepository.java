package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Mention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    @Query("SELECT c FROM Comment c WHERE c.parentComment IS NULL AND c.rootComment IS NULL AND c.post.id = :postId ORDER BY c.createdAt DESC")
    Page<Comment> fetchCommentByPostIdWithoutParent(Long postId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c where c.post.id = :postId")
    Long countByPostId(@Param("postId")Long postId);

    @Query("SELECT COUNT(c) FROM Comment c where c.post.id = :postId and ( c.parentComment.id = :parentCommentId OR c.rootComment.id = :parentCommentId)")
    Long countByTopLevelComment(@Param("postId")Long postId,@Param("parentCommentId")Long commentId);

    @Query("SELECT c FROM Comment c where c.parentComment.id = :parentId OR c.rootComment.id = :parentId")
    List<Comment> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM CommentLike c WHERE c.likeStatus = true AND c.comment.id = :commentId")
    Long getTotalLike(@Param("commentId")Long commentId);


}
