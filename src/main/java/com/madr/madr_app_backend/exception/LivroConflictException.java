package com.madr.madr_app_backend.exception;

public class LivroConflictException extends RuntimeException {
    public LivroConflictException(String titulo) {
        super("Já existe um livro com o título '" + titulo + "' cadastrado!");
    }
}
