package com.project.board.modules.board.domain.entity;

import com.project.board.modules.account.domain.entity.Account;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_comment_id")
    private Long id;
    private String content;
    private LocalDateTime createdDate;
    private Character deleteCheck;
    private String writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
    @Builder
    public BoardComment(String content, LocalDateTime createdDate, String writer, Character deleteCheck, Board board, Account account) {
        this.content = content;
        this.createdDate = createdDate;
        this.writer = writer;
        this.deleteCheck = deleteCheck;
        if(this.board != null){
            board.getBoardCommentList().remove(this);
        }else
            this.board = board;
        if(this.account != null){
            account.getBoardCommentList().remove(this);
        }else
            this.account = account;
    }

    public BoardComment toEntity() {
        return BoardComment.builder()
                .content(content)
                .writer(writer)
                .createdDate(createdDate)
                .deleteCheck(deleteCheck)
                .account(account)
                .board(board)
                .build();

    }
}
