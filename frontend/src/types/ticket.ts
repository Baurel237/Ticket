export type Statut = 'OUVERT' | 'EN_COURS' | 'FERME'

export type EtatListe = 'chargement' | 'donnees' | 'vide' | 'erreur'

export interface Ticket {
  id: number
  titre: string
  description: string | null
  statut: Statut
  dateCreation: string
}

export interface ErreurApi {
  status: number
  erreur: string
  message: string
  horodatage: string
}
