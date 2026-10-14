import { useState } from "react";
import {
  User,
  CheckCircle2,
  ShieldCheck,
  AlertCircle,
} from "lucide-react";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import DonutChart from "../../components/charts/DonutChart";
import { useAuth } from "../../context/AuthContext";
import { profileApi } from "../../lib/api";

function Profile() {
  const { user } = useAuth();
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [step, setStep] = useState(1);
  const steps = ["Identity", "Education", "Academic trend", "Eligibility"];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveError("");
    const form = new FormData(e.currentTarget);
    const value = (name) => form.get(name) || null;
    const knownFeature = (name) => {
      const current = value(name);
      return current ? { status: "KNOWN", value: current } : { status: "UNKNOWN" };
    };
    try {
      await profileApi.updateMine({
        fullName: value("fullName"),
        age: value("ageYears") ? Number(value("ageYears")) : null,
        lifeStage: value("educationStage"),
        course: value("course"),
        college: value("college"),
        cgpa: value("cgpa") ? Number(value("cgpa")) : null,
        familyIncome: value("familyIncome") ? Number(value("familyIncome")) : null,
        category: value("category"),
        state: value("state"),
        yearOfStudy: value("yearOfStudy") ? Number(value("yearOfStudy")) : null,
        gender: value("gender"),
        domicileState: value("domicileState") || value("state"),
        educationStage: value("educationStage"),
        features: {
          age_years: { ...knownFeature("ageYears"), value: value("ageYears") ? Number(value("ageYears")) : null },
          district: knownFeature("district"),
          previous_score: { ...knownFeature("previousScore"), value: value("previousScore") ? Number(value("previousScore")) : null },
          career_goal: knownFeature("careerGoal"),
          disability_status: knownFeature("disabilityStatus"),
        },
      });
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 4000);
    } catch (error) {
      setSaveError(error.response?.data?.message || "Could not save your profile. Please try again.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header */}
      <div>
        <div className="flex items-center gap-2 mb-1">
          <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100 flex items-center gap-1.5">
            <User className="w-3.5 h-3.5" /> User Identity & Academic Record
          </span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          My User Profile
        </h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">
          Keep your academic and financial credentials up-to-date to unlock high-accuracy scholarship matches.
        </p>
      </div>

      {/* Profile Overview Card with Donut Score */}
      <Card className="p-6 sm:p-8" hover={false}>
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          {/* Avatar and basic info */}
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-2xl bg-gradient-to-tr from-blue-700 to-indigo-600 text-white flex items-center justify-center font-extrabold text-2xl sm:text-3xl shadow-sm shrink-0">
              {user?.name?.charAt(0).toUpperCase() || "S"}
            </div>

            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <h2 className="text-xl font-bold text-slate-900">
                  {user?.name || "User"}
                </h2>
                <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  Saved User Account
                </span>
              </div>

              <p className="text-xs sm:text-sm text-slate-500 font-medium">
                {user?.email || "Saved account email unavailable"}
              </p>

              <div className="flex items-center gap-2 text-xs text-slate-400 pt-0.5">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-600" />
                <span>Profile data is saved to your account</span>
              </div>
            </div>
          </div>

          {/* Profile Completion Donut Widget */}
          <div className="flex items-center gap-5 bg-slate-50 border border-slate-200/80 rounded-2xl p-4 shrink-0">
            <div className="w-20 h-20">
              <DonutChart
                value={0}
                maxValue={100}
                size={80}
                strokeWidth={10}
                primaryColor="#2563eb"
                centerValue="—"
                centerLabel=""
              />
            </div>

            <div>
              <p className="text-xs font-bold text-slate-800">Profile Completion</p>
              <p className="text-[11px] text-slate-500 mt-0.5">
                Save profile fields to calculate completeness
              </p>
              <span className="inline-block text-[10px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full mt-1.5">
                No estimate shown
              </span>
            </div>
          </div>
        </div>
      </Card>

      {/* Success Notification */}
      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>Profile changes saved successfully! Your AI recommendations have been updated.</span>
        </div>
      )}
      {saveError && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
          <AlertCircle className="w-4 h-4 text-rose-600" />
          <span>{saveError}</span>
        </div>
      )}

      {/* Main Profile Form */}
      <div className="grid grid-cols-4 gap-2" aria-label="Profile setup progress">
        {steps.map((label, index) => {
          const stepNumber = index + 1;
          const active = stepNumber === step;
          const complete = stepNumber < step;
          return (
            <button
              key={label}
              type="button"
              onClick={() => setStep(stepNumber)}
              className={`rounded-xl px-2 py-2 text-[11px] font-bold transition ${
                active ? "bg-blue-600 text-white" : complete ? "bg-emerald-100 text-emerald-700" : "bg-white text-slate-400 border border-slate-200"
              }`}
            >
              <span className="block text-[10px]">{stepNumber}</span>
              {label}
            </button>
          );
        })}
      </div>
      <Card className="p-6 sm:p-8" hover={false}>
        <form onSubmit={handleSubmit} className="space-y-8">
          <p className="text-xs text-slate-500">Complete the fields relevant to your education stage. You can return to any step before saving.</p>
          <fieldset hidden={step !== 1} className="space-y-8">
          {/* 1. Personal & Contact Details */}
          <div>
            <div className="flex items-center gap-2 pb-3 mb-5 border-b border-slate-100">
              <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold">
                1
              </div>
              <h2 className="text-base font-bold text-slate-900">
                Personal & College Details
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-5 text-xs">
              {/* Full Name */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Full Name
                </label>
                <input
                  type="text"
                  name="fullName"
                  placeholder="Enter your full name"
                  defaultValue={user?.name || "User"}
                  className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                />
              </div>

              {/* College */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  College / Institution Name
                </label>
                <input
                  type="text"
                  name="college"
                  placeholder="Enter your college name"
                  defaultValue="Government College of Engineering"
                  className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">Education Stage</label>
                <select name="educationStage" defaultValue="UG" className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer">
                  <option value="SCHOOL">School</option>
                  <option value="DIPLOMA">Diploma</option>
                  <option value="UG">Undergraduate</option>
                  <option value="PG">Postgraduate</option>
                  <option value="PHD_RESEARCH">PhD / Research</option>
                  <option value="PROFESSIONAL">Professional</option>
                  <option value="VOCATIONAL">Vocational</option>
                  <option value="SKILL_DEVELOPMENT">Skill development</option>
                </select>
              </div>
            </div>
          </div>
          </fieldset>

          {/* 2. Academic Enrollment */}
          <fieldset hidden={step !== 2} className="space-y-8">
          <div>
            <div className="flex items-center gap-2 pb-3 mb-5 border-b border-slate-100">
              <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold">
                2
              </div>
              <h2 className="text-base font-bold text-slate-900">
                Academic Program & Standing
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-5 text-xs">
              {/* Branch */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Academic Branch
                </label>
                <select
                  name="course"
                  defaultValue="Artificial Intelligence & Machine Learning"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select Branch</option>
                  <option>Artificial Intelligence & Machine Learning</option>
                  <option>Computer Science</option>
                  <option>Information Technology</option>
                  <option>Electronics & Telecommunication</option>
                  <option>Mechanical Engineering</option>
                  <option>Civil Engineering</option>
                </select>
              </div>

              {/* Year */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Current Year of Study
                </label>
                <select
                  name="yearOfStudy"
                  defaultValue="3rd Year"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select Year</option>
                  <option>1st Year</option>
                  <option>2nd Year</option>
                  <option>3rd Year</option>
                  <option>4th Year</option>
                </select>
              </div>

              {/* CGPA */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Cumulative CGPA (0 - 10)
                </label>
                <input
                  type="number"
                  name="cgpa"
                  step="0.01"
                  min="0"
                  max="10"
                  placeholder="Example: 8.5"
                  defaultValue="8.2"
                  className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                />
              </div>
            </div>
          </div>
          </fieldset>

          {/* 3. User Context & Academic Trend */}
          <fieldset hidden={step !== 3} className="space-y-8">
          <div>
            <div className="flex items-center gap-2 pb-3 mb-5 border-b border-slate-100">
              <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold">
                3
              </div>
              <h2 className="text-base font-bold text-slate-900">
                User Context & Academic Trend
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5 text-xs">
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">Age</label>
                <input name="ageYears" type="number" min="5" max="100" placeholder="Example: 20" className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition" />
              </div>
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">District</label>
                <input name="district" type="text" placeholder="Enter your district" className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition" />
              </div>
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">Previous Score (%)</label>
                <input name="previousScore" type="number" min="0" max="100" step="0.01" placeholder="Example: 78" className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition" />
              </div>
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">Career Goal</label>
                <input name="careerGoal" type="text" placeholder="Example: Data Scientist" className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition" />
              </div>
            </div>
          </div>
          </fieldset>

          {/* 4. Financial & Demographic Details */}
          <fieldset hidden={step !== 4} className="space-y-8">
          <div>
            <div className="flex items-center gap-2 pb-3 mb-5 border-b border-slate-100">
              <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold">
                4
              </div>
              <h2 className="text-base font-bold text-slate-900">
                Financial, Reservation & Domicile
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-5 text-xs">
              {/* Family Income */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Annual Family Income (INR)
                </label>
                <input
                  type="number"
                  name="familyIncome"
                  placeholder="Example: 250000"
                  defaultValue="300000"
                  className="w-full bg-slate-50/50 border border-slate-200/90 rounded-xl px-4 py-2.5 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                />
              </div>

              {/* Category */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Social Category
                </label>
                <select
                  name="category"
                  defaultValue="OBC"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select Category</option>
                  <option>Open</option>
                  <option>OBC</option>
                  <option>SC</option>
                  <option>ST</option>
                  <option>EWS</option>
                  <option>Other</option>
                </select>
              </div>

              {/* State */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Permanent Domicile State
                </label>
                <select
                  name="state"
                  defaultValue="Maharashtra"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select State</option>
                  <option>Maharashtra</option>
                  <option>Karnataka</option>
                  <option>Gujarat</option>
                  <option>Goa</option>
                  <option>Madhya Pradesh</option>
                  <option>Other</option>
                </select>
              </div>

              {/* Gender */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Gender
                </label>
                <select
                  name="gender"
                  defaultValue="Male"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select Gender</option>
                  <option>Male</option>
                  <option>Female</option>
                  <option>Other</option>
                  <option>Prefer not to say</option>
                </select>
              </div>

              {/* Disability */}
              <div>
                <label className="block font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Disability Status (PwD)
                </label>
                <select
                  name="disabilityStatus"
                  defaultValue="No"
                  className="w-full bg-white border border-slate-200/90 rounded-xl px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition cursor-pointer"
                >
                  <option value="">Select Status</option>
                  <option>No</option>
                  <option>Yes</option>
                </select>
              </div>
            </div>
          </div>
          </fieldset>

          {/* Save Button */}
          <div className="flex items-center justify-end pt-5 border-t border-slate-100">
            <div className="flex w-full items-center justify-between gap-3">
              <Button type="button" size="md" variant="secondary" disabled={step === 1 || isSaving} onClick={() => setStep((current) => current - 1)}>
                Back
              </Button>
              {step < steps.length ? (
                <Button type="button" size="md" onClick={() => setStep((current) => current + 1)}>
                  Next: {steps[step]}
                </Button>
              ) : (
                <Button type="submit" size="md" disabled={isSaving}>
                  {isSaving ? "Saving..." : "Save Profile Credentials"}
                </Button>
              )}
            </div>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default Profile;
