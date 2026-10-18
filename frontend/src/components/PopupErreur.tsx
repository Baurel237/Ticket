import { useEffect, useRef } from 'react'

interface PopupErreurProps {
  titre: string
  message: string
  onFermer: () => void
}

export default function PopupErreur({ titre, message, onFermer }: PopupErreurProps) {
  const boutonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    boutonRef.current?.focus()
    function surTouche(evenement: KeyboardEvent) {
      if (evenement.key === 'Escape') onFermer()
    }
    window.addEventListener('keydown', surTouche)
    return () => window.removeEventListener('keydown', surTouche)
  }, [onFermer])

  return (
    <div className="popup-fond" onClick={onFermer}>
      <div
        className="popup"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="popup-titre"
        aria-describedby="popup-message"
        onClick={(evenement) => evenement.stopPropagation()}
      >
        <h2 id="popup-titre">{titre}</h2>
        <p id="popup-message">{message}</p>
        <button type="button" ref={boutonRef} onClick={onFermer}>
          J'ai compris
        </button>
      </div>
    </div>
  )
}
