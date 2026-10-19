interface AlerteProps {
  variante: 'succes' | 'erreur'
  message: string
  /** Texte de l'action facultative (ex. « Réessayer »). */
  action?: string
  onAction?: () => void
}

/** Alerte annonce le message ; l'action est cliquable au clavier quand présente. */
export default function Alerte({ variante, message, action, onAction }: AlerteProps) {
  const role = variante === 'erreur' ? 'alert' : 'status'

  return (
    <div className={`alerte alerte-${variante}`} role={role}>
      <p>{message}</p>
      {action && onAction && (
        <button type="button" className="bouton bouton-discret" onClick={onAction}>
          {action}
        </button>
      )}
    </div>
  )
}
