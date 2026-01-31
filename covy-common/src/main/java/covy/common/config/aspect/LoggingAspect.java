package covy.common.config.aspect;

import covy.common.util.IPUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class LoggingAspect {

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void springBeanPointcut() {
  }

//  @Pointcut("@annotation(io.plura.common.config.aspect.NoLogging) || @within(io.plura.common.config.aspect.NoLogging)")
//  public void noLoggingPointcut() {
//  }


  @Around("springBeanPointcut()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

    long startTime = System.currentTimeMillis();

    String requestFrom = requestFrom();
    Object endResponse;

    loggingIn(requestFrom, joinPoint);

    try {
      endResponse = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      loggingOut(requestFrom, joinPoint, endResponse, duration);

      return endResponse;
    } catch (Exception e) {
      loggingError(joinPoint, e);
      throw e;
    }
  }

  private String requestFrom() {
    ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (servletRequestAttributes != null) {
      HttpServletRequest request = servletRequestAttributes.getRequest();
      String requestUrl = request.getMethod() + " " + request.getRequestURI();
      String remoteIp = IPUtil.getIpAddr(request);
      return requestUrl + " (IP:" + remoteIp + ")";
    }
    return "";
  }

  private void loggingIn(String requestFrom, JoinPoint joinPoint) {
    log.info("### IN ## {} => {}.{}() ## Args: {}",
        requestFrom,
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        Arrays.toString(joinPoint.getArgs())
    );
  }

  private void loggingOut(String requestFrom, JoinPoint joinPoint, Object endResponse, long duration) {
    log.info("### OUT ## {} => {}.{}() ## 결과: {} ## 실행시간 : {}ms",
        requestFrom,
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(), endResponse,
        duration
    );
  }

  private void loggingError(JoinPoint joinPoint, Exception e) {
    log.error("### 예외 발생: {}.{}() ## 메세지: {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
        e.getMessage());
  }

}