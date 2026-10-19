import { useMemo } from 'react'
import { LIBELLES_STATUT } from './libelles'
import BadgeStatut from './BadgeStatut'
import type { ReactNode } from 'react'
import type { EtatListe, Statut, Ticket } from '../types/ticket'

const formatDate = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'short' })

const STATUTS: Statut[] = ['OUVERT', 'EN_COURS', 'FERME']

interface TicketListProps {
  etat: EtatListe
  tickets: Ticket[]
  filtre: Statut | ''
  erreurChargement: string
  fermetureEnCours: number | null
  formulaireOuvert: boolean
  children?: ReactNode
  onChangerFiltre: (filtre: Statut | '') => void
  onReessayer: () => void
  onFermer: (ticket: Ticket) => void
  onOuvrirFormulaire: () => void
}

/** Tronque proprement les descriptions longues ; affichage côté liste. */
function tronquer(texte: string, max = 120): string {
  return texte.length > max ? `${texte.slice(0, max).trimEnd()}…` : texte
}

function skeletons(nombre: number): ReactNode[] {
  return Array.from({ length: nombre }, (_, index) => (
    <div key={index} className="skeleton-ligne">
      <div style={{ flexGrow: 1 }}>
        <div className="skeleton skeleton-champ" style={{ width: '45%' }} />
        <div className="skeleton skeleton-champ" style={{ width: '70%', margin: 0 }} />
      </div>
      <div className="skeleton skeleton-champ" style={{ width: 90, margin: 0 }} />
      <div className="skeleton skeleton-champ" style={{ width: 120, margin: 0 }} />
    </div>
  ))
}

