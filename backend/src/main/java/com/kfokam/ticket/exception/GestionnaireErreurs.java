package com.kfokam.ticket.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GestionnaireErreurs {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(erreur -> erreur.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> corpsIllisible(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Le corps de la requête est absent ou n'est pas un JSON valide.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApi> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Valeur invalide pour le paramètre « " + e.getName()
                        + " ». Valeurs acceptées pour le statut : OUVERT, EN_COURS, FERME.");
    }

    @ExceptionHandler(TicketIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(TicketIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, "Ticket introuvable", e.getMessage());
    }

    @ExceptionHandler(RegleDeGestionException.class)
    public ResponseEntity<ErreurApi> regleViolee(RegleDeGestionException e) {
        return reponse(HttpStatus.CONFLICT, "Règle de gestion violée", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        return reponse(HttpStatus.CONFLICT, "Règle de gestion violée",
                "Ce titre est déjà utilisé : deux tickets ne peuvent pas avoir le même titre. (RG-02)");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> imprevue(Exception e) {
        if (e instanceof ErrorResponse erreurHttp) {
            HttpStatus status = HttpStatus.valueOf(erreurHttp.getStatusCode().value());
            return reponse(status, "Requête refusée", "Cette demande n'est pas possible.");
        }
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Problème technique",
                "Un problème est survenu sur le serveur. Veuillez réessayer dans un instant.");
    }

    private ResponseEntity<ErreurApi> reponse(HttpStatus status, String erreur, String message) {
        return ResponseEntity.status(status).body(ErreurApi.of(status.value(), erreur, message));
    }
}
