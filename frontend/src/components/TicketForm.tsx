import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'

const MAX_TITRE = 100
const MAX_DESCRIPTION = 2000

interface TicketFormProps {
  envoiEnCours: boolean
  erreur: string
  confirmation: string
  onEffacerErreur: () => void
  onEffacerConfirmation: () => void
  onCreer: (donnees: { titre: string; description: string | null }) => Promise<boolean>
  /** Ferme le formulaire et ramène à la liste. */
  onAnnuler: () => void
}

export default function TicketForm({
  envoiEnCours,
  erreur,
  confirmation,
  onEffacerErreur,
  onEffacerConfirmation,
  onCreer,
  onAnnuler,
}: TicketFormProps) {
  const [titre, setTitre] = useState('')
  const [description, setDescription] = useState('')

  // Validation frontend : complémentaire à celle du backend (400/409 gérés par l'appelant).
  const titreTronque = titre.trim()
  const erreurTitre =
    titre.length > MAX_TITRE
      ? `Le titre ne peut pas dépasser ${MAX_TITRE} caractères.`
      : null

  async function handleSubmit(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    if (erreurTitre || titreTronque === '' || envoiEnCours) return
    const succes = await onCreer({
      titre: titreTronque,
      description: description.trim() === '' ? null : description,
    })
    if (succes) {
      setTitre('')
      setDescription('')
    }
    // En cas d'erreur : les saisies sont conservées pour correction.
  }

  function handleChange(setter: (valeur: string) => void) {
    return (evenement: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setter(evenement.target.value)
      if (erreur) onEffacerErreur()
      if (confirmation) onEffacerConfirmation()
    }
  }

  return (
    <section className="carte" aria-labelledby="titre-formulaire">
      <div className="entete-carte">
        <div>
          <h2 id="titre-formulaire" className="entete-carte-titre">
            Créer un ticket
          </h2>
          <p className="entete-carte-sous-titre">
            Décrivez la demande en un titre clair, complétée d'une description si besoin.
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        <div className="champ-groupe">
          <label htmlFor="titre">
            Titre <span aria-hidden="true" style={{ color: 'var(--erreur-texte)' }}>*</span>
          </label>
          <input
            id="titre"
            type="text"
            required
            aria-required="true"
            aria-invalid={erreurTitre ? true : undefined}
            aria-describedby={erreurTitre ? 'erreur-titre' : 'compteur-titre'}
            maxLength={MAX_TITRE}
            value={titre}
            onChange={handleChange(setTitre)}
          />
          <span id="compteur-titre" className={`compteur${titre.length > MAX_TITRE ? ' compteur-depasse' : ''}`}>
            {titre.length}/{MAX_TITRE} caractères
          </span>
          {erreurTitre && (
            <p id="erreur-titre" className="erreur-champ">
              {erreurTitre}
            </p>
          )}
        </div>

        <div className="champ-groupe">
          <label htmlFor="description">Description (facultative)</label>
          <textarea
            id="description"
            rows={3}
            aria-describedby="compteur-description"
            maxLength={MAX_DESCRIPTION}
            value={description}
            onChange={handleChange(setDescription)}
          />
          <span id="compteur-description" className="compteur">
            {description.length}/{MAX_DESCRIPTION} caractères
          </span>
        </div>

        {erreur && (
          <div className="alerte alerte-erreur" role="alert">
            <p>{erreur}</p>
          </div>
        )}

        {confirmation && !erreur && (
          <div className="alerte alerte-succes" role="status">
            <p>{confirmation}</p>
          </div>
        )}

        <div className="boutons-formulaire">
          <button
            type="button"
            className="bouton bouton-secondaire"
            disabled={envoiEnCours}
            onClick={() => {
              if (erreur) onEffacerErreur()
              onAnnuler()
            }}
          >
            Annuler
          </button>
          <button
            type="submit"
            className="bouton bouton-primaire"
            disabled={titreTronque === '' || !!erreurTitre || envoiEnCours}
          >
            {envoiEnCours && <span className="spinner" aria-hidden="true" />}
            {envoiEnCours ? 'Création en cours…' : 'Créer le ticket'}
          </button>
        </div>
      </form>
    </section>
  )
}
