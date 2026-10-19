import { LIBELLES_STATUT } from './libelles'
import type { Statut } from '../types/ticket'

interface BadgeStatutProps {
  statut: Statut
}

/** Badge coloré + libellé textuel : jamais la couleur seule pour porter l'information. */
export default function BadgeStatut({ statut }: BadgeStatutProps) {
  return <span className={`badge badge-${statut}`}>{LIBELLES_STATUT[statut]}</span>
}
