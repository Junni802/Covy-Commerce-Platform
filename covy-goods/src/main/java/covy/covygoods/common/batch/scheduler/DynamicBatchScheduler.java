package covy.covygoods.common.batch.scheduler;

import covy.covygoods.common.batch.properties.BatchJobProperties;
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

/**
 * 동적 배치 스케줄러
 *
 * application.yml 또는 properties에 정의된 배치 Job과 cron 표현식을 기반으로
 * 정해진 주기에 맞춰 Job을 실행한다.
 */
@RequiredArgsConstructor
@Component
@EnableScheduling
public class DynamicBatchScheduler {

  private final JobLauncher jobLauncher; // Spring Batch JobLauncher
  private final ApplicationContext applicationContext; // Bean 조회용 ApplicationContext
  private final BatchJobProperties batchJobProperties; // 배치 Job 이름과 cron 매핑 정보

  /**
   * 매 분마다 스케줄 확인
   *
   * 배치 Job Properties에서 정의한 모든 Job의 cron 표현식을 확인하고,
   * 현재 시간(now)이 실행 시간과 일치하면 Job 실행
   *
   * @throws Exception Job 실행 중 예외 발생 가능
   */
  @Scheduled(cron = "0 * * * * ?") // 매 분마다 실행
  public void scheduleJobs() throws Exception {
    LocalDateTime now = LocalDateTime.now();

    // 정의된 모든 배치 Job 순회
    for (Map.Entry<String, String> entry : batchJobProperties.getJobs().entrySet()) {
      String jobName = entry.getKey();
      String cronExpression = entry.getValue();

      // cron 표현식 파싱
      CronExpression cron = CronExpression.parse(cronExpression);

      // 직전 기준 다음 실행 시간 계산
      LocalDateTime next = cron.next(now.minusSeconds(1));

      // 현재 시간이 실행 시간과 일치하면 Job 실행
      if (next != null && next.getMinute() == now.getMinute() && next.getHour() == now.getHour()) {
        Job job = (Job) applicationContext.getBean(jobName); // Job Bean 조회
        jobLauncher.run(job, new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()) // JobParameters에 timestamp 추가
            .toJobParameters());
      }
    }
  }
}