export default function TicketList({
  etat,
  tickets,
  filtre,
  erreurChargement,
  fermetureEnCours,
  formulaireOuvert,
  children,
  onChangerFiltre,
  onReessayer,
  onFermer,
  onOuvrirFormulaire,
}: TicketListProps) {
  // Indicateurs et compteurs de filtre : calculés sur les données réelles, jamais fictifs.
  const mesures = useMemo(() => {
    const compte = { OUVERT: 0, EN_COURS: 0, FERME: 0 } as Record<Statut, number>
    for (const t of tickets) compte[t.statut] += 1
    return compte
  }, [tickets])

  const cible = useMemo(
    () => tickets.filter((t) => (filtre === '' ? true : t.statut === filtre)),
    [tickets, filtre],
  )

  const listeVideFiltree = etat === 'donnees' && cible.length === 0

  function changerFiltreDepuisSelectOuChip(valeur: string) {
    if (valeur === '') onChangerFiltre('')
    else if (STATUTS.includes(valeur as Statut)) onChangerFiltre(valeur as Statut)
  }

  return (
    <>
      <section aria-label="Vue d'ensemble" className="grille-mesures">
        <div className="mesure">
          <p className="mesure-valeur">{tickets.length}</p>
          <p className="mesure-libelle">Tickets au total</p>
        </div>
        <div className="mesure mesure-OUVERT">
          <p className="mesure-valeur">{mesures.OUVERT}</p>
          <p className="mesure-libelle">{LIBELLES_STATUT.OUVERT}s</p>
        </div>
        <div className="mesure mesure-EN_COURS">
          <p className="mesure-valeur">{mesures.EN_COURS}</p>
          <p className="mesure-libelle">{LIBELLES_STATUT.EN_COURS}</p>
        </div>
        <div className="mesure mesure-FERME">
          <p className="mesure-valeur">{mesures.FERME}</p>
          <p className="mesure-libelle">{LIBELLES_STATUT.FERME}s</p>
        </div>
      </section>

      {formulaireOuvert && children}

      <section className="carte" aria-labelledby="titre-liste">
        <div className="entete-carte">
          <div>
            <h2 id="titre-liste" className="entete-carte-titre">
              Liste des tickets
            </h2>
            <p className="entete-carte-sous-titre">
              {filtre === '' ? 'Tous les statuts' : `Statut : ${LIBELLES_STATUT[filtre]}`} ·{' '}
              {cible.length} ticket{cible.length > 1 ? 's' : ''}
            </p>
          </div>
          <div className="filtres" role="group" aria-label="Filtrer par statut">
            {([['', 'Tous'], ...STATUTS.map((s) => [s, LIBELLES_STATUT[s]] as [Statut, string])] as [
              Statut | '',
              string,
            ][]).map(([valeur, libelle]) => (
              <button
                key={valeur || 'tous'}
                type="button"
                className={`filtre${filtre === valeur ? ' filtre-actif' : ''}`}
                aria-pressed={filtre === valeur}
                onClick={() => changerFiltreDepuisSelectOuChip(valeur)}
              >
                {libelle}
                {valeur !== '' && (
                  <span className="filtre-nombre">{mesures[valeur as Statut]}</span>
                )}
              </button>
            ))}
          </div>
        </div>

        {etat === 'chargement' && (
          <div role="status" aria-label="Chargement des tickets">
            {skeletons(4)}
          </div>
        )}

        {etat === 'erreur' && (
          <div className="alerte alerte-erreur" role="alert">
            <p>{erreurChargement}</p>
            <button type="button" className="bouton bouton-discret" onClick={onReessayer}>
              Réessayer
            </button>
          </div>
        )}

        {etat === 'vide' && (
          <div className="etat-vide">
            <span className="etat-vide-icone" aria-hidden="true">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 7a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2Z" />
                <path d="M8 12h8" />
                <path d="M12 8v8" />
              </svg>
            </span>
            <h3>Aucun ticket pour le moment</h3>
            <p>Créez votre premier ticket pour commencer à suivre les demandes de support.</p>
            <button type="button" className="bouton bouton-primaire" onClick={onOuvrirFormulaire}>
              Créer le premier ticket
            </button>
          </div>
        )}

        {listeVideFiltree && (
          <div className="etat-vide">
            <h3>Aucun ticket avec ce filtre</h3>
            <p>Aucun ticket ne correspond au statut sélectionné. Essayez un autre filtre.</p>
            <button type="button" className="bouton bouton-secondaire" onClick={() => onChangerFiltre('')}>
              Voir tous les tickets
            </button>
          </div>
        )}

        {etat === 'donnees' && !listeVideFiltree && (
          <>
            {/* Desktop / tablette : tableau lisible (caché sous 720px) */}
            <div className="tableau-tickets tableau-enveloppe">
              <table>
                <thead>
                  <tr>
                    <th scope="col">Ticket</th>
                    <th scope="col">Statut</th>
                    <th scope="col">Date de création</th>
                    <th scope="col">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {cible.map((ticket) => (
                    <tr key={ticket.id}>
                      <td>
                        <span className="ticket-id">#{ticket.id}</span>
                        <div className="ticket-titre">{ticket.titre}</div>
                        {ticket.description && (
                          <div className="ticket-description">
                            {tronquer(ticket.description)}
                          </div>
                        )}
                      </td>
                      <td>
                        <BadgeStatut statut={ticket.statut} />
                      </td>
                      <td className="ticket-date">{formatDate.format(new Date(ticket.dateCreation))}</td>
                      <td>
                        {ticket.statut !== 'FERME' && (
                          <button
                            type="button"
                            className="bouton bouton-discret"
                            disabled={fermetureEnCours === ticket.id}
                            onClick={() => onFermer(ticket)}
                          >
                            Fermer
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile : cartes équivalentes, aucune action masquée */}
            <div className="cartes-tickets">
              {cible.map((ticket) => (
                <article key={ticket.id} className="carte-ticket">
                  <div className="carte-ticket-descriptions">
                    <div>
                      <span className="ticket-id">#{ticket.id}</span>
                      <div className="ticket-titre">{ticket.titre}</div>
                    </div>
                    <BadgeStatut statut={ticket.statut} />
                  </div>
                  {ticket.description && (
                    <p className="ticket-description">{tronquer(ticket.description, 160)}</p>
                  )}
                  <div className="carte-ticket-bas">
                    <span className="ticket-date">
                      {formatDate.format(new Date(ticket.dateCreation))}
                    </span>
                    {ticket.statut !== 'FERME' && (
                      <button
                        type="button"
                        className="bouton bouton-discret"
                        disabled={fermetureEnCours === ticket.id}
                        onClick={() => onFermer(ticket)}
                      >
                        Fermer
                      </button>
                    )}
                  </div>
                </article>
              ))}
            </div>
          </>
        )}
      </section>
    </>
  )
}
