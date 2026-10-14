import { AlertCircle } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";

function ApplicationSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const application = location.state;
  return <div className="min-h-[75vh] flex items-center justify-center py-6 px-4"><Card className="p-6 sm:p-10 max-w-2xl w-full text-center" hover={false}><AlertCircle className="w-10 h-10 mx-auto text-amber-600" /><h1 className="text-2xl font-extrabold text-slate-900 mt-4">Official application status</h1>{application ? <p className="text-sm text-slate-600 mt-3">The official portal returned this response: {application.message || "No tracking record was supplied."}</p> : <p className="text-sm text-slate-600 mt-3">No submitted application response is stored. ScholarMate does not submit official applications or invent submission receipts.</p>}<Button className="mt-6" onClick={() => navigate("/scholarships")}>Browse stored scholarships</Button></Card></div>;
}
export default ApplicationSuccess;
