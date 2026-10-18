import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'

interface TicketFormProps {
  envoiEnCours: boolean
  erreur: string
  confirmation: string
  onEffacerErreur: () => void
  onEffacerConfirmation: () => void
  onCreer: (donnees: { titre: string; description: string | null }) => Promise<boolean>
}

export default function TicketForm({
  envoiEnCours,
  erreur,
  confirmation,
  onEffacerErreur,
  onEffacerConfirmation,
  onCreer,
}: TicketFormProps) {
  const [titre, setTitre] = useState('')
  const [description, setDescription] = useState('')

  async function handleSubmit(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    const succes = await onCreer({
      titre,
      description: description.trim() === '' ? null : description,
    })
    if (succes) {
      setTitre('')
      setDescription('')
    }
  }

  function handleChange(setter: (valeur: string) => void) {
    return (evenement: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setter(evenement.target.value)
      if (erreur) onEffacerErreur()
      if (confirmation) onEffacerConfirmation()
    }
  }

  return (
    <section className="carte">
      <h2>Nouveau ticket</h2>
      <form onSubmit={handleSubmit} noValidate>
        <label htmlFor="titre">Titre</label>
        <input
          id="titre"
          type="text"
          maxLength={100}
          value={titre}
          onChange={handleChange(setTitre)}
        />

        <label htmlFor="description">Description (facultative)</label>
        <textarea
          id="description"
          rows={3}
          maxLength={2000}
          value={description}
          onChange={handleChange(setDescription)}
        />

        {erreur && (
          <p className="message message-erreur" role="alert">
            {erreur}
          </p>
        )}

        {confirmation && !erreur && (
          <p className="message message-succes" role="status">
            {confirmation}
          </p>
        )}

        <button type="submit" disabled={titre.trim() === '' || envoiEnCours}>
          {envoiEnCours ? 'Création…' : 'Créer le ticket'}
        </button>
      </form>
    </section>
  )
}
