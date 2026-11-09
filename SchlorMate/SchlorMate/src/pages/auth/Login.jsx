import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Check, Eye, EyeOff, GraduationCap, LockKeyhole, Mail, Phone, Search, Sparkles } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { loginUser } from "../../services/api";

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(true);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault(); setError(""); setIsSubmitting(true);
    try { const response = await loginUser(email, password); login(response); navigate("/dashboard"); }
    catch (requestError) { setError(requestError.message || "Unable to sign in."); }
    finally { setIsSubmitting(false); }
  };

  return (
    <main className="auth-shell login-shell">
      <div className="auth-orb auth-orb-one" /><div className="auth-orb auth-orb-two" />
      <header className="auth-nav">
        <Link to="/" className="brand-mark" aria-label="ScholarMate home"><span className="brand-icon"><GraduationCap size={22} /></span><span>Scholar<span>Mate</span></span></Link>
        <nav className="auth-links" aria-label="Main navigation"><a href="#home">Home</a><a href="#features">Features</a><a href="#scholarships">Scholarships</a><a href="#institutions">For Institutions</a><a href="#about">About</a></nav>
        <div className="auth-nav-actions"><button className="search-button" type="button" aria-label="Search"><Search size={19} /></button><span className="signin-copy">Don't have an account?</span><Link to="/register" className="filled-button">Sign Up</Link></div>
      </header>
      <section className="auth-content login-content" id="home">
        <div className="story-panel login-story">
          <div className="eyebrow"><span>Welcome back</span><span className="eyebrow-spark">✦</span></div>
          <h1>Welcome<br /><em>Back!</em></h1>
          <p className="story-copy">Continue your journey towards a brighter future with ScholarMate.</p>
          <div className="benefit-list" id="features">
            <div className="benefit-item"><span className="benefit-icon teal"><GraduationCap size={20} /></span><span><strong>Find new</strong><small>opportunities</small></span></div>
            <div className="benefit-item"><span className="benefit-icon orange"><Sparkles size={20} /></span><span><strong>Track your</strong><small>applications</small></span></div>
            <div className="benefit-item"><span className="benefit-icon gold">☆</span><span><strong>Get AI-powered</strong><small>guidance</small></span></div>
          </div>
          <div className="dream-note login-note"><span>Same Dreams<br />Brighter Futures</span><b>↗</b></div>
          <div className="illustration login-illustration" aria-hidden="true"><div className="sun-disc" /><img className="real-hero-image" style={{ width: "76%", height: "84%", objectFit: "cover", objectPosition: "center top", position: "absolute", left: "13%", bottom: "-5%", borderRadius: "46% 46% 7% 7%", transform: "rotate(3deg)", zIndex: 2, boxShadow: "0 18px 30px rgba(36, 71, 54, .22)" }} src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=900&q=85" alt="Students walking across campus" /><div className="polaroid polaroid-one"><span>More Opportunities<br />Brighter Tomorrows</span><b>✦</b></div><div className="paper-plane">➤</div></div>
        </div>
        <div className="register-card-wrap"><div className="register-card login-card"><div className="card-decoration">✦</div><div className="card-cap"><GraduationCap size={34} /></div><div className="card-heading"><h2>Sign In to <span>ScholarMate</span></h2><p>Access your dashboard and continue exploring scholarships, applications and more.</p></div>
          {error && <div className="form-error" role="alert">{error}</div>}
          <form onSubmit={handleSubmit} className="signup-form login-form">
            <label>Email Address<div className="input-wrap"><Mail size={17} /><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="yourname@example.com" required /></div></label>
            <label><span className="label-row">Password <Link to="/forgot-password">Forgot password?</Link></span><div className="input-wrap"><LockKeyhole size={17} /><input type={showPassword ? "text" : "password"} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" required /><button type="button" className="password-toggle" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? "Hide password" : "Show password"}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div></label>
            <label className="consent remember"><button type="button" className={`check-box ${remember ? "checked" : ""}`} onClick={() => setRemember((value) => !value)} aria-label="Remember me">{remember && <Check size={13} strokeWidth={3} />}</button><span>Remember me</span></label>
            <button className="primary-submit" type="submit" disabled={isSubmitting}>{isSubmitting ? "Signing in..." : "Sign In"}<ArrowRight size={19} /></button>
          </form>
          <div className="social-divider"><span>or continue with</span></div><div className="social-buttons"><button type="button"><b className="google-g">G</b>Google</button><button type="button"><b className="microsoft-grid">⊞</b>Microsoft</button><button type="button"><Phone size={16} />Phone</button></div>
          <p className="card-footer-copy">Don't have an account? <Link to="/register">Sign Up <ArrowRight size={13} /></Link></p>
        </div></div>
      </section>
    </main>
  );
}
export default Login;
