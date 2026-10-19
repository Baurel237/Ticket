package com.kfokam.ticket.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

// Tests unitaires du gestionnaire : instanciation directe, aucune couche Spring.
class GestionnaireErreursTests {

    private final GestionnaireErreurs gestionnaire = new GestionnaireErreurs();

    // --- DataIntegrityViolationException ---

    @Test
    void violationIntegriteSurLeTitreRenvoie409() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.integrite(
                new DataIntegrityViolationException("Key (titre)=(Imprimante) already exists."));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().status()).isEqualTo(409);
        assertThat(reponse.getBody().erreur()).isEqualTo("Règle de gestion violée");
        assertThat(reponse.getBody().message())
                .isEqualTo("Ce titre est déjà utilisé : deux tickets ne peuvent pas avoir le même titre.");
        assertThat(reponse.getBody().horodatage()).isNotNull();
    }

    @Test
    void violationIntegriteIdentifieeParContrainteNommeRenvoie409() {
        // Variante base créée avec Flyway : le message cite la contrainte nommée,
        // pas la colonne entre parenthèses.
        ResponseEntity<ErreurApi> reponse = gestionnaire.integrite(
                new DataIntegrityViolationException(
                        "ERROR: duplicate key value violates unique constraint \"uk_titre\""));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(reponse.getBody().erreur()).isEqualTo("Règle de gestion violée");
    }

    @Test
    void violationIntegriteAutreQueLeTitreRenvoie500() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.integrite(
                new DataIntegrityViolationException(
                        "null value in column \"statut\" violates not-null constraint"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().status()).isEqualTo(500);
        assertThat(reponse.getBody().message())
                .isEqualTo("Un problème est survenu sur le serveur. Veuillez réessayer dans un instant.");
    }

    // --- Exception generic ---

    @Test
    void erreurHttpStandardConserveSonStatut() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.imprevue(
                new ExceptionHttp(HttpStatusCode.valueOf(405), "method"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().status()).isEqualTo(405);
        assertThat(reponse.getBody().erreur()).isEqualTo("Requête refusée");
        assertThat(reponse.getBody().message()).isEqualTo("Cette demande n'est pas possible.");
    }

    @Test
    void codeHttpExotiqueNeFaitPasEchouerLeGestionnaire() {
        // 599 n'existe pas dans l'enum HttpStatus : le gestionnaire ne doit pas
        // lever d'exception à son propre tour.
        ResponseEntity<ErreurApi> reponse = gestionnaire.imprevue(
                new ExceptionHttp(HttpStatusCode.valueOf(599), "exotique"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().status()).isEqualTo(500);
        // Le code inconnu est ramené à 500 mais reste identifié comme une requête refusée.
        assertThat(reponse.getBody().erreur()).isEqualTo("Requête refusée");
    }

    @Test
    void exceptionSimpleRenvoie500SansFuiteDeDetail() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.imprevue(
                new IllegalStateException("detail interne qui ne doit pas fuiter"));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().message())
                .isEqualTo("Un problème est survenu sur le serveur. Veuillez réessayer dans un instant.");
        assertThat(reponse.getBody().message()).doesNotContain("detail interne");
    }

    // --- Mappings métier ---

    @Test
    void ticketIntrouvableRenvoie404AvecLeMessageDeLException() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.introuvable(new TicketIntrouvableException(42L));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().message()).isEqualTo("Aucun ticket avec l'identifiant 42.");
    }

    @Test
    void regleDeGestionRenvoie409() {
        ResponseEntity<ErreurApi> reponse = gestionnaire.regleViolee(
                new RegleDeGestionException("Ce ticket est déjà fermé."));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().message()).isEqualTo("Ce ticket est déjà fermé.");
    }

    @Test
    void corpsIllisibleRenvoie400AvecMessageLisible() {
        HttpInputMessage corpsVide = new HttpInputMessage() {
            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public InputStream getBody() {
                return InputStream.nullInputStream();
            }
        };
        ResponseEntity<ErreurApi> reponse =
                gestionnaire.corpsIllisible(new HttpMessageNotReadableException("JSON cassé", corpsVide));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(reponse.getBody()).isNotNull();
        assertThat(reponse.getBody().message())
                .isEqualTo("Le corps de la requête est absent ou n'est pas un JSON valide.");
    }

    // --- Forme homogène ---

    @Test
    void toutesLesErreursOntLaMemeForme() {
        ErreurApi erreur = ErreurApi.of(409, "Règle de gestion violée", "message");

        assertThat(erreur.status()).isEqualTo(409);
        assertThat(erreur.erreur()).isEqualTo("Règle de gestion violée");
        assertThat(erreur.message()).isEqualTo("message");
        assertThat(erreur.horodatage()).isNotNull();
    }

    // --- Double de test : exception portant l'interface ErrorResponse ---

    private static class ExceptionHttp extends Exception implements ErrorResponse {
        private final HttpStatusCode code;

        ExceptionHttp(HttpStatusCode code, String message) {
            super(message);
            this.code = code;
        }

        @Override
        public HttpStatusCode getStatusCode() {
            return code;
        }

        @Override
        public ProblemDetail getBody() {
            return ProblemDetail.forStatusAndDetail(code, String.valueOf(getMessage()));
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }
    }
}
