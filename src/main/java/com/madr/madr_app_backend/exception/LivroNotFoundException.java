package com.madr.madr_app_backend.exception;

public class LivroNotFoundException extends RuntimeException {
  public LivroNotFoundException(String message) {
    super(message);
  }
}
