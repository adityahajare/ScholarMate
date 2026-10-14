import { useEffect, useState } from "react";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import ErrorState from "../../components/common/ErrorState";
import { scholarshipApi } from "../../lib/api";

function ScholarshipDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [scholarship, setScholarship] = useState(null);
  const [error, setError] = useState("");
  useEffect(() => { scholarshipApi.get(id).then(({ data }) => setScholarship(data)).catch((e) => setError(e.response?.data?.message || "This scholarship is not available in the stored catalog.")); }, [id]);
  if (!scholarship && !error) return <Loading message="Loading saved scholarship..." />;
  if (error) return <ErrorState title="Scholarship unavailable" message={error} onRetry={() => window.location.reload()} />;
  return <div className="space-y-6 animate-fadeIn"><Button variant="outline" size="sm" onClick={() => navigate("/scholarships")} icon={ArrowLeft}>Back to Scholarships</Button><Card className="p-6 sm:p-8" hover={false}><div className="flex flex-col md:flex-row md:justify-between gap-4"><div><span className="text-xs font-bold text-blue-700">{scholarship.category || "Unclassified"}</span><h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2">{scholarship.name}</h1><p className="text-sm text-slate-500 mt-1">{scholarship.provider || "Stored provider not specified"}</p></div>{scholarship.applicationDeadline && <div className="text-sm text-slate-600">Deadline: <strong>{scholarship.applicationDeadline}</strong></div>}</div><p className="mt-6 text-sm leading-relaxed text-slate-700">{scholarship.description || "No description was returned by the source."}</p><div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-6 text-sm"><Info label="Amount" value={scholarship.amount == null ? "Not specified" : `₹${Number(scholarship.amount).toLocaleString("en-IN")}`} /><Info label="Course" value={scholarship.course || "Not specified"} /><Info label="State" value={scholarship.state || "Not specified"} /><Info label="Minimum CGPA" value={scholarship.minimumCgpa ?? "Not specified"} /></div><div className="flex flex-wrap gap-3 mt-6">{scholarship.applicationUrl && <a href={scholarship.applicationUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-bold text-white">Open official application portal <ExternalLink className="w-4 h-4" /></a>}{scholarship.sourceUrl && <a href={scholarship.sourceUrl} target="_blank" rel="noreferrer" className="text-sm font-bold text-blue-600 py-2.5">Open source <ExternalLink className="w-4 h-4 inline" /></a>}<Link to={`/scholarships/${id}/apply`} className="text-sm font-bold text-slate-600 py-2.5">Analyze completed form</Link></div></Card></div>;
}
function Info({ label, value }) { return <div className="rounded-xl bg-slate-50 border border-slate-100 p-3"><span className="text-xs text-slate-500">{label}</span><p className="font-bold text-slate-900 mt-1">{value}</p></div>; }
export default ScholarshipDetails;
