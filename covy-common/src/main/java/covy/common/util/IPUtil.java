package covy.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class IPUtil {

  public static String getIpAddr(HttpServletRequest request) {
    String[] headerNames = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    for (String header : headerNames) {
      String ip = request.getHeader(header);
      if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
        // X-Forwarded-For의 경우 여러 IP가 콤마로 연결될 수 있으므로 첫 번째 IP 추출
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
      }
    }

    return request.getRemoteAddr();
  }
}