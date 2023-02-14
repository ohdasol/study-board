package com.project.board.modules.account.infra.predicates;

import com.project.board.modules.account.domain.entity.QAccount;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.tag.domain.entity.Tag;
import com.querydsl.core.types.Predicate;

import java.util.Set;

public class AccountPredicates {

    /**
     * tags, zones에 포함되는 계정을 찾기위해 Predicate를 파라미터로 전달, querydsl에서 제공하는 조건절에 해당하는 타입
     *
     * 계정이 가진 지역 관련 정보 중 어느 하나라도 전달된 지역 정보에 포함되는지, 관심사도 마찬가지인지 확인하는 조건절
     */
    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
