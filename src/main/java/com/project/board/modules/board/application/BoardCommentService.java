package com.project.board.modules.board.application;

import com.project.board.modules.board.domain.entity.BoardComment;
import com.project.board.modules.board.infra.BoardCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardCommentService {
    private final BoardCommentRepository boardCommentRepository;

    @Transactional
    public Long saveBoardComment(BoardComment boardComment){
        boardCommentRepository.save(boardComment.toEntity());
        return boardComment.getId();
    }
}
