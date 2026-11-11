package com.hakandincturk.security.session;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads device details from the login request. Production sits behind Cloudflare and UFW only
 * admits Cloudflare ranges, so CF-Connecting-IP / CF-IPCountry can be trusted there.
 */
@Component
public class ClientInfoResolver {

  private static final int MAX_USER_AGENT = 512;
  private static final int MAX_IP = 45;

  public ClientInfo resolve(HttpServletRequest request) {
    return new ClientInfo(userAgent(request), ip(request), country(request));
  }

  private String userAgent(HttpServletRequest request) {
    String ua = request.getHeader("User-Agent");
    if (ua == null) return null;
    return ua.length() > MAX_USER_AGENT ? ua.substring(0, MAX_USER_AGENT) : ua;
  }

  private String ip(HttpServletRequest request) {
    String ip = firstNonBlank(request.getHeader("CF-Connecting-IP"), request.getHeader("X-Real-IP"), request.getRemoteAddr());
    if (ip == null) return null;
    ip = ip.trim();
    return ip.length() > MAX_IP ? ip.substring(0, MAX_IP) : ip;
  }

  private String country(HttpServletRequest request) {
    String country = request.getHeader("CF-IPCountry");
    return country != null && country.trim().length() == 2 ? country.trim().toUpperCase() : null;
  }

  private static String firstNonBlank(String... values) {
    for (String v : values) {
      if (v != null && !v.isBlank()) return v;
    }
    return null;
  }
}
