import { useEffect, useState } from "react";
import { Printer, FileText, AlertCircle } from "lucide-react";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import ErrorState from "../../components/common/ErrorState";
import { profileApi, recommendationApi, scholarshipApi } from "../../lib/api";

function Reports() {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { Promise.all([profileApi.getMine(), scholarshipApi.list(), recommendationApi.getMine()]).then(([profile, scholarships, recommendations]) => setReport({ profile: profile.data, scholarships: scholarships.data || [], recommendations: recommendations.data?.recommendations || [] })).catch((e) => setError(e.response?.data?.message || "Unable to load saved report data.")).finally(() => setLoading(false)); }, []);
  if (loading) return <Loading message="Preparing your saved report..." />;
  if (error) return <ErrorState title="Report unavailable" message={error} onRetry={() => window.location.reload()} />;
  return <div className="space-y-6 animate-fadeIn"><div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4"><div><span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100"><FileText className="w-3.5 h-3.5" /> Saved data report</span><h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">Academic & Scholarship Report</h1><p className="text-slate-500 text-sm mt-1">This report contains database-backed values only.</p></div><Button onClick={() => window.print()} icon={Printer}>Print / Export</Button></div><div className="grid grid-cols-1 sm:grid-cols-3 gap-4"><Stat label="Stored scholarships" value={report.scholarships.length} /><Stat label="Recommendations" value={report.recommendations.length} /><Stat label="Profile" value={report.profile ? "Saved" : "Missing"} /></div><Card className="p-6" hover={false}><h2 className="font-bold text-slate-900">Saved User Profile</h2><div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4 text-sm">{Object.entries(report.profile || {}).filter(([key]) => key !== "id" && key !== "user").map(([key, value]) => <div key={key} className="rounded-lg bg-slate-50 p-3"><span className="text-xs text-slate-500">{key}</span><p className="font-semibold text-slate-900 break-words">{value == null || value === "" ? "Not provided" : String(value)}</p></div>)}</div>{!report.profile && <div className="flex gap-2 text-sm text-amber-700"><AlertCircle className="w-4 h-4" /> No saved profile was returned.</div>}</Card></div>;
}
function Stat({ label, value }) { return <Card className="p-5" hover><p className="text-xs font-bold text-slate-500 uppercase tracking-wider">{label}</p><p className="text-3xl font-extrabold text-slate-900 mt-2">{value}</p></Card>; }
export default Reports;
