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

// Phrases lisibles quand le serveur ne fournit pas de message : jamais de code technique.
function messagePourStatut(status) {
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
      return "Un problème est survenu. Veuillez réessayer dans un instant."
  }
}

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

  throw new ApiError(corps?.message ?? messagePourStatut(reponse.status), { status: reponse.status })
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
