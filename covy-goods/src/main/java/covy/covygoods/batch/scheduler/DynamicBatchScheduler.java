package covy.covygoods.batch.scheduler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class DynamicBatchScheduler {

  private final JobLauncher jobLauncher;
  private final ApplicationContext applicationContext;

  @Value("#{${batch.jobs:{}}}")
  private Map<String, String> jobCronMap;

  @Scheduled(cron = "0 * * * * ?") // 매 분마다 스케줄 확인
  public void scheduleJobs() throws Exception {
    for (Map.Entry<String, String> entry : jobCronMap.entrySet()) {
      String jobName = entry.getKey();
      String cronExpression = entry.getValue();

      // 실제 운영 환경에서는 cronExpression 검증 로직을 추가
      // 여기서는 예시로 모든 Job 실행
      Job job = (Job) applicationContext.getBean(jobName);
      jobLauncher.run(job, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters());
    }
  }
}