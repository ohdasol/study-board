package com.project.board.modules.tag.application;

import com.project.board.modules.tag.infra.repository.TagRepository;
import com.project.board.modules.tag.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {
    private final TagRepository tagRepository;

    // 태그 존재할 경우 찾아서 반환, 존재하지 않을 경우 TagRepository에 저장 후 반환
    public Tag findOrCreateNew(String tagTitle) {
        return tagRepository.findByTitle(tagTitle).orElseGet(
                () -> tagRepository.save(Tag.builder()
                        .title(tagTitle)
                        .build())
        );
    }
}
