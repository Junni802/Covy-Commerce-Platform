package covy.covygoods.batch.scheduler;

import covy.covygoods.batch.properties.BatchJobProperties;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class DynamicBatchScheduler {

  private final JobLauncher jobLauncher;
  private final ApplicationContext applicationContext;
  private final BatchJobProperties batchJobProperties;


  @Scheduled(cron = "0 * * * * ?") // 매 분마다 확인
  public void scheduleJobs() throws Exception {
    LocalDateTime now = LocalDateTime.now();

    for (Map.Entry<String, String> entry : batchJobProperties.getJobs().entrySet()) {
      String jobName = entry.getKey();
      String cronExpression = entry.getValue();

      CronExpression cron = CronExpression.parse(cronExpression);
      LocalDateTime next = cron.next(now.minusSeconds(1)); // 직전 기준 다음 실행시간 계산

      // 지금(now)이 정확히 실행 시간인지 체크
      if (next != null && next.getMinute() == now.getMinute() && next.getHour() == now.getHour()) {
        Job job = (Job) applicationContext.getBean(jobName);
        jobLauncher.run(job, new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters());
      }
    }
  }
}
