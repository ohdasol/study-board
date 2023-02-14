package com.project.board.modules.event.infra.repository;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.event.domain.entity.Enrollment;
import com.project.board.modules.event.domain.entity.Event;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  boolean existsByEventAndAccount(Event event, Account account);

  Enrollment findByEventAndAccount(Event event, Account account);

  @EntityGraph("Enrollment.withEventAndStudy")
  List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, boolean accepted);
}