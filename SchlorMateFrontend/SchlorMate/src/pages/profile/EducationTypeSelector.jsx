import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProfile } from '../../context/ProfileContext';
import EducationCard from '../../components/EducationCard';

const options = [
  { id: 'school', title: 'School — Classes 8–10', desc: 'Secondary school level', icon: '🎒' },
  { id: 'intermediate', title: 'Class 11–12 — Intermediate', desc: 'Higher secondary', icon: '📚' },
  { id: 'diploma', title: 'Diploma — Polytechnic', desc: 'Technical diploma', icon: '🛠️' },
  { id: 'iti', title: 'ITI — Industrial Training', desc: 'Vocational training', icon: '🏭' },
  { id: 'undergrad', title: 'Undergraduate — B.Tech, BE, BSc, BCA, BBA', desc: 'First degree', icon: '🎓' },
  { id: 'postgrad', title: 'Postgraduate — ME, M.Tech, MSc, MBA, MCA', desc: 'Second degree', icon: '📖' },
  { id: 'phd', title: 'PhD — Research', desc: 'Doctorate', icon: '🔬' },
  { id: 'other', title: 'Other — Vocational or professional', desc: 'Other paths', icon: '⚙️' },
];

export default function EducationTypeSelector() {
  const navigate = useNavigate();
  const { saveStep } = useProfile();
  const [selected, setSelected] = useState(null);

  const handleNext = async () => {
    if (!selected) return;
    await saveStep('educationType', { type: selected });
    navigate('/profile/builder');
  };

  return (
    <div className="education-selector">
      <h2>Let&apos;s Create Your Profile</h2>
      <p>Select your current education level to get a personalized form.</p>
      <div className="cards-grid">
        {options.map(opt => (
          <EducationCard
            key={opt.id}
            selected={selected === opt.id}
            onClick={() => setSelected(opt.id)}
            icon={opt.icon}
            title={opt.title}
            description={opt.desc}
          />
        ))}
      </div>
      <p className="helper">
        The form will be customized according to the selected education level.
      </p>
      <button className="next-btn" disabled={!selected} onClick={handleNext}>Next</button>
    </div>
  );
}
