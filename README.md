# Module Ticket — Application de suivi des tickets de support

API **Spring Boot + PostgreSQL** et écran **React + TypeScript**.

## Lancer le projet

### 1. Base de données (Docker)

```bash
docker compose up -d
```

Le volume `final_postgres_data` est créé automatiquement par Docker Compose et
conservé entre les recréations du conteneur. Le conteneur `tickets-postgres`
(PostgreSQL 16) expose le port **5432** avec la base `tickets_db` et le compte
`postgres` / `postgres`. Le schéma (`tickets`) est géré par **Flyway** ; si la
table existe déjà (base créée par une version antérieure), elle est marquée
comme baseline puis les migrations suivantes s'appliquent.

### 2. Backend (port 8085)

```bash
cd backend
mvnw spring-boot:run    # Windows : mvnw.cmd spring-boot:run
```

- Swagger : http://localhost:8085/swagger-ui/index.html
- Profil `prod` (Swagger/OpenAPI désactivés, utile en déploiement réel) :
  `mvnw spring-boot:run "-Dspring-boot.run.profiles=prod"`
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
cd backend && mvnw test          # 37 tests H2, aucun Docker requis
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
| GET | `/api/tickets?statut=&page=&taille=` | Lister, filtré par statut ; pagination optionnelle (`taille` max 500), triée du plus récent au plus ancien | 200 | 400 (statut inconnu) |
| PATCH | `/api/tickets/{id}/fermer` | Fermer un ticket | 200 | 404, 409 |

Règles de gestion : **RG-01** titre obligatoire (100 caractères max), description
facultative (2000 caractères max), statut initial OUVERT · **RG-02** deux tickets
ne peuvent pas avoir le même titre (égalité stricte, casse comprise) ·
**RG-03** un ticket FERME ne change plus d'état (garde atomique en SQL).

Les erreurs ont toutes la forme `{ "status", "erreur", "message", "horodatage" }`.
Les dates sont sérialisées en ISO-8601 avec fuseau (`Instant`), correctement interprétées par le navigateur.

## Architecture

- `backend/` : `model` (entité) → `repository` → `service` (DTO en entrée/sortie) → `controller`,
  gestion des erreurs centralisée dans `exception/GestionnaireErreurs` (journalisée),
  migrations Flyway dans `src/main/resources/db/migration`, configuration
  CORS et OpenAPI dans `config/`.
- `backend/src/test/` : tests H2 du service, de la pagination et des 3 endpoints
  (codes 200/201/400/404/409), tests unitaires du gestionnaire d'erreurs
  (contraintes 409/500, codes HTTP exotiques).
- `frontend/src/types/ticket.ts` : types partagés (`Ticket`, `Statut`).
- `frontend/src/services/ticketService.ts` : seul endroit où `fetch` est appelé.
- `frontend/src/components/` : `TicketsPage` (état, appels API et en-tête),
  `TicketList` (indicateurs, filtres, tableau/cartes), `TicketForm` (création),
  `Dialogue` (confirmation/erreur modale accessible), `Alerte`, `BadgeStatut`,
  `libelles`.

## Preuves visuelles

Captures dans [`docs/captures/`](docs/captures/) : `ui-vide` (liste vide), `ui-400` (validation),
`ui-409` (doublon à la création), `ui-409-fermeture` (double fermeture), `ui-erreur`
(popup en cas d'échec réseau) et `swagger` (contrat OpenAPI).
