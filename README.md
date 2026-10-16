# Module Ticket — Rattrapage KFOKAM48 (Batch 2, Phase 3)

Application de suivi des tickets de support : API **Spring Boot + PostgreSQL** et écran **React**.

## Lancer le projet

### 1. Base de données (Docker)

```bash
docker run -d --name ticket-postgres \
  -e POSTGRES_USER=ticket -e POSTGRES_PASSWORD=ticket -e POSTGRES_DB=ticketdb \
  -p 5433:5432 postgres:16
```

Le port 5433 évite un conflit avec un PostgreSQL local sur 5432.

### 2. Backend (port 8085)

```bash
cd backend
mvn spring-boot:run
```

- Swagger : http://localhost:8085/swagger-ui/index.html
- La table `tickets` est créée automatiquement au démarrage.

### 3. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Le serveur de développement relaie `/api` vers le backend (voir `frontend/vite.config.js`).

### 4. Retrouver un ticket en base

```bash
docker exec ticket-postgres psql -U ticket -d ticketdb \
  -c "SELECT id, titre, statut, date_creation FROM tickets ORDER BY id;"
```

## API

| Verbe | Chemin | Rôle | Succès | Erreurs |
|---|---|---|---|---|
| POST | `/api/tickets` | Créer un ticket | 201 | 400, 409 |
| GET | `/api/tickets?statut=` | Lister, filtrable par statut | 200 | 400 (statut inconnu) |
| PATCH | `/api/tickets/{id}/fermer` | Fermer un ticket | 200 | 404, 409 |

Règles de gestion : **RG-01** titre obligatoire (100 caractères max), statut initial OUVERT ·
**RG-02** deux tickets ne peuvent pas avoir le même titre · **RG-03** un ticket FERME ne change plus d'état.

Les erreurs ont toutes la forme `{ "status", "erreur", "message", "horodatage" }`.

## Architecture

- `backend/` : `model` (entité) → `repository` → `service` (DTO en entrée/sortie) → `controller`, gestion des erreurs centralisée dans `exception/GestionnaireErreurs`.
- `frontend/src/services/ticketService.js` : seul endroit où `fetch` est appelé.
- `frontend/src/components/` : `TicketsPage` (état et appels), `TicketList`, `TicketForm`.
