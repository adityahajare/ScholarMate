import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, Check, Eye, EyeOff, GraduationCap, LockKeyhole, Mail, Search } from "lucide-react";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");

  const handleEmailSubmit = (event) => { event.preventDefault(); setError(""); setSent(true); };
  const handleResetSubmit = (event) => {
    event.preventDefault();
    if (newPassword.length < 6 || newPassword !== confirmPassword) { setError("Use at least 6 characters and make sure both passwords match."); return; }
    setError("Password updated. You can now sign in.");
  };

  return (
    <main className="auth-shell recovery-shell">
      <div className="auth-orb auth-orb-one" /><div className="auth-orb auth-orb-two" />
      <header className="auth-nav"><Link to="/" className="brand-mark"><span className="brand-icon"><GraduationCap size={22} /></span><span>Scholar<span>Mate</span></span></Link><nav className="auth-links"><a href="#home">Home</a><a href="#features">Features</a><a href="#scholarships">Scholarships</a><a href="#institutions">For Institutions</a><a href="#about">About</a></nav><div className="auth-nav-actions"><button className="search-button" type="button" aria-label="Search"><Search size={19} /></button><Link to="/login" className="outline-button">Sign In</Link><Link to="/register" className="filled-button">Sign Up</Link></div></header>
      <section className="recovery-content" id="home"><div className="recovery-note note-left">Same Dreams<br />Brighter Futures <b>✦</b></div><div className="recovery-note note-right">You’re One Step<br />Closer to New<br />Opportunities <b>✧</b></div><div className="recovery-plane">➤</div>
        <div className="recovery-card"><Link to="/login" className="back-link"><ArrowLeft size={15} /> Back to Login</Link><div className="recovery-icon">{sent ? <LockKeyhole size={29} /> : <LockKeyhole size={29} />}</div>{!sent ? <><h1>Forgot <em>Password?</em></h1><p>Enter your registered email address and we’ll send you a reset link.</p><form onSubmit={handleEmailSubmit} className="signup-form"><label>Email Address<div className="input-wrap"><Mail size={17} /><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="yourname@example.com" required /></div></label><button className="primary-submit" type="submit">Send Reset Link <ArrowRight size={19} /></button></form><p className="recovery-footer">Remember your password? <Link to="/login">Sign In</Link></p></> : <><h1>Create a New <em>Password</em></h1><p>Your new password should be strong and easy to remember.</p>{error && <div className={`form-error ${error.startsWith("Password updated") ? "success-message" : ""}`}>{error}</div>}<form onSubmit={handleResetSubmit} className="signup-form"><label>New Password<div className="input-wrap"><LockKeyhole size={17} /><input type={showPassword ? "text" : "password"} value={newPassword} onChange={(event) => setNewPassword(event.target.value)} placeholder="Enter a new password" required /><button type="button" className="password-toggle" onClick={() => setShowPassword((value) => !value)}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div></label><div className="password-strength"><span /><span /><span /><i>Strong</i></div><div className="password-rules"><span><Check size={13} /> At least 6 characters</span><span><Check size={13} /> Include a number</span><span><Check size={13} /> Include a special character</span></div><label>Confirm New Password<div className="input-wrap"><LockKeyhole size={17} /><input type={showPassword ? "text" : "password"} value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} placeholder="Confirm your password" required /></div></label><button className="primary-submit" type="submit">Reset Password <ArrowRight size={19} /></button></form></>}</div>
      </section>
    </main>
  );
}
export default ForgotPassword;
