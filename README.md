# Module Ticket — Application de suivi des tickets de support

API **Spring Boot + PostgreSQL** et écran **React + TypeScript**.

## Lancer le projet

### 1. Base de données (Docker)

```bash
# Le volume du conteneur final doit exister (une seule fois) :
docker volume create final_postgres_data

docker compose up -d
```

Le conteneur `tickets-postgres` (PostgreSQL 16) expose le port **5432** avec la base
`tickets_db` et le compte `postgres` / `postgres`. La table `tickets` est créée
automatiquement au démarrage du backend.

### 2. Backend (port 8085)

```bash
cd backend
mvnw spring-boot:run    # Windows : mvnw.cmd spring-boot:run
```

- Swagger : http://localhost:8085/swagger-ui/index.html
- Configuration externalisée par variables d'environnement avec valeurs par défaut :
  `DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`.

### 3. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Le serveur de développement Vite relaie `/api` vers le backend (voir `frontend/vite.config.ts`).

### 4. Vérifications

```bash
cd backend && mvnw test          # tests H2 en mémoire (aucun Docker requis)
cd frontend && npm run lint && npm run typecheck && npm run build
```

### 5. Retrouver un ticket en base

```bash
docker exec tickets-postgres psql -U postgres -d tickets_db \
  -c "SELECT id, titre, statut, date_creation FROM tickets ORDER BY id;"
```

## API

| Verbe | Chemin | Rôle | Succès | Erreurs |
|---|---|---|---|---|
| POST | `/api/tickets` | Créer un ticket (en-tête `Location`) | 201 | 400, 409 |
| GET | `/api/tickets?statut=` | Lister, filtrable par statut | 200 | 400 (statut inconnu) |
| PATCH | `/api/tickets/{id}/fermer` | Fermer un ticket | 200 | 404, 409 |

Règles de gestion : **RG-01** titre obligatoire (100 caractères max), statut initial OUVERT ·
**RG-02** deux tickets ne peuvent pas avoir le même titre · **RG-03** un ticket FERME ne change plus d'état.

Les erreurs ont toutes la forme `{ "status", "erreur", "message", "horodatage" }`.
Les dates sont sérialisées en ISO-8601 avec fuseau (`Instant`), correctement interprétées par le navigateur.

## Architecture

- `backend/` : `model` (entité) → `repository` → `service` (DTO en entrée/sortie) → `controller`,
  gestion des erreurs centralisée dans `exception/GestionnaireErreurs` (journalisée), configuration
  CORS et OpenAPI dans `config/`.
- `backend/src/test/` : tests H2 du service et des 3 endpoints (codes 200/201/400/404/409).
- `frontend/src/types/ticket.ts` : types partagés (`Ticket`, `Statut`).
- `frontend/src/services/ticketService.ts` : seul endroit où `fetch` est appelé.
- `frontend/src/components/` : `TicketsPage` (état et appels), `TicketList`, `TicketForm`, `PopupErreur`.
