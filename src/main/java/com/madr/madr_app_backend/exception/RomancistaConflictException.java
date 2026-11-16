package com.madr.madr_app_backend.exception;

public class RomancistaConflictException extends RuntimeException {

    public RomancistaConflictException(String nome)
    {
        super("Já existe um(a) romancista cadastrado(a) com o nome: '" + nome + "'");
    }
}
