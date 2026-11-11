package com.hakandincturk.security.session;

/** What the login request tells us about the device, stored with the session for the session list. */
public record ClientInfo(String userAgent, String ipAddress, String country) {
}
