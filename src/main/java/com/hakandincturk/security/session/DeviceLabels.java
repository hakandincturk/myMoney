package com.hakandincturk.security.session;

/**
 * Turns a User-Agent header into a short label such as "Chrome · macOS" for the session list.
 * Covers the major browsers and platforms only; anything else is "Bilinmeyen cihaz".
 */
public final class DeviceLabels {

  public static final String UNKNOWN = "Bilinmeyen cihaz";

  private DeviceLabels() {
  }

  public static String fromUserAgent(String ua) {
    if (ua == null || ua.isBlank()) {
      return UNKNOWN;
    }
    String browser = browser(ua);
    String platform = platform(ua);
    if (browser == null || platform == null) {
      return UNKNOWN;
    }
    return browser + " · " + platform;
  }

  // Order matters: Edge and Chrome on iOS also contain "Chrome"/"Safari" tokens.
  private static String browser(String ua) {
    if (ua.contains("Edg/") || ua.contains("EdgiOS/") || ua.contains("EdgA/")) return "Edge";
    if (ua.contains("Firefox/") || ua.contains("FxiOS/")) return "Firefox";
    if (ua.contains("CriOS/") || ua.contains("Chrome/")) return "Chrome";
    if (ua.contains("Safari/")) return "Safari";
    return null;
  }

  // Order matters: iPhone/iPad user agents contain "like Mac OS X", Android contains "Linux".
  private static String platform(String ua) {
    if (ua.contains("iPhone")) return "iPhone";
    if (ua.contains("iPad")) return "iPad";
    if (ua.contains("Android")) return "Android";
    if (ua.contains("Windows")) return "Windows";
    if (ua.contains("Mac OS X") || ua.contains("Macintosh")) return "macOS";
    if (ua.contains("Linux")) return "Linux";
    return null;
  }
}
