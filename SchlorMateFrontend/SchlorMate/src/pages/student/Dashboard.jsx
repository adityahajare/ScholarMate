import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AlertCircle, ArrowRight, ClipboardList, Compass, GraduationCap, Sparkles, UserCheck } from "lucide-react";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import ErrorState from "../../components/common/ErrorState";
import { mlApi, profileApi, recommendationApi, scholarshipApi } from "../../lib/api";
import { useAuth } from "../../context/AuthContext";

function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [scholarships, setScholarships] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [mlAvailable, setMlAvailable] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    Promise.all([profileApi.getMine(), scholarshipApi.list()])
      .then(([profileResponse, scholarshipResponse]) => {
        if (!active) return;
        setProfile(profileResponse.data);
        setScholarships(scholarshipResponse.data || []);
      })
      .catch((requestError) => active && setError(requestError.response?.data?.message || "Unable to load your saved dashboard data."))
      .finally(() => active && setLoading(false));
    recommendationApi.getMine()
      .then((response) => {
        if (active) setRecommendations(response.data?.recommendations || []);
      })
      .then(() => mlApi.status())
      .then((response) => active && setMlAvailable(Boolean(response.data?.available)))
      .catch(() => active && setMlAvailable(false));
    return () => { active = false; };
  }, []);

  const categories = useMemo(() => scholarships.reduce((counts, scholarship) => {
    const category = scholarship.category || "Unclassified";
    counts[category] = (counts[category] || 0) + 1;
    return counts;
  }, {}), [scholarships]);
  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
  const userName = user?.name || user?.email || "User";

  if (loading) return <Loading message="Loading your saved data..." />;
  if (error) return <ErrorState title="Dashboard unavailable" message={error} onRetry={() => window.location.reload()} />;

  return (
    <div className="space-y-6 animate-fadeIn">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 bg-gradient-to-r from-blue-900 via-blue-800 to-indigo-900 rounded-3xl p-6 sm:p-8 text-white">
        <div><span className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-white/10 text-blue-200 border border-white/10"><Sparkles className="w-3.5 h-3.5" /> Stored catalog and profile data</span><h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight mt-3">{greeting}, {userName}</h1><p className="text-blue-100 text-sm mt-2">Your dashboard uses records saved from your account and the latest scholarship catalog.</p></div>
        <div className="flex flex-wrap gap-3"><Button variant="invertOutline" onClick={() => navigate("/scholarships")} icon={Compass}>Explore Scholarships</Button><Button variant="invert" onClick={() => navigate("/recommendations")} icon={Sparkles}>View Matches</Button></div>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4"><Metric icon={GraduationCap} label="Stored scholarships" value={scholarships.length} /><Metric icon={Sparkles} label="Saved recommendations" value={recommendations.length} /><Metric icon={UserCheck} label="Profile" value={profile ? "Saved" : "Not available"} /><Metric icon={ClipboardList} label="Applications" value="No records" /></div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="p-6" hover={false}><h2 className="text-base font-bold text-slate-900">Saved scholarship categories</h2><p className="text-xs text-slate-500 mt-1">Calculated from the current database catalog.</p>{Object.keys(categories).length ? <div className="mt-4 space-y-3">{Object.entries(categories).map(([category, count]) => <div key={category} className="flex justify-between text-sm"><span>{category}</span><strong>{count}</strong></div>)}</div> : <EmptyData message="No stored scholarships are available yet." />}</Card>
        <Card className="p-6" hover={false}><h2 className="text-base font-bold text-slate-900">Recommended from saved data</h2><p className="text-xs text-slate-500 mt-1">AI results are shown only when returned by the backend.</p><div className={`mt-3 text-xs ${mlAvailable ? "text-emerald-700" : "text-amber-700"}`}>ML service: {mlAvailable === null ? "checking..." : mlAvailable ? "online" : "offline; recommendations will retry when the service is available"}</div>{recommendations.length ? <div className="mt-4 space-y-3">{recommendations.slice(0, 3).map((item) => <div key={item.scholarshipId} className="flex items-center justify-between gap-3"><span className="text-sm font-semibold">{item.name}</span><span className="text-xs font-bold text-emerald-700">{Math.round((item.score || 0) * 100)}%</span></div>)}</div> : <EmptyData message="No recommendations are available for this profile yet." />}<Link to="/recommendations" className="inline-flex items-center gap-1 mt-5 text-xs font-bold text-blue-600">Open recommendations <ArrowRight className="w-3 h-3" /></Link></Card>
      </div>
      <Card className="p-5" hover={false}><div className="flex items-start gap-3 text-sm text-slate-600"><AlertCircle className="w-4 h-4 text-amber-600 mt-0.5" /><span>Application tracking is not shown as completed until an application record is saved by the backend.</span></div></Card>
    </div>
  );
}

function Metric({ icon: Icon, label, value }) { return <Card className="p-5" hover><Icon className="w-5 h-5 text-blue-600" /><p className="text-xs font-bold text-slate-500 uppercase tracking-wider mt-3">{label}</p><p className="text-2xl font-extrabold text-slate-900 mt-1">{value}</p></Card>; }
function EmptyData({ message }) { return <p className="mt-5 text-sm text-slate-500">{message}</p>; }
export default Dashboard;
