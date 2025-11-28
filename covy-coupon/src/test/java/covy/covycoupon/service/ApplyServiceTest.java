package covy.covycoupon.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import covy.covycoupon.repository.CouponRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplyServiceTest {
    @Autowired
    private CouponService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모() {
        applyService.apply(1L);

        long count = couponRepository.count();

        // 쿠폰 1개가 정상적으로 발급되었는지 검증
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void 여러명응모() throws InterruptedException {
        // 동시에 여러 요청이 들어오기 때문에 멀티쓰레드를 사용한다.
        // 1000개의 요청이 동시에 들어오는 경우를 가정한다.
        int threadCount = 2000;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // count 값을 threadCount 값으로 초기화
        CountDownLatch latch = new CountDownLatch(threadCount);
        for(int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.execute(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    // count 값을 1 감소
                    latch.countDown();
                }

            });
        }

        // await() 이후 로직은 count 값이 0이 되고 나서 실행된다.
        latch.await();

        Thread.sleep(1000);

        // 100개의 쿠폰이 생성된 것을 예상
        long count = couponRepository.count();
        assertThat(count).isEqualTo(1000);
    }


}