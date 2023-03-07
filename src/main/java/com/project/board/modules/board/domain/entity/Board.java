package com.project.board.modules.board.domain.entity;

import com.project.board.modules.account.domain.entity.Account;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String title;

    private String content;

    private String writer;

    @CreatedDate // 시간 자동 생성
    @Column(updatable = false)
    private LocalDateTime createdDate;

    private Long countVisit;

    @OneToMany(mappedBy = "board")
    private List<BoardComment> boardCommentList = new ArrayList<>();

    public void setAccount(Account account) {
        this.account = account;
        account.getBoardList().add(this);
    }

    public Board toEntity() {
        return Board.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .createdDate(createdDate)
                .countVisit(countVisit)
                .build();
    }

    @Builder
    public Board(String title, String content, String writer, LocalDateTime createdDate, Long countVisit, Account account, List<BoardComment> boardCommentList) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdDate = createdDate;
        this.countVisit = countVisit;
        if (this.account != null) {
            account.getBoardList().remove(this);
        }
        this.boardCommentList = boardCommentList;

    }
    public void updateVisit(Long countVisit) {
        this.countVisit = countVisit;
    }

    public String update(String title, String content) {
        this.title = title;
        this.content = content;
        return title;
    }
}
