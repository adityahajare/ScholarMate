import { useState } from "react";
import { Link } from "react-router-dom";
import { GraduationCap, ArrowLeft, Mail, CheckCircle2 } from "lucide-react";
import { authApi } from "../../lib/api";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const { data } = await authApi.forgotPassword(email);
      setMessage(data.message || "If an account with that email exists, password reset instructions have been sent.");
      setSubmitted(true);
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
        "Unable to send password reset request. Please check your email."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f8f9fe] flex flex-col justify-center items-center py-12 px-4 sm:px-6 font-sans">
      <div className="max-w-md w-full">
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
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Forgot Password</h1>
          <p className="text-xs text-slate-500 mt-1">
            Enter your registered email and we will send you password reset instructions.
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-3xl border border-slate-200/90 shadow-xl p-8 sm:p-10">
          {error && (
            <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-semibold text-rose-700">
              {String(error)}
            </div>
          )}

          {submitted ? (
            <div className="text-center space-y-4 py-4">
              <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <h2 className="text-base font-bold text-slate-900">Request Received</h2>
              <p className="text-xs text-slate-600 leading-relaxed">{message}</p>
              <div className="pt-4 border-t border-slate-100 flex flex-col gap-2">
                <Link
                  to="/reset-password"
                  className="w-full py-2.5 px-4 rounded-xl bg-[#544af4] hover:bg-[#4438e8] text-white font-bold text-xs shadow-md shadow-indigo-600/25 transition text-center"
                >
                  Have a reset token? Enter it here
                </Link>
                <Link
                  to="/login"
                  className="inline-flex items-center justify-center gap-1.5 text-xs font-bold text-slate-600 hover:text-slate-900 py-2"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back to Login
                </Link>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1.5">
                  Registered Email Address
                </label>
                <div className="relative">
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="student@example.com"
                    className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs sm:text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10"
                  />
                  <Mail className="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                </div>
              </div>

              <button
                type="submit"
                disabled={submitting}
                className="w-full mt-2 py-3 px-4 rounded-xl bg-[#544af4] hover:bg-[#4438e8] text-white font-bold text-sm shadow-md shadow-indigo-600/25 transition disabled:opacity-50 cursor-pointer"
              >
                {submitting ? "Sending Request..." : "Send Reset Instructions"}
              </button>

              <div className="text-center pt-3">
                <Link
                  to="/login"
                  className="inline-flex items-center gap-1 text-xs font-bold text-slate-600 hover:text-slate-900"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back to Login
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
