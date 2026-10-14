import { useEffect, useState } from "react";
import { AlertCircle, Sparkles } from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import ErrorState from "../../components/common/ErrorState";
import { recommendationApi } from "../../lib/api";

function Prediction() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => { recommendationApi.getMine().then(({ data: response }) => setData(response)).catch((e) => setError(e.response?.data?.message || "The backend could not calculate predictions from saved data.")).finally(() => setLoading(false)); }, []);
  if (loading) return <Loading message="Loading saved recommendation scores..." />;
  if (error) return <ErrorState title="Prediction unavailable" message={error} onRetry={() => window.location.reload()} />;
  const recommendations = data?.recommendations || [];
  return <div className="space-y-6 animate-fadeIn"><div><span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-50 text-purple-700 border border-purple-100"><Sparkles className="w-3.5 h-3.5" /> Backend ranking results</span><h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">Success Prediction</h1><p className="text-slate-500 text-sm mt-1">Scores below are displayed only when returned by the recommendation service.</p></div>{recommendations.length ? <div className="grid grid-cols-1 md:grid-cols-2 gap-4">{recommendations.map((item) => { const score = Math.round((item.score || 0) * 100); return <Card key={item.scholarshipId} className="p-6" hover><h2 className="font-bold text-slate-900">{item.name}</h2><p className="text-xs text-slate-500 mt-1">{item.provider || "Stored provider"}</p><p className="text-4xl font-extrabold text-emerald-600 mt-5">{score}%</p><p className="text-xs text-slate-500 mt-1">Backend recommendation score</p>{item.reasons?.length ? <ul className="mt-4 list-disc pl-5 text-xs text-slate-600">{item.reasons.map((reason) => <li key={reason}>{reason}</li>)}</ul> : null}</Card>; })}</div> : <Card className="p-6" hover={false}><div className="flex items-center gap-2 text-slate-600"><AlertCircle className="w-4 h-4" /> No prediction is available until saved profile and scholarship data produce a match.</div></Card>}</div>;
}
export default Prediction;
