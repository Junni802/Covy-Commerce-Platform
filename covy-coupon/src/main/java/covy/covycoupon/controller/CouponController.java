package covy.covycoupon.controller;

import covy.covycoupon.model.CouponInDto;
import covy.covycoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/apply")
    public void applyCoupon(@RequestBody CouponInDto request) {
        log.info("apply coupon request by userId = {}", request.getUserId());
        couponService.apply(request.getUserId());
    }
}
