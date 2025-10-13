package covy.covygoods.common.batch.repository;

import covy.covygoods.common.batch.entity.BatchJobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobScheduleRepository extends JpaRepository<BatchJobSchedule, String> {
}