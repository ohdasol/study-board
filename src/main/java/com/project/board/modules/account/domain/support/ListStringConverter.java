package com.project.board.modules.account.domain.support;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Converter // List를 DB 컬럼 하나에 매핑하기 위한 클래스
public class ListStringConverter implements AttributeConverter<List<String>, String> { // 인터페이스 상속
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return Optional.ofNullable(attribute)
                .filter(list -> !list.isEmpty()) // 비었을 때 아무것도 하지 않음
                .map(a -> String.join(",", a))
                .orElse(null);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Collections.emptyList();
        }
        return Stream.of(dbData.split(","))
                .collect(Collectors.toList());
    }
}
