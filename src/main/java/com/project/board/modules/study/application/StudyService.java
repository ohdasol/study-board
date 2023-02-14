package com.project.board.modules.study.application;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.tag.domain.entity.Tag;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.endpoint.form.StudyDescriptionForm;
import com.project.board.modules.study.endpoint.form.StudyForm;
import com.project.board.modules.study.event.StudyCreatedEvent;
import com.project.board.modules.study.event.StudyUpdateEvent;
import com.project.board.modules.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {
    private final StudyRepository studyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 스터디 폼과 현재 로그인 중인 계정 정보를 전달받아 스터디 생성
     * 스터디 생성 후에는 생성한 계정을 관리자로 등록해 주고 DB에 저장
     */
    public Study createNewStudy(StudyForm studyForm, Account account) {
        Study study = Study.from(studyForm);
        study.addManager(account);
        return studyRepository.save(study);
    }

    // 일반 사용자의 접근과 관리자가 수정하기 위해 접근할 때를 구분
    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkStudyExists(path, study);
        return study;
    }

    public Study getStudyToUpdate(Account account, String path) {
        return getStudy(account, path, studyRepository.findByPath(path));
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        return getStudy(account, path, studyRepository.findStudyWithTagsByPath(path));
    }

    /**
     * 스터디 설정 내에서 관심 주제를 변경하거나 지역을 변경할 때는 각각 관심 주제(tag)와 지역(zone) 정보가 스터디와 함께 조회되어야 함
     * 메서드를 분리하여 해당하는 테이블만 join
     */
    public Study getStudyToUpdateZone(Account account, String path) {
        return getStudy(account, path, studyRepository.findStudyWithZonesByPath(path));
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        return getStudy(account, path, studyRepository.findStudyWithManagersByPath(path));
    }

    // 스터디 존재, 관리자 여부 검증 메서드도 추출
    private Study getStudy(Account account, String path, Study studyByPath) {
        checkStudyExists(path, studyByPath);
        checkAccountIsManager(account, studyByPath);
        return studyByPath;
    }

    private void checkStudyExists(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    private void checkAccountIsManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    // 스터디 설명 수정
    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        study.updateDescription(studyDescriptionForm);
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 소개를 수정했습니다.")); // 스터디가 수정될 때 이벤트 발생시킴
    }

    public void updateStudyImage(Study study, String image) {
        study.updateImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setBanner(false);
    }

    public void addTag(Study study, Tag tag) {
        study.addTag(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.removeTag(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.addZone(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.removeZone(zone);
    }

    public void publish(Study study) {
        study.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디를 종료했습니다."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 시작합니다."));
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 종료했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(StudyForm.VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.updatePath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.updateTitle(newTitle);
    }

    public void remove(Study study) {
        if (!study.isRemovable()) {
            throw new IllegalStateException("스터디를 삭제할 수 없습니다.");
        }
        studyRepository.delete(study);
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }

    public Study getStudyToEnroll(String path) {
        return studyRepository.findStudyOnlyByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + "에 해당하는 스터디가 존재하지 않습니다."));
    }
}
