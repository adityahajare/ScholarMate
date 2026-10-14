import { useState } from "react";
import { GraduationCap, LogOut, Menu, User } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function Navbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const signOut = () => { logout(); navigate("/login"); };
  return <header className="sticky top-0 z-30 h-16 bg-white/95 backdrop-blur-md border-b border-slate-200/80 px-4 sm:px-6 flex items-center justify-between"><div className="flex items-center gap-3"><button onClick={onMenuClick} className="md:hidden p-2 rounded-xl text-slate-600 hover:bg-slate-100" aria-label="Open navigation menu"><Menu className="w-5 h-5" /></button><div className="flex items-center gap-2.5"><div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white"><GraduationCap className="w-5 h-5" /></div><span className="text-lg font-bold text-slate-900">Scholar<span className="text-blue-600">Mate</span></span></div></div><div className="relative"><button onClick={() => setOpen(!open)} className="flex items-center gap-2 rounded-xl px-3 py-2 hover:bg-slate-100"><User className="w-4 h-4 text-slate-500" /><span className="hidden sm:block text-sm font-semibold text-slate-700">{user?.name || user?.email || "User"}</span></button>{open && <div className="absolute right-0 mt-2 w-48 rounded-xl border border-slate-200 bg-white p-2 shadow-lg"><button onClick={() => { setOpen(false); navigate("/profile"); }} className="w-full flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-left hover:bg-slate-50"><User className="w-4 h-4" /> User Profile</button><button onClick={signOut} className="w-full flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-left text-rose-600 hover:bg-rose-50"><LogOut className="w-4 h-4" /> Sign out</button></div>}</div></header>;
}
export default Navbar;
