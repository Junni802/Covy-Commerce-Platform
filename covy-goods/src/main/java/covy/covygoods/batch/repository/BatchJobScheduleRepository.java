package covy.covygoods.batch.repository;

import covy.covygoods.batch.entity.BatchJobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobScheduleRepository extends JpaRepository<BatchJobSchedule, String> {
}