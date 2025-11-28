package covy.covycoupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CovyCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(CovyCouponApplication.class, args);
	}

}
