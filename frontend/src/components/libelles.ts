import type { Statut } from '../types/ticket'

// Libellés lisibles : le code backend (OUVERT, EN_COURS…) reste technique,
// l'interface affiche du français correctement accentué.
export const LIBELLES_STATUT: Record<Statut, string> = {
  OUVERT: 'Ouvert',
  EN_COURS: 'En cours',
  FERME: 'Fermé',
}
