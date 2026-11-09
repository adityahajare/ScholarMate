import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Check, Eye, EyeOff, GraduationCap, Landmark, LockKeyhole, Mail, Phone, Search, Sparkles, UserRound } from "lucide-react";
import { registerUser } from "../../services/api";
import { useAuth } from "../../context/AuthContext";

const roles = [
  { id: "student", label: "Student", icon: GraduationCap },
  { id: "parent", label: "Parent", icon: UserRound },
  { id: "institution", label: "Institution", icon: Landmark },
];
const benefits = [
  { icon: Search, label: "Personalized", detail: "Recommendations", tone: "orange" },
  { icon: Sparkles, label: "AI Application", detail: "Support", tone: "teal" },
  { icon: GraduationCap, label: "Document", detail: "Assistance", tone: "blue" },
  { icon: Phone, label: "Deadline", detail: "Reminders", tone: "gold" },
];

function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [role, setRole] = useState("student");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [mobile, setMobile] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [accepted, setAccepted] = useState(false);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!accepted) { setError("Please accept the Terms of Service and Privacy Policy to continue."); return; }
    setError(""); setIsSubmitting(true);
    try {
      const response = await registerUser({ name: fullName, email, password, mobile, role });
      login(response); navigate("/dashboard");
    } catch (requestError) { setError(requestError.message || "Unable to create your account."); }
    finally { setIsSubmitting(false); }
  };

  return (
    <main className="auth-shell">
      <div className="auth-orb auth-orb-one" /><div className="auth-orb auth-orb-two" />
      <header className="auth-nav">
        <Link to="/" className="brand-mark" aria-label="ScholarMate home"><span className="brand-icon"><GraduationCap size={22} /></span><span>Scholar<span>Mate</span></span></Link>
        <nav className="auth-links" aria-label="Main navigation"><a href="#home">Home</a><a href="#features">Features</a><a href="#scholarships">Scholarships</a><a href="#institutions">For Institutions</a><a href="#about">About</a></nav>
        <div className="auth-nav-actions"><button className="search-button" type="button" aria-label="Search"><Search size={19} /></button><span className="signin-copy">Already have an account?</span><Link to="/login" className="outline-button">Sign In</Link></div>
      </header>

      <section className="auth-content" id="home">
        <div className="story-panel">
          <div className="eyebrow"><span>Join ScholarMate</span><span className="eyebrow-spark">✦</span></div>
          <h1>One Step Closer<br />to <em>Your Dreams</em></h1>
          <p className="story-copy">Create your account and get access to personalized scholarships, AI support and a brighter future.</p>
          <div className="benefit-list" id="features">{benefits.map(({ icon: Icon, label, detail, tone }, index) => <div className="benefit-item" style={{ "--delay": `${index * 90}ms` }} key={label}><span className={`benefit-icon ${tone}`}><Icon size={20} /></span><span><strong>{label}</strong><small>{detail}</small></span></div>)}</div>
          <div className="dream-note"><span>Same Dreams<br />Different Paths</span><b>↗</b><span>One Platform</span></div>
          <div className="student-proof"><div className="avatar-stack"><i>AM</i><i>RK</i><i>PS</i><i>+</i></div><span><strong>50,000+ students</strong><small>already exploring opportunities with ScholarMate</small></span></div>
          <div className="illustration" aria-hidden="true"><div className="sun-disc" /><img className="real-hero-image" style={{ width: "72%", height: "78%", objectFit: "cover", position: "absolute", left: "17%", bottom: "-4%", borderRadius: "48% 48% 8% 8%", transform: "rotate(-4deg)", zIndex: 2, boxShadow: "0 18px 30px rgba(36, 71, 54, .22)" }} src="https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=900&q=85" alt="Students exploring campus" /><div className="polaroid polaroid-one"><span>Better<br />Education</span><b>✦</b></div><div className="polaroid polaroid-two"><span>New<br />Opportunities</span><b>✧</b></div><div className="paper-plane">➤</div></div>
        </div>

        <div className="register-card-wrap"><div className="register-card"><div className="card-decoration">✦</div><div className="card-cap"><GraduationCap size={34} /></div><div className="card-heading"><h2>Create Your Account</h2><p>It's quick, free and takes less than 2 minutes.</p></div>
          <div className="role-tabs" role="tablist" aria-label="Account type">{roles.map(({ id, label, icon: Icon }) => <button type="button" role="tab" aria-selected={role === id} className={role === id ? "active" : ""} onClick={() => setRole(id)} key={id}><Icon size={18} />{label}</button>)}</div>
          {error && <div className="form-error" role="alert">{error}</div>}
          <form onSubmit={handleSubmit} className="signup-form">
            <label>Full Name<div className="input-wrap"><UserRound size={17} /><input value={fullName} onChange={(event) => setFullName(event.target.value)} placeholder="Enter your full name" required /></div></label>
            <label>Email Address<div className="input-wrap"><Mail size={17} /><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="yourname@example.com" required /></div></label>
            <label>Mobile Number<div className="input-wrap phone-input"><span className="country-code">🇮🇳 <b>+91</b></span><input type="tel" value={mobile} onChange={(event) => setMobile(event.target.value)} placeholder="Enter mobile number" /></div></label>
            <label>Create Password<div className="input-wrap"><LockKeyhole size={17} /><input type={showPassword ? "text" : "password"} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Create a strong password" minLength={6} required /><button type="button" className="password-toggle" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? "Hide password" : "Show password"}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div></label>
            <label className="consent"><button type="button" className={`check-box ${accepted ? "checked" : ""}`} onClick={() => setAccepted((value) => !value)} aria-label="Accept terms">{accepted && <Check size={13} strokeWidth={3} />}</button><span>I agree to the <a href="#terms">Terms of Service</a> and <a href="#privacy">Privacy Policy</a></span></label>
            <button className="primary-submit" type="submit" disabled={isSubmitting}>{isSubmitting ? "Creating account..." : "Create Account"}<ArrowRight size={19} /></button>
          </form>
          <div className="social-divider"><span>or continue with</span></div><div className="social-buttons"><button type="button"><b className="google-g">G</b>Google</button><button type="button"><b className="microsoft-grid">⊞</b>Microsoft</button><button type="button"><Phone size={16} />Phone</button></div>
        </div></div>
      </section>
    </main>
  );
}
export default Register;
