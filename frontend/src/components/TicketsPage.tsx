import { useCallback, useEffect, useRef, useState } from 'react'
import { creerTicket, fermerTicket, listerTickets } from '../services/ticketService'
import TicketForm from './TicketForm'
import PopupErreur from './PopupErreur'
import TicketList from './TicketList'
import type { EtatListe, Statut, Ticket } from '../types/ticket'

interface PopupInfo {
  titre: string
  message: string
}

export default function TicketsPage() {
  const [tickets, setTickets] = useState<Ticket[]>([])
  const [filtre, setFiltre] = useState<Statut | ''>('')
  const [etat, setEtat] = useState<EtatListe>('chargement')
  const [erreurChargement, setErreurChargement] = useState('')
  const [erreurFormulaire, setErreurFormulaire] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [popup, setPopup] = useState<PopupInfo | null>(null)
  const [envoiEnCours, setEnvoiEnCours] = useState(false)
  const [fermetureEnCours, setFermetureEnCours] = useState<number | null>(null)
  const derniereRequete = useRef(0)

  // L'état « chargement » est posé par l'utilisateur (filtre, réessai) ou par
  // l'état initial — jamais directement depuis un effet.
  const charger = useCallback(async (statut: Statut | '') => {
    const numero = ++derniereRequete.current
    try {
      const donnees = await listerTickets(statut)
      if (numero !== derniereRequete.current) return
      setTickets(donnees)
      setErreurChargement('')
      setEtat(donnees.length === 0 ? 'vide' : 'donnees')
    } catch (erreur) {
      if (numero !== derniereRequete.current) return
      setErreurChargement(erreur instanceof Error ? erreur.message : 'Erreur inattendue.')
      setEtat('erreur')
    }
  }, [])

  useEffect(() => {
    // Chargement de données à l'affichage / changement de filtre : patron React
    // standard, les setState ne sont exécutés qu'après le await (hors corps de l'effet).
    // eslint-disable-next-line react/set-state-in-effect
    charger(filtre)
  }, [charger, filtre])

  function changerFiltre(nouveau: Statut | '') {
    setEtat('chargement')
    setFiltre(nouveau)
  }

  function reessayer() {
    setEtat('chargement')
    charger(filtre)
  }

  async function handleCreation(donnees: {
    titre: string
    description: string | null
  }): Promise<boolean> {
    setEnvoiEnCours(true)
    setErreurFormulaire('')
    setConfirmation('')
    try {
      await creerTicket(donnees)
      await charger(filtre)
      // Le ticket créé peut être hors du filtre actif : on confirme explicitement.
      setConfirmation('Ticket créé.')
      return true
    } catch (erreur) {
      setErreurFormulaire(erreur instanceof Error ? erreur.message : 'Erreur inattendue.')
      return false
    } finally {
      setEnvoiEnCours(false)
    }
  }

  async function handleFermeture(id: number) {
    setFermetureEnCours(id)
    try {
      await fermerTicket(id)
      await charger(filtre)
    } catch (erreur) {
      setPopup({
        titre: 'Fermeture impossible',
        message: erreur instanceof Error ? erreur.message : 'Erreur inattendue.',
      })
    } finally {
      setFermetureEnCours(null)
    }
  }

  return (
    <main className="page">
      <header>
        <h1>Gestion des tickets</h1>
        <p className="sous-titre">Suivi des demandes du service de support</p>
      </header>

      <TicketForm
        envoiEnCours={envoiEnCours}
        erreur={erreurFormulaire}
        confirmation={confirmation}
        onEffacerErreur={() => setErreurFormulaire('')}
        onEffacerConfirmation={() => setConfirmation('')}
        onCreer={handleCreation}
      />

      <TicketList
        etat={etat}
        tickets={tickets}
        filtre={filtre}
        erreurChargement={erreurChargement}
        fermetureEnCours={fermetureEnCours}
        onChangerFiltre={changerFiltre}
        onReessayer={reessayer}
        onFermer={handleFermeture}
      />

      {popup && (
        <PopupErreur titre={popup.titre} message={popup.message} onFermer={() => setPopup(null)} />
      )}
    </main>
  )
}
