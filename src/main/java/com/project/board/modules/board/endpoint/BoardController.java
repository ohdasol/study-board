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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;

    // 글 작성 뷰
    @GetMapping("/boardForm")
    public String viewBoardForm() {
        return "board/boardForm";
    }

    // 글 작성
    @PostMapping("/boardForm")
    public String createBoard(@ModelAttribute Board board, Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        board.setWriter(username);
        board.setCountVisit(1L);
        boardService.saveBoard(board);

        return "redirect:/board/boardList";
    }

    // 게시판 리스트
    // 페이징 처리, 검색
    @GetMapping("/boardList")
    public String viewBoardList(Model model, @PageableDefault(size = 10) Pageable pageable,
                            @RequestParam(required = false, defaultValue = "") String searchText) {

        Page<Board> boards = boardRepository.findByTitleContainingOrContentContaining(searchText, searchText, pageable);

        int startPage = Math.max(1, boards.getPageable().getPageNumber() - 1);
        int endPage = Math.min(boards.getTotalPages(), boards.getPageable().getPageNumber() + 3);

        model.addAttribute("boards", boards);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "/board/boardList";
    }

    // 상세 내용
    @GetMapping("/boardContent/{id}")
    public String boardContent(@PathVariable("id") Long id, Model model) {
        Board boards = boardRepository.findById(id).get();
        List<BoardComment> comments = boardCommentRepository.findCommentId(id);

        Board board = Board.builder()
                .countVisit(boards.getCountVisit())
                .build();

        boardService.updateVisit(boards.getId(), board);
        boardService.countVisitLogic(id);

        model.addAttribute(boards);
        model.addAttribute("comments", comments);
        return "board/boardContent";
    }

    // 글 수정
    @GetMapping("/boardUpdate/{id}")
    public String viewBoardUpdate(@PathVariable("id") Long id, Model model) {
        Board board = boardRepository.findById(id).get();
        model.addAttribute("board", board);
        return "board/boardUpdate";
    }

    @PostMapping("/boardUpdate/{id}")
    public String boardUpdate(@PathVariable("id") Long id, Board board) {
        Board boards = boardRepository.findById(id).get();

        boards.setTitle(board.getTitle());
        boards.setContent(board.getContent());
        boardService.updateBoard(id, board);
        return "redirect:/board/boardList";
    }

    // 글 삭제
    @GetMapping("/delete")
    public String deleteBoard(Long id) {
        boardService.deleteBoard(id);
        return "redirect:/board/boardList";
    }
}
