import { useEffect, useRef } from 'react'

export default function PopupErreur({ titre, message, onFermer }) {
  const boutonRef = useRef(null)

  useEffect(() => {
    boutonRef.current?.focus()
    function surTouche(evenement) {
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
