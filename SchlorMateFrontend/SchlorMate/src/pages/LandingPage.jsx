import { Link, useNavigate } from "react-router-dom";
import {
  GraduationCap,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
  Users,
  Search,
  BookOpen,
  Award,
} from "lucide-react";

export default function LandingPage() {
  const navigate = useNavigate();

  const stats = [
    { value: "10K+", label: "Scholarships" },
    { value: "100K+", label: "Students" },
    { value: "95%", label: "Match Accuracy" },
    { value: "50+", label: "Sources" },
  ];

  const features = [
    {
      icon: Sparkles,
      title: "AI-Powered Matching",
      description: "Our multi-signal recommendation engine matches your CGPA, course, income, and category with ideal opportunities.",
    },
    {
      icon: ShieldCheck,
      title: "100% Verified Portals",
      description: "Direct integrations with National Scholarship Portal (NSP), MahaDBT, and trusted private endowments.",
    },
    {
      icon: BookOpen,
      title: "Instant Document Review",
      description: "Upload marksheets or certificates with smart OCR data extraction to confirm eligibility with zero guesswork.",
    },
    {
      icon: Award,
      title: "Automated Deadline Alerts",
      description: "Track application statuses and upcoming deadlines so you never miss funding opportunities.",
    },
  ];

  return (
    <div className="min-h-screen bg-white text-slate-800 flex flex-col font-sans">
      {/* Top Navigation */}
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-18 flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#6355ff] to-[#4838e8] flex items-center justify-center text-white shadow-md shadow-indigo-600/30">
              <GraduationCap className="w-5 h-5" />
            </div>
            <span className="text-xl font-extrabold text-slate-900 tracking-tight">
              Scholar<span className="text-[#554af6]">Mate</span>
            </span>
          </Link>

          {/* Links */}
          <nav className="hidden md:flex items-center gap-8 text-sm font-semibold text-slate-600">
            <Link to="/" className="text-[#554af6] hover:text-[#4539e5] transition">Home</Link>
            <a href="#features" className="hover:text-slate-900 transition">Features</a>
            <Link to="/scholarships" className="hover:text-slate-900 transition">Scholarships</Link>
            <a href="#about" className="hover:text-slate-900 transition">About</a>
          </nav>

          {/* Action buttons */}
          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="px-4 py-2 text-sm font-bold text-slate-700 hover:text-indigo-600 transition"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="px-5 py-2.5 rounded-xl bg-[#544af4] hover:bg-[#4438e8] text-white text-sm font-bold shadow-md shadow-indigo-600/25 transition inline-flex items-center gap-1.5"
            >
              Get Started <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative pt-12 pb-20 lg:pt-20 lg:pb-28 bg-gradient-to-b from-[#f8f9ff] via-white to-white overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left Text */}
          <div className="lg:col-span-7 space-y-6 text-center lg:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-100">
              <Sparkles className="w-3.5 h-3.5 text-indigo-600" /> AI-Driven Opportunity Discovery
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black text-slate-900 tracking-tight leading-tight">
              Your Partner in Finding the <span className="text-[#544af4]">Right Scholarships</span>
            </h1>

            <p className="text-base sm:text-lg text-slate-600 max-w-xl mx-auto lg:mx-0 leading-relaxed font-normal">
              Discover, match and apply for scholarships with the power of AI. One platform, All opportunities.
            </p>

            <div className="pt-2 flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
              <button
                onClick={() => navigate("/register")}
                className="px-7 py-3.5 rounded-xl bg-[#544af4] hover:bg-[#4337e7] text-white font-bold text-sm shadow-lg shadow-indigo-600/30 transition transform hover:-translate-y-0.5"
              >
                Get Started
              </button>
              <button
                onClick={() => navigate("/scholarships")}
                className="px-7 py-3.5 rounded-xl border border-slate-300 hover:border-slate-400 bg-white text-slate-700 font-bold text-sm shadow-xs transition"
              >
                Learn More
              </button>
            </div>
          </div>

          {/* Right Hero Graphic */}
          <div className="lg:col-span-5 relative flex justify-center">
            <div className="relative w-full max-w-md aspect-square rounded-3xl bg-gradient-to-tr from-[#eceaff] to-[#fbfaff] border border-indigo-100/80 p-8 flex flex-col justify-between shadow-xl">
              {/* Floating badges */}
              <div className="flex justify-between items-start">
                <div className="rounded-2xl bg-white p-3 shadow-md border border-slate-100 flex items-center gap-3 animate-pulse">
                  <div className="w-9 h-9 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center font-bold">
                    95%
                  </div>
                  <div>
                    <p className="text-[11px] font-bold text-slate-800">Eligibility Match</p>
                    <p className="text-[10px] text-slate-500">AI Verified</p>
                  </div>
                </div>

                <div className="rounded-2xl bg-white p-3 shadow-md border border-slate-100 flex items-center gap-2">
                  <Award className="w-5 h-5 text-indigo-600" />
                  <span className="text-xs font-bold text-slate-800">₹50,000 / yr</span>
                </div>
              </div>

              {/* Center student avatar depiction */}
              <div className="text-center py-6">
                <div className="w-28 h-28 mx-auto rounded-3xl bg-gradient-to-br from-[#544af4] to-[#7f75ff] flex items-center justify-center text-white shadow-xl shadow-indigo-500/30">
                  <GraduationCap className="w-14 h-14" />
                </div>
                <h3 className="text-base font-extrabold text-slate-900 mt-4">Personalized Matching</h3>
                <p className="text-xs text-slate-500 mt-1">NSP • MahaDBT • Private Schemes</p>
              </div>

              {/* Bottom status badge */}
              <div className="rounded-xl bg-white/90 backdrop-blur-xs p-3 border border-slate-100 flex items-center justify-between text-xs">
                <span className="font-semibold text-slate-600">Active Schemes</span>
                <span className="font-bold text-indigo-600">342 Available Today</span>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Bar */}
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 mt-16">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 bg-white rounded-2xl border border-slate-200/90 p-6 sm:p-8 shadow-sm">
            {stats.map((stat) => (
              <div key={stat.label} className="text-center">
                <p className="text-3xl sm:text-4xl font-extrabold text-[#544af4] tracking-tight">
                  {stat.value}
                </p>
                <p className="text-xs sm:text-sm font-semibold text-slate-600 mt-1">
                  {stat.label}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-slate-50 border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-14">
            <span className="text-xs font-bold text-[#544af4] uppercase tracking-wider">Features</span>
            <h2 className="text-3xl font-extrabold text-slate-900 mt-2">
              Everything you need to secure funding
            </h2>
            <p className="text-slate-500 text-sm mt-2">
              Streamline your education journey from application to grant disbursement.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feat) => {
              const Icon = feat.icon;
              return (
                <div
                  key={feat.title}
                  className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-xs hover:shadow-md transition"
                >
                  <div className="w-11 h-11 rounded-xl bg-indigo-50 text-[#544af4] flex items-center justify-center mb-4">
                    <Icon className="w-5 h-5" />
                  </div>
                  <h3 className="text-base font-bold text-slate-900">{feat.title}</h3>
                  <p className="text-xs text-slate-600 leading-relaxed mt-2">{feat.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer id="about" className="mt-auto border-t border-slate-200 bg-white py-8 px-4 sm:px-6">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div className="flex items-center gap-2">
            <GraduationCap className="w-4 h-4 text-[#544af4]" />
            <span className="font-bold text-slate-800">ScholarMate</span> &copy; 2026. All rights reserved.
          </div>
          <div className="flex items-center gap-6">
            <Link to="/login" className="hover:text-slate-800">Login</Link>
            <Link to="/register" className="hover:text-slate-800">Sign Up</Link>
            <Link to="/scholarships" className="hover:text-slate-800">Catalog</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
