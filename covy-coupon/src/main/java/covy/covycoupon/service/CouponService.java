package covy.covycoupon.service;

import covy.covycoupon.common.kafka.producer.CouponCreateProducer;
import covy.covycoupon.repository.CouponCountRepository;
import covy.covycoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);
    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;

    // 쿠폰 발급 로직
    public void apply(Long userId) {
        boolean success = couponCountRepository.issueCoupon(userId);

        if (!success) {
            return; // 이미 발급 받았거나 수량 초과
        }

        // 발급 성공 시 처리
        couponCreateProducer.create(userId);
    }
}
