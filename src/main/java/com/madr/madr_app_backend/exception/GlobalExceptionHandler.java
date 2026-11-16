package com.madr.madr_app_backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(RomancistaNotFoundException.class)
    public ProblemDetail handleNotFound(RomancistaNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Romancista não encontrado");
        problem.setType(URI.create("https://madr.com/errors/not-found"));
        return problem;
    }

    @ExceptionHandler(RomancistaConflictException.class)
    public ProblemDetail handleConflict(RomancistaConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflito de dados");
        problem.setType(URI.create("https://madr.com/errors/conflict"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos");
        problem.setTitle("Erro de validação");
        problem.setType(URI.create("https://madr.com/errors/bad-request"));

        Map<String, Set<String>> invalidFields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        TreeMap::new,
                        Collectors.mapping(
                                error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Valor inválido"),
                                Collectors.toCollection(TreeSet::new)
                        )
                ));
        problem.setProperty("invalidFields", invalidFields);

        return problem;
    }

    @ExceptionHandler(LivroNotFoundException.class)
    public ProblemDetail handleLivroNotFound(LivroNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Livro não encontrado");
        problem.setType(URI.create("https://madr.com/errors/not-found"));
        return problem;
    }

    @ExceptionHandler(LivroConflictException.class)
    public ProblemDetail handleLivroConflict(LivroConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflito de dados");
        problem.setType(URI.create("https://madr.com/errors/conflict"));

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Operação não permitida pois viola restrição de integridade no banco de dados"
        );
        problem.setTitle("Conflito de dados");
        problem.setType(URI.create("https://madr.com/errors/conflict"));
        return problem;
    }
}
