import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { AlertCircle, CheckCircle2, FileText, Sparkles, UserCheck } from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import ErrorState from "../../components/common/ErrorState";
import { profileApi } from "../../lib/api";

const fields = [
  ["course", "Course"], ["college", "College"], ["state", "State"],
  ["category", "Category"], ["cgpa", "CGPA"], ["familyIncome", "Family income"],
  ["yearOfStudy", "Year of study"], ["gender", "Gender"],
];

function Improvements() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { profileApi.getMine().then(({ data }) => setProfile(data)).catch((e) => setError(e.response?.data?.message || "Unable to load the saved profile.")).finally(() => setLoading(false)); }, []);
  if (loading) return <Loading message="Reading your saved profile..." />;
  if (error) return <ErrorState title="Profile data unavailable" message={error} onRetry={() => window.location.reload()} />;
  const missing = fields.filter(([key]) => profile?.[key] == null || profile[key] === "");
  const completed = fields.length - missing.length;
  return <div className="space-y-6 animate-fadeIn">
    <div><span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100"><Sparkles className="w-3.5 h-3.5" /> Saved profile review</span><h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">Profile Improvement Roadmap</h1><p className="text-slate-500 text-sm mt-1">Only missing fields from your saved User Profile are listed. No estimated scores are invented.</p></div>
    <Card className="p-6" hover={false}><div className="flex items-center gap-3"><UserCheck className="w-5 h-5 text-blue-600" /><div><h2 className="font-bold text-slate-900">Profile completeness</h2><p className="text-sm text-slate-500">{completed} of {fields.length} tracked fields are saved.</p></div></div><div className="w-full bg-slate-100 rounded-full h-2 mt-4"><div className="bg-blue-600 h-2 rounded-full" style={{ width: `${Math.round((completed / fields.length) * 100)}%` }} /></div></Card>
    {missing.length ? <div className="space-y-3">{missing.map(([key, label]) => <Card key={key} className="p-5" hover={false}><div className="flex items-center justify-between gap-4"><div className="flex items-center gap-3"><FileText className="w-5 h-5 text-amber-600" /><div><h3 className="font-bold text-slate-900">Add {label}</h3><p className="text-xs text-slate-500">This value is currently missing from your saved profile.</p></div></div><Link to="/profile" className="text-xs font-bold text-blue-600">Update profile</Link></div></Card>)}</div> : <Card className="p-6" hover={false}><div className="flex items-center gap-2 text-emerald-700"><CheckCircle2 className="w-5 h-5" /> Your tracked profile fields are complete.</div></Card>}
    <div className="p-4 rounded-xl bg-slate-100 border border-slate-200 text-xs text-slate-600 flex gap-2"><AlertCircle className="w-4 h-4 text-blue-600 shrink-0" /> Recommendations will change only after saved profile data changes.</div>
  </div>;
}
export default Improvements;
