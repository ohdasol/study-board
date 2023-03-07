package com.project.board.modules.board.endpoint;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.infra.repository.AccountRepository;
import com.project.board.modules.board.application.BoardCommentService;
import com.project.board.modules.board.application.BoardService;
import com.project.board.modules.board.domain.entity.Board;
import com.project.board.modules.board.domain.entity.BoardComment;
import com.project.board.modules.board.infra.BoardCommentRepository;
import com.project.board.modules.board.infra.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardCommentController {

    private final BoardCommentService boardCommentService;
    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final AccountRepository accountRepository;

    // 댓글 작성
    @PostMapping("/boardContent/{id}")
    public String addComment(@PathVariable("id") Long id, @ModelAttribute BoardComment boardComment, Model model) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        Board board = boardRepository.findById(id).get();
        Account account = accountRepository.findByNickname(username);

        LocalDateTime now = LocalDateTime.now();

        boardComment.setWriter(username);
        boardComment.setCreatedDate(now);
        boardComment.setDeleteCheck('N');
        boardComment.setAccount(account);
        boardComment.setBoard(board);

        boardCommentService.saveBoardComment(boardComment);

        List<BoardComment> comments = boardCommentRepository.findCommentId(id);

        model.addAttribute("comments", comments);
        model.addAttribute(board);
        return "board/boardContent";
    }
}
