package com.project.board.modules.board.application;

import com.project.board.modules.board.domain.entity.Board;
import com.project.board.modules.board.infra.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    private ThreadLocal<Long> countVisitStore = new ThreadLocal<>();

    @Transactional
    public Long saveBoard(Board board) {
        boardRepository.save(board.toEntity());
        return board.getId();
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Transactional
    public Page<Board> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Page<Board> paging(int page) {
        return boardRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional
    public Long updateBoard(@PathVariable("id") Long id, Board board) {
        Board boards =  boardRepository.findById(id).orElseThrow((() ->
                new IllegalStateException("해당 게시글이 존재하지 않습니다.")));

        boards.update(board.getTitle(), board.getContent());
        return id;
    }

    public void deleteBoard(Long id){
        boardRepository.deleteById(id);
    }


    // 조회수
    @Transactional
    public void updateVisit(Long id, Board boards) {
        Board board = boardRepository.findById(id).orElseThrow((() ->
                new IllegalStateException("해당 게시글이 존재하지 않습니다.")));

        board.updateVisit(boards.getCountVisit());
    }

    public Board findById(Long id){
        Board board = boardRepository.findById(id).get();
        return board;
    }

    @Transactional
    public Long countVisitLogic(Long id) {

        Board board = boardRepository.findById(id).orElseThrow((() ->
                new IllegalStateException("해당 게시글이 존재하지 않습니다.")));

        countVisitStore.set(board.getCountVisit() + 1L);
        board.updateVisit(countVisitStore.get());
        sleep(100);

        countVisitStore.remove();
        return countVisitStore.get();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
