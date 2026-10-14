import { ArrowLeft, AlertCircle } from "lucide-react";
import { useNavigate } from "react-router-dom";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";

function ApplicationDetails() {
  const navigate = useNavigate();
  return <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn"><Button variant="outline" size="sm" onClick={() => navigate("/applications")} icon={ArrowLeft}>Back to Applications</Button><Card className="p-6 sm:p-8" hover={false}><div className="flex items-start gap-3 text-slate-700"><AlertCircle className="w-5 h-5 text-amber-600 shrink-0" /><div><h1 className="text-xl font-bold text-slate-900">Application record unavailable</h1><p className="text-sm mt-2">ScholarMate does not submit official applications. Details appear here only after the backend stores an application record.</p><p className="text-xs text-slate-500 mt-3">No scholarship name, status, date, documents, or reference number is fabricated.</p></div></div></Card></div>;
}
export default ApplicationDetails;
