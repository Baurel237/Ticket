import { useState } from 'react'

const FILTRES = [
  { valeur: '', libelle: 'Tous' },
  { valeur: 'OUVERT', libelle: 'OUVERT' },
  { valeur: 'EN_COURS', libelle: 'EN_COURS' },
  { valeur: 'FERME', libelle: 'FERME' },
]

const formatDate = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'short' })

export default function TicketList({
  etat,
  tickets,
  filtre,
  erreurChargement,
  erreurFermeture,
  fermetureEnCours,
  onChangerFiltre,
  onReessayer,
  onFermer,
}) {
  const [confirmation, setConfirmation] = useState(null)

  function confirmerFermeture(id) {
    setConfirmation(null)
    onFermer(id)
  }

  return (
    <section className="carte">
      <div className="entete-liste">
        <h2>Tickets</h2>
        <div>
          <label htmlFor="filtre-statut">Statut </label>
          <select
            id="filtre-statut"
            value={filtre}
            onChange={(evenement) => onChangerFiltre(evenement.target.value)}
          >
            {FILTRES.map((f) => (
              <option key={f.valeur} value={f.valeur}>
                {f.libelle}
              </option>
            ))}
          </select>
        </div>
      </div>

      {erreurFermeture && (
        <p className="message message-erreur" role="alert">
          {erreurFermeture}
        </p>
      )}

      {etat === 'chargement' && (
        <p className="message" role="status">
          <span className="spinner" aria-hidden="true" /> Chargement des tickets…
        </p>
      )}

      {etat === 'erreur' && (
        <div className="message message-erreur" role="alert">
          <p>{erreurChargement}</p>
          <button type="button" onClick={onReessayer}>
            Réessayer
          </button>
        </div>
      )}

      {etat === 'vide' && <p className="message">Aucun ticket</p>}

      {etat === 'donnees' && (
        <table>
          <thead>
            <tr>
              <th>Titre</th>
              <th>Statut</th>
              <th>Date de création</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((ticket) => (
              <tr key={ticket.id}>
                <td>
                  {ticket.titre}
                  {ticket.description && <div className="description">{ticket.description}</div>}
                </td>
                <td>
                  <span className={`badge badge-${ticket.statut}`}>{ticket.statut}</span>
                </td>
                <td>{formatDate.format(new Date(ticket.dateCreation))}</td>
                <td>
                  {ticket.statut !== 'FERME' &&
                    (confirmation === ticket.id ? (
                      <span className="confirmation">
                        Fermer ce ticket ?{' '}
                        <button type="button" onClick={() => confirmerFermeture(ticket.id)}>
                          Oui
                        </button>{' '}
                        <button type="button" className="secondaire" onClick={() => setConfirmation(null)}>
                          Non
                        </button>
                      </span>
                    ) : (
                      <button
                        type="button"
                        disabled={fermetureEnCours === ticket.id}
                        onClick={() => setConfirmation(ticket.id)}
                      >
                        Fermer
                      </button>
                    ))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
