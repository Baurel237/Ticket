import { useCallback, useEffect, useRef, useState } from 'react'
import { creerTicket, fermerTicket, listerTickets } from '../services/ticketService.js'
import TicketForm from './TicketForm.jsx'
import TicketList from './TicketList.jsx'

export default function TicketsPage() {
  const [tickets, setTickets] = useState([])
  const [filtre, setFiltre] = useState('')
  const [etat, setEtat] = useState('chargement')
  const [erreurChargement, setErreurChargement] = useState('')
  const [erreurFormulaire, setErreurFormulaire] = useState('')
  const [erreurFermeture, setErreurFermeture] = useState('')
  const [envoiEnCours, setEnvoiEnCours] = useState(false)
  const [fermetureEnCours, setFermetureEnCours] = useState(null)
  const derniereRequete = useRef(0)

  const charger = useCallback(async (statut, { silencieux = false } = {}) => {
    const numero = ++derniereRequete.current
    if (!silencieux) {
      setEtat('chargement')
    }
    try {
      const donnees = await listerTickets(statut)
      if (numero !== derniereRequete.current) return
      setTickets(donnees)
      setErreurChargement('')
      setEtat(donnees.length === 0 ? 'vide' : 'donnees')
    } catch (erreur) {
      if (numero !== derniereRequete.current) return
      setErreurChargement(erreur.message)
      setEtat('erreur')
    }
  }, [])

  useEffect(() => {
    charger(filtre)
  }, [charger, filtre])

  async function handleCreation(ticket) {
    setEnvoiEnCours(true)
    setErreurFormulaire('')
    try {
      await creerTicket(ticket)
      await charger(filtre, { silencieux: true })
      return true
    } catch (erreur) {
      setErreurFormulaire(erreur.message)
      return false
    } finally {
      setEnvoiEnCours(false)
    }
  }

  async function handleFermeture(id) {
    setFermetureEnCours(id)
    setErreurFermeture('')
    try {
      await fermerTicket(id)
      await charger(filtre, { silencieux: true })
    } catch (erreur) {
      setErreurFermeture(erreur.message)
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
        onEffacerErreur={() => setErreurFormulaire('')}
        onCreer={handleCreation}
      />

      <TicketList
        etat={etat}
        tickets={tickets}
        filtre={filtre}
        erreurChargement={erreurChargement}
        erreurFermeture={erreurFermeture}
        fermetureEnCours={fermetureEnCours}
        onChangerFiltre={setFiltre}
        onReessayer={() => charger(filtre)}
        onFermer={handleFermeture}
      />
    </main>
  )
}
