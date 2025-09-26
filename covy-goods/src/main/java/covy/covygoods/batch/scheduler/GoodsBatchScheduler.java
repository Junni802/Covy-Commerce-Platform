package covy.covygoods.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class GoodsBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job goodsJob;

  @Scheduled(cron = "0 0 2 * * ?")
  public void runGoodsJob() throws Exception {
    jobLauncher.run(goodsJob, new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters());
  }

}
