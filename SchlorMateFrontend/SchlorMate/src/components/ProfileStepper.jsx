import { Link, useLocation } from 'react-router-dom';
import './ProfileStepper.css';

const steps = [
  { label: 'Personal Info', path: '/profile/builder' },
  // future steps can be added here
  { label: 'Review & Submit', path: '/profile/builder/review' },
];

export default function ProfileStepper() {
  const location = useLocation();
  const currentIndex = steps.findIndex(s => location.pathname.startsWith(s.path));

  return (
    <div className="profile-stepper">
      {steps.map((s, idx) => (
        <Link
          key={s.path}
          to={s.path}
          className={`step ${idx === currentIndex ? 'active' : ''} ${idx < currentIndex ? 'completed' : ''}`}
        >
          <span className="step-number">{idx + 1}</span>
          <span className="step-label">{s.label}</span>
          {idx < currentIndex && <span className="check">✔︎</span>}
        </Link>
      ))}
    </div>
  );
}
