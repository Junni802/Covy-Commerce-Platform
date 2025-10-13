package covy.covygoods.common.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "batch_job_schedule")
@Getter
@Setter
public class BatchJobSchedule {
  @Id
  private String jobName;

  private String cronExpression;

  private Boolean enabled;
}