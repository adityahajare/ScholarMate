import { ClipboardList, ExternalLink, FileText, Search } from "lucide-react";
import { Link } from "react-router-dom";
import Card from "../../components/common/Card";
import EmptyState from "../../components/common/EmptyState";

function Applications() {
  return (
    <div className="space-y-6 animate-fadeIn">
      <div>
        <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100 inline-flex items-center gap-1.5">
          <ClipboardList className="w-3.5 h-3.5" /> User Application Tracking
        </span>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">My Applications</h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">Applications will appear here after they are submitted through ScholarMate.</p>
      </div>

      <Card className="p-6 sm:p-8" hover={false}>
        <EmptyState
          icon={Search}
          title="No live applications yet"
          message="The application tracker is ready, but no application records are available for this User account yet. Start with a verified scholarship and return here to track its status."
          buttonText="Explore Scholarships"
          onButtonClick={() => { window.location.href = "/scholarships"; }}
        />
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="p-5" hover={false}><FileText className="w-5 h-5 text-blue-600 mb-3" /><p className="text-sm font-bold text-slate-900">Submit from verified listings</p><p className="text-xs text-slate-500 mt-1">Use live scholarship details before starting an application.</p></Card>
        <Card className="p-5" hover={false}><ClipboardList className="w-5 h-5 text-emerald-600 mb-3" /><p className="text-sm font-bold text-slate-900">Keep your profile current</p><p className="text-xs text-slate-500 mt-1">Eligibility changes when your academic or financial data changes.</p></Card>
        <Card className="p-5" hover={false}><ExternalLink className="w-5 h-5 text-indigo-600 mb-3" /><p className="text-sm font-bold text-slate-900">Track official portals</p><p className="text-xs text-slate-500 mt-1">ScholarMate links you to the official application destination.</p></Card>
      </div>

      <Link to="/scholarships" className="inline-flex text-sm font-bold text-blue-600 hover:text-blue-700">Browse scholarships →</Link>
    </div>
  );
}

export default Applications;
