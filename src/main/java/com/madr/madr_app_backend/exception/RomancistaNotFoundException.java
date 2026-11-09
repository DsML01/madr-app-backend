package com.madr.madr_app_backend.exception;

public class RomancistaNotFoundException extends RuntimeException {

    public RomancistaNotFoundException(Long id)
    {
        super("Romancista com id " + id + " não encotnrado no MADR");
    }
}
