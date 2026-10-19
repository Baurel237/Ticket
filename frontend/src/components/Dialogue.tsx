import { useEffect, useRef } from 'react'
import type { ReactNode } from 'react'

interface BoutonDialogue {
  libelle: string
  onClick: () => void
  variante?: 'primaire' | 'secondaire' | 'dangereux'
  disabled?: boolean
  chargement?: boolean
}

interface DialogueProps {
  titre: string
  /** Corrps du message : texte simple ou JSX (pour mettre le titre du ticket en gras). */
  contenu: ReactNode
  variante?: 'info' | 'alerte'
  boutons: BoutonDialogue[]
  onFermer: () => void
}

/** Ferme le dialogue sur Échap et maintient le focus à l'intérieur. */
function useDialogueAccessible(onFermer: () => void, reference: React.RefObject<HTMLDivElement | null>) {
  useEffect(() => {
    function surTouche(evenement: KeyboardEvent) {
      if (evenement.key === 'Escape') {
        evenement.preventDefault()
        onFermer()
        return
      }
      if (evenement.key !== 'Tab') return
      const dialogue = reference.current
      if (!dialogue) return
      const cibles = dialogue.querySelectorAll<HTMLElement>(
        'button:not(:disabled), input, textarea, [tabindex]:not([tabindex="-1"])',
      )
      if (cibles.length === 0) return
      const premiere = cibles[0]
      const derniere = cibles[cibles.length - 1]
      if (evenement.shiftKey && document.activeElement === premiere) {
        evenement.preventDefault()
        derniere.focus()
      } else if (!evenement.shiftKey && document.activeElement === derniere) {
        evenement.preventDefault()
        premiere.focus()
      }
    }
    window.addEventListener('keydown', surTouche)
    return () => window.removeEventListener('keydown', surTouche)
  }, [onFermer, reference])
}

export default function Dialogue({
  titre,
  contenu,
  variante = 'info',
  boutons,
  onFermer,
}: DialogueProps) {
  const dialogueReference = useRef<HTMLDivElement>(null)
  useDialogueAccessible(onFermer, dialogueReference)

  useEffect(() => {
    // WCAG 2.4.3 : à l'ouverture le focus entre dans le dialogue, à la
    // fermeture il revient sur l'élément déclencheur.
    const declencheur = document.activeElement as HTMLElement | null
    dialogueReference.current?.querySelector<HTMLElement>('button')?.focus()
    return () => declencheur?.focus()
  }, [])

  return (
    <div className="popup-fond" onClick={onFermer}>
      <div
        ref={dialogueReference}
        className={`popup${variante === 'alerte' ? ' popup-alerte' : ''}`}
        role={variante === 'alerte' ? 'alertdialog' : 'dialog'}
        aria-modal="true"
        aria-labelledby="popup-titre"
        aria-describedby="popup-contenu"
        onClick={(evenement) => evenement.stopPropagation()}
      >
        <h2 id="popup-titre">{titre}</h2>
        <p id="popup-contenu">{contenu}</p>
        <div className="popup-actions">
          {boutons.map((bouton) => (
            <button
              key={bouton.libelle}
              type="button"
              className={`bouton bouton-${bouton.variante ?? 'primaire'}`}
              disabled={bouton.disabled}
              onClick={bouton.onClick}
            >
              {bouton.chargement ? <span className="spinner" aria-hidden="true" /> : null}
              {bouton.libelle}
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
