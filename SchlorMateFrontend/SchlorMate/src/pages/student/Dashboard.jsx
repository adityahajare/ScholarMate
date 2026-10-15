import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  GraduationCap,
  Sparkles,
  Bookmark,
  Calendar,
  ArrowRight,
  Clock,
  Building2,
  ChevronRight,
  TrendingUp,
} from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import DonutChart from "../../components/charts/DonutChart";
import { profileApi, scholarshipApi, recommendationApi } from "../../lib/api";
import { useAuth } from "../../context/AuthContext";

function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [scholarships, setScholarships] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    Promise.allSettled([
      profileApi.getMine(),
      scholarshipApi.list(),
      recommendationApi.getMine(),
    ]).then(([profileRes, scholarshipRes, recRes]) => {
      if (!active) return;
      if (profileRes.status === "fulfilled") setProfile(profileRes.value.data);
      if (scholarshipRes.status === "fulfilled") setScholarships(scholarshipRes.value.data || []);
      if (recRes.status === "fulfilled") setRecommendations(recRes.value.data?.recommendations || []);
      setLoading(false);
    });
    return () => {
      active = false;
    };
  }, []);

  const displayName = user?.name || profile?.fullName || "Aditya";
  const profileCompletion = profile?.completionPercentage || 80;

  // Curated demo/fallback recommendations matching mockup
  const displayRecommendations = recommendations.length
    ? recommendations.slice(0, 3).map((r) => ({
        id: r.scholarshipId || r.id,
        name: r.name,
        provider: r.provider || "Government of India",
        amount: r.amount ? `₹${r.amount.toLocaleString()}/year` : "₹50,000/year",
        match: Math.round((r.score || 0.95) * 100),
        deadline: r.applicationDeadline || "31 Dec 2025",
        tag: r.course || "Engineering",
      }))
    : [
        {
          id: 1,
          name: "AICTE Pragati Scholarship",
          provider: "Government of India",
          amount: "₹50,000/year",
          match: 95,
          deadline: "31 Dec 2025",
          tag: "Engineering",
        },
        {
          id: 2,
          name: "Swanath Scholarship",
          provider: "Maharashtra Govt",
          amount: "₹25,000/year",
          match: 90,
          deadline: "15 Nov 2025",
          tag: "Undergraduate",
        },
        {
          id: 3,
          name: "Central Sector Scheme",
          provider: "Government of India",
          amount: "₹12,000/year",
          match: 82,
          deadline: "10 Dec 2025",
          tag: "All Courses",
        },
      ];

  const applicationSegments = [
    { label: "Draft", value: 4, color: "#94a3b8" },
    { label: "Submitted", value: 8, color: "#3b82f6" },
    { label: "Under Review", value: 3, color: "#f59e0b" },
    { label: "Approved", value: 3, color: "#10b981" },
  ];

  const upcomingDeadlines = [
    { name: "AICTE Pragati", date: "31 Dec 2025", days: "45 days", color: "text-emerald-700 bg-emerald-50 border-emerald-200" },
    { name: "State Merit", date: "15 Nov 2025", days: "30 days", color: "text-amber-700 bg-amber-50 border-amber-200" },
    { name: "INSPIRE", date: "10 Dec 2025", days: "40 days", color: "text-indigo-700 bg-indigo-50 border-indigo-200" },
  ];

  if (loading) return <Loading message="Loading dashboard overview..." />;

  return (
    <div className="space-y-6 animate-fadeIn pb-12 font-sans">
      {/* Header Banner */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Welcome back, {displayName}! 👋
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Here's your scholarship journey overview.
        </p>
      </div>

      {/* 4 Summary Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Profile Completion */}
        <Card className="p-5 flex items-center justify-between" hover>
          <div>
            <p className="text-xs font-semibold text-slate-500">Profile Completion</p>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-3xl font-black text-slate-900">{profileCompletion}%</span>
              <span className="text-xs font-bold text-emerald-600 flex items-center gap-0.5">
                <TrendingUp className="w-3.5 h-3.5" /> Good
              </span>
            </div>
            <div className="w-32 bg-slate-100 rounded-full h-1.5 mt-2">
              <div
                className="bg-emerald-500 h-1.5 rounded-full"
                style={{ width: `${profileCompletion}%` }}
              />
            </div>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
            ⚡
          </div>
        </Card>

        {/* Card 2: Eligible Scholarships */}
        <Card className="p-5 flex items-center justify-between" hover>
          <div>
            <p className="text-xs font-semibold text-slate-500">Eligible Scholarships</p>
            <p className="text-3xl font-black text-slate-900 mt-1">
              {scholarships.length || 42}
            </p>
            <p className="text-[11px] text-indigo-600 font-semibold mt-1">Based on profile</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-[#544af4] flex items-center justify-center">
            <GraduationCap className="w-6 h-6" />
          </div>
        </Card>

        {/* Card 3: Saved Scholarships */}
        <Card className="p-5 flex items-center justify-between" hover>
          <div>
            <p className="text-xs font-semibold text-slate-500">Saved Scholarships</p>
            <p className="text-3xl font-black text-slate-900 mt-1">15</p>
            <p className="text-[11px] text-slate-400 font-medium mt-1">Pinned for later</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <Bookmark className="w-5 h-5" />
          </div>
        </Card>

        {/* Card 4: Upcoming Deadlines */}
        <Card className="p-5 flex items-center justify-between" hover>
          <div>
            <p className="text-xs font-semibold text-slate-500">Upcoming Deadlines</p>
            <p className="text-3xl font-black text-rose-600 mt-1">8</p>
            <p className="text-[11px] text-rose-500 font-semibold mt-1">Next: 30 days</p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center">
            <Calendar className="w-5 h-5" />
          </div>
        </Card>
      </div>

      {/* Main Grid: Left Recommendations + Right Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Recommended for You (7 cols) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base sm:text-lg font-bold text-slate-900">
                Recommended for You
              </h2>
              <p className="text-xs text-slate-500">AI-matched scholarships for your profile</p>
            </div>
            <Link
              to="/recommendations"
              className="text-xs font-bold text-[#544af4] hover:underline inline-flex items-center gap-1"
            >
              View All <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="space-y-3.5">
            {displayRecommendations.map((item) => (
              <Card key={item.id} className="p-5 hover:border-indigo-200 transition" hover>
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                  <div className="space-y-1.5 flex-1">
                    <div className="flex items-center gap-2">
                      <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-100">
                        {item.match}% Match
                      </span>
                      <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-600">
                        {item.tag}
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-slate-900 leading-snug">
                      {item.name}
                    </h3>

                    <div className="flex items-center gap-2 text-xs text-slate-500">
                      <Building2 className="w-3.5 h-3.5 text-slate-400" />
                      <span>{item.provider}</span>
                    </div>
                  </div>

                  <div className="sm:text-right shrink-0">
                    <p className="text-lg font-black text-slate-900">{item.amount}</p>
                    <p className="text-[11px] text-slate-500 flex items-center sm:justify-end gap-1 mt-0.5">
                      <Clock className="w-3 h-3 text-slate-400" /> Apply by {item.deadline}
                    </p>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <Link
                    to={`/scholarships/${item.id}`}
                    className="text-xs font-bold text-slate-600 hover:text-slate-900"
                  >
                    View Details
                  </Link>
                  <Link
                    to={`/scholarships/${item.id}/apply`}
                    className="px-4 py-1.5 rounded-xl bg-[#544af4] hover:bg-[#4337e7] text-white font-bold text-xs shadow-xs transition"
                  >
                    Apply Now
                  </Link>
                </div>
              </Card>
            ))}
          </div>
        </div>

        {/* Right Column: Application Status + Upcoming Deadlines (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Application Status Donut */}
          <Card className="p-6" hover={false}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-slate-900">Application Status</h2>
              <span className="text-xs font-bold text-slate-500">18 Total</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-12 gap-4 items-center">
              <div className="sm:col-span-7 flex justify-center">
                <DonutChart
                  size={160}
                  strokeWidth={16}
                  centerLabel="Total"
                  centerValue="18"
                  segments={applicationSegments}
                />
              </div>

              <div className="sm:col-span-5 space-y-2 text-xs">
                {applicationSegments.map((seg) => (
                  <div key={seg.label} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span
                        className="w-2.5 h-2.5 rounded-full shrink-0"
                        style={{ backgroundColor: seg.color }}
                      />
                      <span className="text-slate-600 font-medium">{seg.label}</span>
                    </div>
                    <span className="font-bold text-slate-900">{seg.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </Card>

          {/* Upcoming Deadlines */}
          <Card className="p-6" hover={false}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-slate-900">Upcoming Deadlines</h2>
              <Link to="/scholarships" className="text-xs font-bold text-[#544af4] hover:underline">
                Calendar
              </Link>
            </div>

            <div className="space-y-3">
              {upcomingDeadlines.map((item) => (
                <div
                  key={item.name}
                  className="flex items-center justify-between p-3 rounded-xl bg-slate-50/80 border border-slate-100"
                >
                  <div className="space-y-0.5">
                    <p className="text-xs font-bold text-slate-900">{item.name}</p>
                    <p className="text-[11px] text-slate-500">{item.date}</p>
                  </div>
                  <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold border ${item.color}`}>
                    {item.days}
                  </span>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
