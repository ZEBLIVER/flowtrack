package com.study.FlowTrack.exception;

import javax.naming.AuthenticationException;

public class UsernameNotFoundException extends AuthenticationException {
  public UsernameNotFoundException(String message) {
    super(message);
  }
}
