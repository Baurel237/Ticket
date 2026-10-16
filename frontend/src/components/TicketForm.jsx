import { useState } from 'react'

export default function TicketForm({ envoiEnCours, erreur, onEffacerErreur, onCreer }) {
  const [titre, setTitre] = useState('')
  const [description, setDescription] = useState('')

  async function handleSubmit(evenement) {
    evenement.preventDefault()
    const succes = await onCreer({ titre, description: description.trim() === '' ? null : description })
    if (succes) {
      setTitre('')
      setDescription('')
    }
  }

  function handleChange(setter) {
    return (evenement) => {
      setter(evenement.target.value)
      if (erreur) onEffacerErreur()
    }
  }

  return (
    <section className="carte">
      <h2>Nouveau ticket</h2>
      <form onSubmit={handleSubmit} noValidate>
        <label htmlFor="titre">Titre</label>
        <input id="titre" type="text" value={titre} onChange={handleChange(setTitre)} />

        <label htmlFor="description">Description (facultative)</label>
        <textarea
          id="description"
          rows="3"
          value={description}
          onChange={handleChange(setDescription)}
        />

        {erreur && (
          <p className="message message-erreur" role="alert">
            {erreur}
          </p>
        )}

        <button type="submit" disabled={titre.trim() === '' || envoiEnCours}>
          {envoiEnCours ? 'Création…' : 'Créer le ticket'}
        </button>
      </form>
    </section>
  )
}
