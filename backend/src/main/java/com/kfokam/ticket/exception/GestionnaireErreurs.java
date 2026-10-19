package com.kfokam.ticket.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GestionnaireErreurs {

    private static final Logger log = LoggerFactory.getLogger(GestionnaireErreurs.class);

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
        // Les valeurs acceptées d'un paramètre enum (statut) sont dérivées de l'enum
        // lui-même : aucune liste codée en dur dans le gestionnaire.
        if (e.getRequiredType() != null && e.getRequiredType().isEnum()) {
            String valeurs = Arrays.stream(e.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                    "Valeur invalide pour le paramètre « " + e.getName() + " ». Valeurs acceptées : " + valeurs + ".");
        }
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Valeur invalide pour le paramètre « " + e.getName() + " ».");
    }

    @ExceptionHandler(TicketIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(TicketIntrouvableException e) {
        log.info("Ticket introuvable : {}", e.getMessage());
        return reponse(HttpStatus.NOT_FOUND, "Ticket introuvable", e.getMessage());
    }

    @ExceptionHandler(RegleDeGestionException.class)
    public ResponseEntity<ErreurApi> regleViolee(RegleDeGestionException e) {
        log.warn("Règle de gestion violée : {}", e.getMessage());
        return reponse(HttpStatus.CONFLICT, "Règle de gestion violée", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        String details = (cause == null || cause.getMessage() == null) ? "" : cause.getMessage().toLowerCase();
        // 409 uniquement quand la contrainte violée concerne bien le titre (RG-02).
        // Détection en deux niveaux : le nom explicite de la contrainte (uk_titre,
        // posé par la migration Flyway) d'abord, puis la mention de la colonne
        // dans le message du driver, qui varie selon le SGBD mais reste le seul
        // indice disponible sur une base créée avant le nommage de la contrainte.
        if (details.contains("uk_titre") || details.contains("(titre")) {
            log.warn("Contrainte d'unicité du titre violée : {}", details);
            return reponse(HttpStatus.CONFLICT, "Règle de gestion violée",
                    "Ce titre est déjà utilisé : deux tickets ne peuvent pas avoir le même titre.");
        }
        log.error("Violation d'intégrité des données non reconnue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Problème technique",
                "Un problème est survenu sur le serveur. Veuillez réessayer dans un instant.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> imprevue(Exception e) {
        if (e instanceof ErrorResponse erreurHttp) {
            // resolve() au lieu de valueOf() : un code HTTP exotique (non standard)
            // ne doit pas faire échouer le gestionnaire lui-même.
            HttpStatus resolu = HttpStatus.resolve(erreurHttp.getStatusCode().value());
            HttpStatus status = (resolu != null) ? resolu : HttpStatus.INTERNAL_SERVER_ERROR;
            log.warn("Requête refusée : {} {} ({})", status.value(), e.getClass().getSimpleName(), e.getMessage());
            return reponse(status, "Requête refusée", "Cette demande n'est pas possible.");
        }
        log.error("Erreur non gérée", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Problème technique",
                "Un problème est survenu sur le serveur. Veuillez réessayer dans un instant.");
    }

    private ResponseEntity<ErreurApi> reponse(HttpStatus status, String erreur, String message) {
        return ResponseEntity.status(status).body(ErreurApi.of(status.value(), erreur, message));
    }
}
