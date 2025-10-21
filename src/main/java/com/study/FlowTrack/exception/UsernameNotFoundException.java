package com.study.FlowTrack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.naming.AuthenticationException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsernameNotFoundException extends AuthenticationException {
  public UsernameNotFoundException(String message) {
    super(message);
  }
}
