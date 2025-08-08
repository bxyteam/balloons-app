package com.browxy.balloons.exceptions;

public class NoSpaceLeftOnDeviceException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public NoSpaceLeftOnDeviceException(Throwable t1) {
      super(t1);
  }

}
