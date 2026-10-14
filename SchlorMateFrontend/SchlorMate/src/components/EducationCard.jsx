// src/components/EducationCard.jsx
import './EducationCard.css';

export default function EducationCard({ icon, title, description, selected, onClick }) {
  return (
    <div
      className={`edu-card ${selected ? 'selected' : ''}`}
      onClick={onClick}
    >
      <div className="icon">{icon}</div>
      <h3>{title}</h3>
      <p>{description}</p>
      {selected && <span className="checkmark">✔︎</span>}
    </div>
  );
}
