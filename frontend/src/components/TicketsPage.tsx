import { useCallback, useEffect, useRef, useState } from 'react'
import { creerTicket, fermerTicket, listerTickets } from '../services/ticketService'
import TicketForm from './TicketForm'
import Dialogue from './Dialogue'
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
  // Formulaire affiché seulement à la demande ; l'état vide y envoie l'utilisateur.
  const [formulaireOuvert, setFormulaireOuvert] = useState(false)
  const [ticketADefermer, setTicketADefermer] = useState<Ticket | null>(null)
  const derniereRequete = useRef(0)

  // Une seule requête charge TOUTE la liste : les filtres et les indicateurs
  // sont calculés côté client sur des données réelles et toujours cohérentes.
  const charger = useCallback(async () => {
    const numero = ++derniereRequete.current
    try {
      const donnees = await listerTickets()
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
    // Chargement des données à l'affichage : patron React standard. Le lint
    // signale l'appel de charger() depuis un effet, mais les setState qu'il
    // effectue sont tous after le await : jamais dans le corps synchrone de l'effet.
    // oxlint-disable-next-line react/set-state-in-effect
    charger()
  }, [charger])

  function changerFiltre(nouveau: Statut | '') {
    setFiltre(nouveau)
  }

  function ouvrirFormulaire() {
    setFormulaireOuvert(true)
  }

  function reessayer() {
    setEtat('chargement')
    charger()
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
      await charger()
      // Le ticket créé peut être hors du filtre actif : on confirme explicitement.
      setConfirmation(`Le ticket « ${donnees.titre} » a été créé.`)
      return true
    } catch (erreur) {
      setErreurFormulaire(erreur instanceof Error ? erreur.message : 'Erreur inattendue.')
      return false
    } finally {
      setEnvoiEnCours(false)
    }
  }

  function demanderFermeture(ticket: Ticket) {
    setTicketADefermer(ticket)
  }

  async function confirmerFermeture() {
    if (!ticketADefermer) return
    const id = ticketADefermer.id
    setFermetureEnCours(id)
    try {
      await fermerTicket(id)
      await charger()
      setConfirmation(`Le ticket « ${ticketADefermer.titre} » a été fermé.`)
      setTicketADefermer(null)
    } catch (erreur) {
      setTicketADefermer(null)
      setPopup({
        titre: 'Fermeture impossible',
        message: erreur instanceof Error ? erreur.message : 'Erreur inattendue.',
      })
    } finally {
      setFermetureEnCours(null)
    }
  }

  function annulerFermeture() {
    if (fermetureEnCours === null) setTicketADefermer(null)
  }

  return (
    <main className="page">
      <header className="en-tete-app">
        <div className="marque">
          <span className="marque-icone" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2Z" />
              <path d="M8 9h8" />
              <path d="M8 13h5" />
            </svg>
          </span>
          <div>
            <p className="marque-titre">Gestion des tickets</p>
            <p className="marque-slogan">Suivi des demandes du service de support</p>
          </div>
        </div>
        {formulaireOuvert ? (
          <button
            type="button"
            className="bouton bouton-secondaire"
            onClick={() => setFormulaireOuvert(false)}
          >
            Masquer le formulaire
          </button>
        ) : (
          <button type="button" className="bouton bouton-primaire" onClick={ouvrirFormulaire}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <path d="M12 5v14M5 12h14" />
            </svg>
            Nouveau ticket
          </button>
        )}
      </header>

      <div className="entete-carte" style={{ padding: '0 0 16px' }}>
        <div>
          <h1 className="titre-page">Tickets</h1>
          <p className="sous-titre">Créez, suivez et clôturez les demandes de support.</p>
        </div>
      </div>

      <TicketList
        etat={etat}
        tickets={tickets}
        filtre={filtre}
        erreurChargement={erreurChargement}
        fermetureEnCours={fermetureEnCours}
        onChangerFiltre={changerFiltre}
        onReessayer={reessayer}
        onFermer={demanderFermeture}
        onOuvrirFormulaire={ouvrirFormulaire}
        formulaireOuvert={formulaireOuvert}
      >
        <TicketForm
          envoiEnCours={envoiEnCours}
          erreur={erreurFormulaire}
          confirmation={confirmation}
          onEffacerErreur={() => setErreurFormulaire('')}
          onEffacerConfirmation={() => setConfirmation('')}
          onCreer={handleCreation}
          onAnnuler={() => setFormulaireOuvert(false)}
        />
      </TicketList>

      {ticketADefermer && (
        <Dialogue
          variante="alerte"
          titre="Fermer ce ticket ?"
          contenu={
            <>
              Vous êtes sur le point de fermer le ticket{' '}
              <b>« {ticketADefermer.titre} »</b>. Cette action ne pourra pas être annulée.
            </>
          }
          boutons={[
            { libelle: 'Annuler', variante: 'secondaire', onClick: annulerFermeture, disabled: fermetureEnCours !== null },
            {
              libelle: 'Confirmer la fermeture',
              variante: 'dangereux',
              onClick: confirmerFermeture,
              disabled: fermetureEnCours !== null,
              chargement: fermetureEnCours !== null,
            },
          ]}
          onFermer={annulerFermeture}
        />
      )}

      {popup && (
        <Dialogue
          titre={popup.titre}
          contenu={popup.message}
          variante="alerte"
          boutons={[{ libelle: "J'ai compris", variante: 'primaire', onClick: () => setPopup(null) }]}
          onFermer={() => setPopup(null)}
        />
      )}
    </main>
  )
}
