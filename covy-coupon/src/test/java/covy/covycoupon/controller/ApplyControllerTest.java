package covy.covycoupon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import covy.covycoupon.model.CouponInDto;
import covy.covycoupon.repository.CouponCountRepository;
import covy.covycoupon.repository.CouponRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 롤백을 자동으로 수행
class ApplyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponCountRepository couponCountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        // Redis 키 제거
        couponCountRepository.deleteKey("coupon-count");
        couponCountRepository.deleteKey("applied-user");
    }

    @Test
    void applyCoupon() throws Exception {
        // 요청 파라미터
        CouponInDto request = new CouponInDto(1L);

        // API 요청 및 응답 확인
        mockMvc.perform(post("/coupon/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 쿠폰 발급 확인
        Assertions.assertThat(couponRepository.findById(request.getUserId()))
                .isNotEmpty();
    }
}

