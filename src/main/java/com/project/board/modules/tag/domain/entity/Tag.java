package com.project.board.modules.tag.domain.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
/**
 * 관심 주제 도메인 설계
 * 관심 주제는 Tag 형태로 관리, 엔티티로 취급
 * Tag 자체의 독자적인 Life Cycle을 가지고 다른 엔티티의 참조가 필요하기 때문
 * Account가 Tag를 참조하고 다대다 관계
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class Tag {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String title;
}
