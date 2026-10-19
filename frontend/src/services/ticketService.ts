import type { ErreurApi, Statut, Ticket } from '../types/ticket'

const BASE_URL = '/api/tickets'

export class ApiError extends Error {
  readonly status: number | null
  readonly injoignable: boolean

  constructor(message: string, options: { status?: number | null; injoignable?: boolean } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = options.status ?? null
    this.injoignable = options.injoignable ?? false
  }
}

const MESSAGE_INJOIGNABLE =
  "Le serveur est injoignable. Vérifiez qu'il est démarré, puis réessayez."

// Phrases lisibles quand le serveur ne fournit pas de message : jamais de code technique.
function messagePourStatut(status: number): string {
  switch (status) {
    case 400:
      return 'Les informations saisies ne sont pas valides. Vérifiez-les puis réessayez.'
    case 401:
    case 403:
      return "Cette action n'a pas pu être effectuée. Rechargez la page puis réessayez."
    case 404:
      return "L'élément demandé est introuvable. Il a peut-être été supprimé."
    case 409:
      return "Cette action est impossible dans l'état actuel des données."
    default:
      return 'Un problème est survenu. Veuillez réessayer dans un instant.'
  }
}

async function appeler<T>(url: string, options?: RequestInit): Promise<T> {
  let reponse: Response
  try {
    reponse = await fetch(url, options)
  } catch {
    throw new ApiError(MESSAGE_INJOIGNABLE, { injoignable: true })
  }

  if (reponse.ok) {
    return (await reponse.json()) as T
  }

  let corps: Partial<ErreurApi> | null = null
  try {
    corps = (await reponse.json()) as Partial<ErreurApi>
  } catch {
    corps = null
  }

  // Un 5xx sans message du serveur signifie que le backend n'est pas joignable.
  if (!corps?.message && reponse.status >= 500) {
    throw new ApiError(MESSAGE_INJOIGNABLE, { status: reponse.status, injoignable: true })
  }

  throw new ApiError(corps?.message ?? messagePourStatut(reponse.status), {
    status: reponse.status,
  })
}

export function listerTickets(statut?: Statut | ''): Promise<Ticket[]> {
  const query = statut ? `?statut=${encodeURIComponent(statut)}` : ''
  return appeler<Ticket[]>(`${BASE_URL}${query}`)
}

export function creerTicket(donnees: { titre: string; description: string | null }): Promise<Ticket> {
  return appeler<Ticket>(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(donnees),
  })
}

export function fermerTicket(id: number): Promise<Ticket> {
  return appeler<Ticket>(`${BASE_URL}/${id}/fermer`, { method: 'PATCH' })
}
