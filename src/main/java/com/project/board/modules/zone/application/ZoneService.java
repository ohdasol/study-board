package com.project.board.modules.zone.application;

import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.zone.infra.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;

    /**
     * @PostConstruct : 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
     *
     * @PostConstruct으로 인해 빈 등록 이후에 해당 메서드가 실행
     * Zone Entity 클래스에서 문자열을 Zone Entity로 매핑해 주는 static 메서드는 파일을 읽어와 각 라인을 바로 매핑하기 위해 구현
     * 매핑한 결과를 ZoneRepository.saveAll 메서드를 이용해 모두 저장해 주면 지역 데이터 초기화 완료
     */
    @PostConstruct
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("zones_kr.csv");
            List<String> allLines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
            List<Zone> zones = allLines.stream().map(Zone::map).collect(Collectors.toList());
            zoneRepository.saveAll(zones);
        }
    }
}
