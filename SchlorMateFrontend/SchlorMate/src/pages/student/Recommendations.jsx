import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Sparkles, Lightbulb, CheckCircle2, Calendar, ArrowRight, Building2, ShieldCheck } from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import { mlApi, recommendationApi } from "../../lib/api";

function Recommendations() {
  const [recommendations, setRecommendations] = useState([]);
  const [eligibleCount, setEligibleCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mlAvailable, setMlAvailable] = useState(null);

  const loadRecommendations = () => {
    setLoading(true);
    setError("");
    mlApi.status()
      .then((statusResponse) => setMlAvailable(Boolean(statusResponse.data?.available)))
      .catch(() => setMlAvailable(false));
    recommendationApi.getMine()
      .then((recommendationResponse) => {
        const { data } = recommendationResponse;
        setRecommendations((data.recommendations || []).map((item) => ({
          ...item,
          id: item.scholarshipId,
          match: Math.round((item.score || 0) * 100),
          deadline: item.applicationDeadline || "Open deadline",
          reasons: item.reasons?.length ? item.reasons : ["Matched against your saved profile and eligibility data."],
        })));
        setEligibleCount(data.eligibleCandidateCount || 0);
      })
      .catch((requestError) => setError(requestError.response?.data?.message || "Complete your profile to generate recommendations."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    const timer = setTimeout(loadRecommendations, 0);
    return () => clearTimeout(timer);
  }, []);

  if (loading) return <Loading message="Generating live recommendations..." />;
  if (error) return <ErrorState title="Recommendations unavailable" message={error} onRetry={loadRecommendations} />;
  if (!recommendations.length) {
    return <EmptyState icon={Sparkles} title="No recommendations yet" message="Complete your User Profile and add eligibility information to generate live matches." buttonText="Open User Profile" onButtonClick={() => { window.location.href = "/profile"; }} />;
  }

  const peakMatch = Math.max(...recommendations.map((item) => item.match));
  const averageMatch = Math.round(recommendations.reduce((sum, item) => sum + item.match, 0) / recommendations.length);

  return (
    <div className="space-y-6 animate-fadeIn">
      <div>
        <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100 inline-flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5" /> Stored Catalog AI Ranking
        </span>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">Personalized Recommendations</h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">These matches are generated from your saved User Profile and the latest stored scholarship catalog.</p>
      </div>

      <div className={`rounded-2xl border p-4 text-xs ${mlAvailable ? "border-emerald-200 bg-emerald-50 text-emerald-800" : "border-amber-200 bg-amber-50 text-amber-800"}`}>
        <span className="font-bold">ML service: </span>{mlAvailable === null ? "checking..." : mlAvailable ? "online" : "offline — start the FastAPI service on port 8000"}
      </div>

      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200/80 rounded-2xl p-5">
        <div className="flex items-start gap-3.5">
          <div className="w-9 h-9 rounded-xl bg-blue-600 text-white flex items-center justify-center shrink-0"><Lightbulb className="w-5 h-5" /></div>
          <div><h2 className="text-sm font-bold text-blue-900">How your matches are calculated</h2><p className="text-xs text-blue-700 mt-1 leading-relaxed">Stored scholarship records are checked against your profile first, then ranked using academic, financial, semantic, clustering, and graph signals.</p></div>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="p-5" hover><p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Eligible opportunities</p><p className="text-3xl font-extrabold text-slate-900 mt-1">{eligibleCount}</p></Card>
        <Card className="p-5" hover><p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Peak AI match</p><p className="text-3xl font-extrabold text-emerald-600 mt-1">{peakMatch}%</p></Card>
        <Card className="p-5" hover><p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Average compatibility</p><p className="text-3xl font-extrabold text-blue-600 mt-1">{averageMatch}%</p></Card>
      </div>

      <div className="space-y-4">
        {recommendations.map((scholarship, index) => (
          <Card key={scholarship.id} className="p-6" hover>
            <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-5">
              <div className="flex items-start gap-4"><div className="w-10 h-10 rounded-xl bg-blue-50 border border-blue-100 flex items-center justify-center font-extrabold text-sm text-blue-700 shrink-0">#{index + 1}</div><div><h2 className="text-lg font-bold text-slate-900">{scholarship.name}</h2><div className="flex items-center gap-1.5 text-xs text-slate-500 mt-1"><Building2 className="w-3.5 h-3.5 text-slate-400" />{scholarship.provider || "Verified provider"}<ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /></div><span className="inline-flex items-center gap-1 bg-amber-50 text-amber-700 border border-amber-100 px-2.5 py-0.5 rounded-full text-xs font-semibold mt-2"><Calendar className="w-3.5 h-3.5" />Deadline: {scholarship.deadline}</span></div></div>
              <div className="text-right"><span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">AI Match</span><p className="text-3xl font-extrabold text-emerald-600">{scholarship.match}%</p></div>
            </div>
            <div className="mt-5 space-y-1"><div className="flex justify-between text-xs font-semibold"><span className="text-slate-600">Match accuracy</span><span className="text-emerald-600">{scholarship.match}%</span></div><div className="w-full bg-slate-100 rounded-full h-2"><div className="bg-emerald-500 h-2 rounded-full" style={{ width: `${scholarship.match}%` }} /></div></div>
            <div className="mt-5 pt-5 border-t border-slate-100"><h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2.5 flex items-center gap-1.5"><CheckCircle2 className="w-4 h-4 text-emerald-600" />Why this matches your profile</h3><div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">{scholarship.reasons.map((reason, reasonIndex) => <div key={reasonIndex} className="flex items-start gap-2 text-slate-600 bg-slate-50/70 p-2.5 rounded-xl border border-slate-100"><span className="text-emerald-700 font-bold">✓</span><span>{reason}</span></div>)}</div></div>
            <div className="flex flex-col sm:flex-row gap-2.5 mt-5 pt-3 border-t border-slate-100"><Link to={`/scholarships/${scholarship.id}`} className="flex-1 text-center py-2.5 px-4 rounded-xl text-xs font-bold border border-slate-300 text-slate-700 hover:bg-slate-50">View Details</Link><Link to={`/scholarships/${scholarship.id}/apply`} className="flex-1 text-center py-2.5 px-4 rounded-xl text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 inline-flex items-center justify-center gap-2">Apply Now <ArrowRight className="w-3.5 h-3.5" /></Link></div>
          </Card>
        ))}
      </div>
    </div>
  );
}

export default Recommendations;
