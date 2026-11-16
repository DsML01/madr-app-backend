package com.madr.madr_app_backend.exception;

public class LivroNotFoundException extends RuntimeException {
    public LivroNotFoundException(Long id) {
        super("Livro com o id: " + id + "não encontrado!");
    }
}
