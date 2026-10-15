import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GraduationCap, Eye, EyeOff, ShieldCheck, UserCheck } from "lucide-react";
import { authApi } from "../../lib/api";
import { useAuth } from "../../context/AuthContext";

export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({
    name: "",
    email: "",
    mobile: "",
    password: "",
    confirmPassword: "",
    educationType: "UNDERGRADUATE",
    educationLevel: "First Year (UG)",
    course: "Computer Engineering",
    state: "Maharashtra",
    isUnder18: false,
    guardianName: "",
    guardianPhone: "",
    termsAgreed: true,
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => {
    const val = e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [key]: val }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.termsAgreed) {
      return setError("Please agree to the Terms & Conditions and Privacy Policy to proceed.");
    }
    if (form.password !== form.confirmPassword) {
      return setError("Passwords do not match.");
    }
    if (form.password.length < 6) {
      return setError("Password must be at least 6 characters.");
    }
    if (form.isUnder18 && (!form.guardianName.trim() || !form.guardianPhone.trim())) {
      return setError("Guardian name and contact phone are required for students under 18 years old.");
    }

    setSubmitting(true);
    try {
      const { data } = await authApi.register({
        name: form.name.trim(),
        email: form.email.toLowerCase().trim(),
        mobile: form.mobile.trim(),
        password: form.password,
        educationType: form.educationType,
        educationLevel: form.educationLevel,
        course: form.course.trim(),
        state: form.state,
        isUnder18: form.isUnder18,
        guardianName: form.isUnder18 ? form.guardianName.trim() : null,
        guardianPhone: form.isUnder18 ? form.guardianPhone.trim() : null,
        termsAccepted: form.termsAgreed,
        role: "STUDENT",
      });

      login(data);
      navigate("/dashboard");
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
        requestError.response?.data?.error ||
        "Unable to create account. Please check your details and try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const indianStates = [
    "Andhra Pradesh", "Assam", "Bihar", "Chhattisgarh", "Delhi", "Gujarat",
    "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka",
    "Kerala", "Madhya Pradesh", "Maharashtra", "Odisha", "Punjab", "Rajasthan",
    "Tamil Nadu", "Telangana", "Uttar Pradesh", "Uttarakhand", "West Bengal", "All India / Other"
  ];

  return (
    <div className="min-h-screen bg-[#f8f9fe] flex flex-col justify-center items-center py-12 px-4 sm:px-6 font-sans">
      <div className="max-w-xl w-full">
        {/* Brand Header */}
        <div className="text-center mb-6">
          <Link to="/" className="inline-flex items-center gap-2 mb-4">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#6355ff] to-[#4838e8] flex items-center justify-center text-white shadow-md shadow-indigo-600/30">
              <GraduationCap className="w-6 h-6" />
            </div>
            <span className="text-2xl font-black text-slate-900 tracking-tight">
              Scholar<span className="text-[#544af4]">Mate</span>
            </span>
          </Link>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Create Student Account</h1>
          <p className="text-xs text-slate-500 mt-1">Unlock verified scholarships tailored for your education</p>
        </div>

        {/* Form Card */}
        <div className="bg-white rounded-3xl border border-slate-200/90 shadow-xl p-8 sm:p-10">
          {error && (
            <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-semibold text-rose-700">
              {String(error)}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Full Name & Mobile */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Full Name *</label>
                <input
                  type="text"
                  required
                  value={form.name}
                  onChange={update("name")}
                  placeholder="e.g. Aditya Hajare"
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Mobile Number *</label>
                <input
                  type="tel"
                  required
                  value={form.mobile}
                  onChange={update("mobile")}
                  placeholder="e.g. 9876543210"
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10"
                />
              </div>
            </div>

            {/* Email Address */}
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Email Address *</label>
              <input
                type="email"
                required
                value={form.email}
                onChange={update("email")}
                placeholder="aditya@example.com"
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10"
              />
            </div>

            {/* Passwords with Show/Hide */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Password *</label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    value={form.password}
                    onChange={update("password")}
                    placeholder="Min. 6 characters"
                    className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 pr-9"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Confirm Password *</label>
                <div className="relative">
                  <input
                    type={showConfirmPassword ? "text" : "password"}
                    required
                    value={form.confirmPassword}
                    onChange={update("confirmPassword")}
                    placeholder="Re-enter password"
                    className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 pr-9"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    {showConfirmPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>
            </div>

            {/* Education Type & Level */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Education Type *</label>
                <select
                  value={form.educationType}
                  onChange={update("educationType")}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 text-slate-700"
                >
                  <option value="HIGHER_SECONDARY">Higher Secondary (11th/12th)</option>
                  <option value="DIPLOMA">Diploma / Polytechnic</option>
                  <option value="UNDERGRADUATE">Undergraduate (B.Tech, BSc, BA, etc.)</option>
                  <option value="POSTGRADUATE">Postgraduate (Masters, MBA, etc.)</option>
                  <option value="PHD">PhD / Doctoral Research</option>
                  <option value="VOCATIONAL">Vocational Training</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Education Level / Year *</label>
                <select
                  value={form.educationLevel}
                  onChange={update("educationLevel")}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 text-slate-700"
                >
                  <option value="11th Standard">11th Standard</option>
                  <option value="12th Standard">12th Standard</option>
                  <option value="First Year (UG)">First Year (UG / Diploma)</option>
                  <option value="Second Year (UG)">Second Year (UG / Diploma)</option>
                  <option value="Third Year (UG)">Third Year (UG / Diploma)</option>
                  <option value="Final Year (UG)">Final Year (UG)</option>
                  <option value="First Year (PG)">First Year (PG)</option>
                  <option value="Final Year (PG)">Final Year (PG)</option>
                  <option value="Doctoral Candidate">Doctoral Candidate</option>
                </select>
              </div>
            </div>

            {/* Course & State */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Course / Stream *</label>
                <input
                  type="text"
                  required
                  value={form.course}
                  onChange={update("course")}
                  placeholder="e.g. Computer Science, Commerce"
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Domicile State *</label>
                <select
                  value={form.state}
                  onChange={update("state")}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 text-slate-700"
                >
                  {indianStates.map((st) => (
                    <option key={st} value={st}>{st}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Guardian Mode Checkbox for Under 18 */}
            <div className="pt-1">
              <label className="flex items-center gap-2 cursor-pointer text-xs text-slate-700 bg-slate-50 p-2.5 rounded-xl border border-slate-200/80">
                <input
                  type="checkbox"
                  checked={form.isUnder18}
                  onChange={update("isUnder18")}
                  className="w-4 h-4 rounded text-[#544af4] border-slate-300 focus:ring-indigo-500"
                />
                <span className="font-semibold text-slate-800">
                  Student is under 18 years old (Requires Guardian Consent)
                </span>
              </label>
            </div>

            {/* Conditional Guardian Fields */}
            {form.isUnder18 && (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 p-3 rounded-2xl bg-indigo-50/50 border border-indigo-100">
                <div>
                  <label className="block text-xs font-bold text-indigo-900 mb-1">Guardian / Parent Name *</label>
                  <input
                    type="text"
                    required={form.isUnder18}
                    value={form.guardianName}
                    onChange={update("guardianName")}
                    placeholder="Parent or Guardian name"
                    className="w-full rounded-xl border border-indigo-200 bg-white px-3.5 py-2 text-xs outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-indigo-900 mb-1">Guardian Mobile Number *</label>
                  <input
                    type="tel"
                    required={form.isUnder18}
                    value={form.guardianPhone}
                    onChange={update("guardianPhone")}
                    placeholder="Parent or Guardian phone"
                    className="w-full rounded-xl border border-indigo-200 bg-white px-3.5 py-2 text-xs outline-none focus:border-indigo-500"
                  />
                </div>
              </div>
            )}

            {/* Terms and Consent Checkbox */}
            <div className="pt-1">
              <label className="flex items-start gap-2 cursor-pointer text-xs text-slate-600">
                <input
                  type="checkbox"
                  checked={form.termsAgreed}
                  onChange={update("termsAgreed")}
                  className="w-4 h-4 rounded text-[#544af4] border-slate-300 focus:ring-indigo-500 mt-0.5"
                />
                <span>
                  I agree to the{" "}
                  <span className="text-[#544af4] font-semibold">Terms & Conditions</span>,{" "}
                  <span className="text-[#544af4] font-semibold">DPDP Consent</span>, and{" "}
                  <span className="text-[#544af4] font-semibold">Privacy Policy</span>.
                </span>
              </label>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full mt-2 py-3 px-4 rounded-xl bg-[#544af4] hover:bg-[#4438e8] text-white font-bold text-sm shadow-md shadow-indigo-600/25 transition disabled:opacity-50 cursor-pointer"
            >
              {submitting ? "Creating Verified Account..." : "Create Account"}
            </button>
          </form>

          <p className="mt-5 text-center text-xs text-slate-500">
            Already have an account?{" "}
            <Link to="/login" className="font-bold text-[#544af4] hover:underline">
              Login
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
