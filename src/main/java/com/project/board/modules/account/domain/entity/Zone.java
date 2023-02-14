package com.project.board.modules.account.domain.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
/**
 * 지역 도메인 설계
 * 엔티티 타입
 *
 * 속성은 아래와 같다
 * city: 영문 도시 이름
 * localNameOfCity: 한국어 도시 이름
 * province: 주(도) 이름, nullable
 *
 * Account와 Zone의 객체간 관계 : 단방향 다대다 관계 (->)
 * Account와 Zone의 관계형 관계 : 조인 테이블 사용, 일대다 관계
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Zone {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    private String province;

    public static Zone map(String line) {
        String[] split = line.split(",");
        Zone zone = new Zone();
        zone.city = split[0];
        zone.localNameOfCity = split[1];
        zone.province = split[2];
        return zone;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}