package com.project.board.modules.board.infra;

import com.project.board.modules.board.domain.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    @Query("select c from BoardComment c where c.board.id = :id")
    List<BoardComment> findCommentId(@Param("id") Long id);
}
