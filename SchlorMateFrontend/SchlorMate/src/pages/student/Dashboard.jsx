import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Calendar, ChevronRight, Clock, Building2, Sparkles } from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import DonutChart from "../../components/charts/DonutChart";
import { mlApi, profileApi, scholarshipApi, recommendationApi } from "../../lib/api";
import { useAuth } from "../../context/AuthContext";

const profileFields = [
  "fullName", "age", "lifeStage", "occupation", "institutionType", "course", "college",
  "cgpa", "familyIncome", "category", "state", "yearOfStudy", "gender", "domicileState",
];

function formatAmount(amount) {
  return amount == null ? "Amount not specified" : `₹${Number(amount).toLocaleString("en-IN")}`;
}

function Dashboard() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [scholarships, setScholarships] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [eligibleCount, setEligibleCount] = useState(0);
  const [mlAvailable, setMlAvailable] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    Promise.allSettled([
      profileApi.getMine(), scholarshipApi.list(), recommendationApi.getMine(), mlApi.status(),
    ]).then(([profileRes, scholarshipRes, recommendationRes, mlRes]) => {
      if (!active) return;
      if (profileRes.status === "fulfilled") setProfile(profileRes.value.data);
      if (scholarshipRes.status === "fulfilled") {
        const catalog = scholarshipRes.value.data;
        setScholarships(Array.isArray(catalog) ? catalog : catalog?.content || []);
      }
      if (recommendationRes.status === "fulfilled") {
        const response = recommendationRes.value.data || {};
        setRecommendations(response.recommendations || []);
        setEligibleCount(response.eligibleCandidateCount || 0);
      }
      if (mlRes.status === "fulfilled") setMlAvailable(Boolean(mlRes.value.data?.available));
      setLoading(false);
    });
    return () => { active = false; };
  }, []);

  if (loading) return <Loading message="Loading live dashboard data..." />;

  const displayName = user?.name || profile?.fullName || "there";
  const profileCompletion = profile
    ? Math.round((profileFields.filter((field) => profile[field] !== null && profile[field] !== undefined && profile[field] !== "").length / profileFields.length) * 100)
    : 0;
  const displayRecommendations = recommendations.slice(0, 3).map((item) => ({
    id: item.scholarshipId || item.id,
    name: item.name || "Unnamed scholarship",
    provider: item.provider || "Provider not specified",
    amount: formatAmount(item.amount),
    match: Math.round((item.score || 0) * 100),
    deadline: item.applicationDeadline || "No deadline provided",
    tag: item.course || item.category || "Scholarship",
  }));
  const upcomingDeadlines = [...recommendations, ...scholarships]
    .filter((item) => item.applicationDeadline)
    .map((item) => ({ name: item.name || "Unnamed scholarship", date: item.applicationDeadline }))
    .filter((item, index, items) => items.findIndex((candidate) => candidate.name === item.name) === index)
    .sort((a, b) => new Date(a.date) - new Date(b.date)).slice(0, 3);
  const applicationSegments = [{ label: "Tracked applications", value: 0, color: "#94a3b8" }];
  const mlStatusClass = mlAvailable ? "border-emerald-200 bg-emerald-50 text-emerald-800" : "border-amber-200 bg-amber-50 text-amber-800";

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      <div><h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Welcome back, {displayName}!</h1><p className="text-xs sm:text-sm text-slate-500 mt-1">Your scholarship journey, based on the latest saved data.</p></div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-5" hover><p className="text-xs font-semibold text-slate-500">Profile Completion</p><p className="text-3xl font-black text-slate-900 mt-1">{profileCompletion}%</p><div className="w-full bg-slate-100 rounded-full h-1.5 mt-3"><div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: `${profileCompletion}%` }} /></div></Card>
        <Card className="p-5" hover><p className="text-xs font-semibold text-slate-500">Eligible Scholarships</p><p className="text-3xl font-black text-slate-900 mt-1">{eligibleCount}</p><p className="text-[11px] text-indigo-600 font-semibold mt-1">From backend eligibility</p></Card>
        <Card className="p-5" hover><p className="text-xs font-semibold text-slate-500">AI Recommendations</p><p className="text-3xl font-black text-slate-900 mt-1">{recommendations.length}</p><p className="text-[11px] text-slate-400 font-medium mt-1">From your saved profile</p></Card>
        <Card className="p-5" hover><p className="text-xs font-semibold text-slate-500">Upcoming Deadlines</p><p className="text-3xl font-black text-rose-600 mt-1">{upcomingDeadlines.length}</p><p className="text-[11px] text-rose-500 font-semibold mt-1">From live catalog data</p></Card>
      </div>

      <div className={`rounded-2xl border p-4 text-xs ${mlStatusClass}`}><span className="font-bold">ML service: </span>{mlAvailable === null ? "checking..." : mlAvailable ? "online" : "offline"}</div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between"><div><h2 className="text-base sm:text-lg font-bold text-slate-900">Recommended for You</h2><p className="text-xs text-slate-500">Live ML matches for your profile</p></div><Link to="/recommendations" className="text-xs font-bold text-[#544af4] hover:underline inline-flex items-center gap-1">View All <ChevronRight className="w-3.5 h-3.5" /></Link></div>
          {displayRecommendations.length ? displayRecommendations.map((item) => <Card key={item.id} className="p-5" hover><div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4"><div className="space-y-1.5 flex-1"><div className="flex items-center gap-2"><span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-100">{item.match}% Match</span><span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-600">{item.tag}</span></div><h3 className="text-base font-bold text-slate-900 leading-snug">{item.name}</h3><div className="flex items-center gap-2 text-xs text-slate-500"><Building2 className="w-3.5 h-3.5 text-slate-400" />{item.provider}</div></div><div className="sm:text-right shrink-0"><p className="text-lg font-black text-slate-900">{item.amount}</p><p className="text-[11px] text-slate-500 flex items-center sm:justify-end gap-1 mt-0.5"><Clock className="w-3 h-3 text-slate-400" />Apply by {item.deadline}</p></div></div><div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between"><Link to={`/scholarships/${item.id}`} className="text-xs font-bold text-slate-600 hover:text-slate-900">View Details</Link><Link to={`/scholarships/${item.id}/apply`} className="px-4 py-1.5 rounded-xl bg-[#544af4] hover:bg-[#4337e7] text-white font-bold text-xs">Apply Now</Link></div></Card>) : <Card className="p-6" hover={false}><div className="flex items-center gap-2 text-sm text-slate-500"><Sparkles className="w-4 h-4" />Complete your profile to generate live ML recommendations.</div></Card>}
        </div>

        <div className="lg:col-span-5 space-y-6">
          <Card className="p-6" hover={false}><div className="flex items-center justify-between mb-4"><h2 className="text-base font-bold text-slate-900">Application Status</h2><span className="text-xs font-bold text-slate-500">0 Total</span></div><div className="flex justify-center"><DonutChart size={160} strokeWidth={16} centerLabel="Total" centerValue="0" segments={applicationSegments} /></div><p className="text-xs text-center text-slate-500 mt-3">Application tracking is not available from the backend yet.</p></Card>
          <Card className="p-6" hover={false}><div className="flex items-center justify-between mb-4"><h2 className="text-base font-bold text-slate-900">Upcoming Deadlines</h2><Link to="/scholarships" className="text-xs font-bold text-[#544af4] hover:underline">Browse</Link></div>{upcomingDeadlines.length ? <div className="space-y-3">{upcomingDeadlines.map((item) => <div key={item.name} className="flex items-center justify-between p-3 rounded-xl bg-slate-50/80 border border-slate-100"><div><p className="text-xs font-bold text-slate-900">{item.name}</p><p className="text-[11px] text-slate-500">{item.date}</p></div><Calendar className="w-4 h-4 text-indigo-500" /></div>)}</div> : <p className="text-sm text-slate-500">No upcoming deadlines were returned by the backend.</p>}</Card>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
