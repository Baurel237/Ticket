# Frontend — Gestion des tickets (React + TypeScript + Vite)

Écran de gestion des tickets : liste filtrable par statut, création avec affichage des
erreurs serveur, fermeture avec confirmation.

## Commandes

```bash
npm install
npm run dev        # serveur de développement (port 5173, proxy /api -> localhost:8085)
npm run lint       # Oxlint
npm run typecheck  # TypeScript strict, sans émission
npm run build      # typecheck + build de production dans dist/
```

## Structure

- `src/types/ticket.ts` — types partagés (`Ticket`, `Statut`, états de liste, `ErreurApi`)
- `src/services/ticketService.ts` — appel de l'API (seul fichier utilisant `fetch`)
- `src/components/TicketsPage.tsx` — état de l'écran et orchestration
- `src/components/TicketList.tsx` — liste, filtre par statut, confirmation de fermeture
- `src/components/TicketForm.tsx` — formulaire de création (titre limité à 100 caractères)
- `src/components/PopupErreur.tsx` — message d'erreur accessible (Échap pour fermer)
