import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Check, Sparkles } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import { EDUCATION_OPTIONS } from './profileSchema';
import { useProfile } from '../../context/ProfileContext';

function ProfileSetup() {
  const navigate = useNavigate();
  const { draft, saveStep } = useProfile();
  const [selected, setSelected] = useState(draft?.educationType?.value || '');
  const [saving, setSaving] = useState(false);

  const continueToBuilder = async () => {
    if (!selected) return;
    setSaving(true);
    await saveStep('educationType', { value: selected, state: 'KNOWN' });
    setSaving(false);
    navigate('/profile/builder');
  };

  return <div className="space-y-6 animate-fadeIn">
    <div><span className="text-xs font-bold uppercase tracking-wider text-blue-700">Profile Builder</span><h1 className="mt-1 text-2xl font-extrabold text-slate-900 sm:text-3xl">Let&apos;s create your profile</h1><p className="mt-1 text-sm text-slate-500">Choose your current education path. We&apos;ll ask only the questions relevant to you.</p></div>
    <Card className="overflow-hidden border-slate-200/90 p-0 shadow-sm" hover={false}>
      <div className="border-b border-slate-100 bg-gradient-to-r from-blue-50 via-white to-indigo-50 px-5 py-4 sm:px-8"><div className="flex items-start gap-3"><div className="rounded-xl bg-blue-600 p-2 text-white shadow-sm"><Sparkles className="h-4 w-4" /></div><div><p className="text-sm font-bold text-slate-900">Personalized questions</p><p className="mt-0.5 text-xs text-slate-500">Only relevant sections will be added to your profile.</p></div></div></div>
      <div className="p-5 sm:p-8">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {EDUCATION_OPTIONS.map((option) => <button key={option.id} type="button" onClick={() => setSelected(option.id)} aria-pressed={selected === option.id} className={`relative rounded-2xl border p-4 text-left transition focus:outline-none focus:ring-2 focus:ring-blue-500 ${selected === option.id ? 'border-blue-600 bg-blue-50 shadow-md ring-2 ring-blue-100' : 'border-slate-200 bg-white hover:-translate-y-0.5 hover:border-blue-300 hover:bg-slate-50 hover:shadow-sm'}`}><span className={`flex h-11 w-11 items-center justify-center rounded-xl text-lg font-extrabold ${selected === option.id ? 'bg-blue-600 text-white' : 'bg-indigo-100 text-indigo-700'}`}>{option.icon}</span>{selected === option.id && <span className="absolute right-3 top-3 flex h-5 w-5 items-center justify-center rounded-full bg-emerald-500 text-white"><Check className="h-3.5 w-3.5" /></span>}<span className="mt-3 block text-sm font-bold text-slate-900">{option.title}</span><span className="mt-1 block text-xs leading-relaxed text-slate-500">{option.subtitle}</span></button>)}
        </div>
        <div className="mt-6 flex flex-col gap-3 border-t border-slate-100 pt-5 sm:flex-row sm:items-center sm:justify-between"><p className="text-xs text-slate-500">You can change answers later from Profile Builder.</p><Button onClick={continueToBuilder} disabled={!selected || saving}>{saving ? 'Saving...' : 'Next →'}</Button></div>
      </div>
    </Card>
  </div>;
}

export default ProfileSetup;
