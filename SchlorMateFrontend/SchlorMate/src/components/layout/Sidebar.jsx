import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  GraduationCap,
  Sparkles,
  Target,
  TrendingUp,
  FileText,
  ClipboardList,
  BarChart3,
  Bell,
  Settings,
  X,
} from "lucide-react";

function Sidebar({ isOpen, setIsOpen }) {
  const navItems = [
    { name: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
    { name: "Scholarships", path: "/scholarships", icon: GraduationCap },
    { name: "AI Recommendations", path: "/recommendations", icon: Sparkles },
    { name: "Success Prediction", path: "/prediction", icon: Target },
    { name: "Profile Improvement", path: "/profile/builder", icon: TrendingUp },
    { name: "Documents", path: "/documents", icon: FileText },
    { name: "My Applications", path: "/applications", icon: ClipboardList },
    { name: "Reports & Insights", path: "/reports", icon: BarChart3 },
    { name: "Notifications", path: "/notifications", icon: Bell },
    { name: "Settings", path: "/settings", icon: Settings },
  ];

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs z-40 md:hidden transition-opacity"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`
          fixed md:static
          top-0 left-0
          z-50
          w-64
          h-screen
          bg-[#18133f]
          text-slate-200
          flex flex-col
          shrink-0
          transition-transform
          duration-300 ease-in-out
          ${isOpen ? "translate-x-0" : "-translate-x-full md:translate-x-0"}
        `}
      >
        {/* Header / Brand */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-[#6355ff] to-[#4838e8] flex items-center justify-center text-white shadow-md shadow-indigo-900/40">
              <GraduationCap className="w-5 h-5" />
            </div>
            <span className="text-lg font-bold text-white tracking-tight">
              Scholar<span className="text-[#8479ff]">Mate</span>
            </span>
          </div>

          <button
            onClick={() => setIsOpen(false)}
            className="md:hidden p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition"
            aria-label="Close sidebar"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation list */}
        <nav className="flex-1 px-3 py-4 overflow-y-auto space-y-1 scrollbar-thin scrollbar-thumb-white/10">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setIsOpen(false)}
                className={({ isActive }) => `
                  flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-150
                  ${
                    isActive
                      ? "bg-[#544af4] text-white shadow-sm shadow-indigo-600/30 font-bold"
                      : "text-slate-300 hover:text-white hover:bg-white/8"
                  }
                `}
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span className="truncate">{item.name}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Footer info in sidebar */}
        <div className="p-4 border-t border-white/10 shrink-0">
          <div className="rounded-xl bg-white/5 p-3 text-center">
            <p className="text-[11px] font-medium text-slate-300">Need Assistance?</p>
            <a
              href="mailto:support@scholarmate.in"
              className="mt-1 block text-[11px] font-bold text-[#9d94ff] hover:underline"
            >
              Contact Support
            </a>
          </div>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
