const BASE_URL = '/api/tickets'

export class ApiError extends Error {
  constructor(message, { status = null, injoignable = false } = {}) {
    super(message)
    this.status = status
    this.injoignable = injoignable
  }
}

const MESSAGE_INJOIGNABLE =
  "Le serveur est injoignable. Vérifiez qu'il est démarré, puis réessayez."

async function appeler(url, options) {
  let reponse
  try {
    reponse = await fetch(url, options)
  } catch {
    throw new ApiError(MESSAGE_INJOIGNABLE, { injoignable: true })
  }

  if (reponse.ok) {
    return reponse.json()
  }

  let corps = null
  try {
    corps = await reponse.json()
  } catch {
    corps = null
  }

  // Un 5xx sans message du serveur signifie que le backend n'est pas joignable.
  if (!corps?.message && reponse.status >= 500) {
    throw new ApiError(MESSAGE_INJOIGNABLE, { status: reponse.status, injoignable: true })
  }

  throw new ApiError(corps?.message ?? `Le serveur a refusé la requête (code ${reponse.status}).`, {
    status: reponse.status,
  })
}

export function listerTickets(statut) {
  const query = statut ? `?statut=${encodeURIComponent(statut)}` : ''
  return appeler(`${BASE_URL}${query}`)
}

export function creerTicket({ titre, description }) {
  return appeler(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ titre, description }),
  })
}

export function fermerTicket(id) {
  return appeler(`${BASE_URL}/${id}/fermer`, { method: 'PATCH' })
}
