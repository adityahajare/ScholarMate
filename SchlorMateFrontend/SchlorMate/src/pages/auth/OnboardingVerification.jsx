import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowRight, CheckCircle2, FileSearch, Upload } from "lucide-react";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import { documentReviewApi } from "../../lib/api";

function OnboardingVerification() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const review = async () => {
    if (!file) return setError("Choose a PDF, PNG, or JPEG document first.");
    setError(""); setBusy(true);
    try { const { data } = await documentReviewApi.reviewFile(file); setResult(data); }
    catch (requestError) { setError(requestError.response?.data?.message || "The document could not be analyzed."); }
    finally { setBusy(false); }
  };

  return <div className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6"><div className="mx-auto max-w-2xl"><Card className="p-6 sm:p-8" hover={false}><div className="mb-6 flex items-start gap-4"><div className="rounded-2xl bg-blue-100 p-3 text-blue-700"><FileSearch /></div><div><p className="text-sm font-bold uppercase tracking-wider text-blue-600">Optional first step</p><h1 className="mt-1 text-2xl font-extrabold text-slate-900">Review a proof document</h1><p className="mt-2 text-sm leading-relaxed text-slate-500">Upload a marksheet, certificate, income proof, or similar document. OCR will extract suggestions and compare them with the stored scholarship catalog. Your upload and response are saved to your account immediately.</p></div></div>{error && <div className="mb-4 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm font-semibold text-rose-700">{error}</div>}{!result ? <><label className="flex cursor-pointer flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 p-8 text-center hover:border-blue-400"><Upload className="mb-3 h-7 w-7 text-slate-400" /><span className="text-sm font-bold text-slate-800">{file ? file.name : "Choose a document"}</span><span className="mt-1 text-xs text-slate-500">PDF, PNG, or JPEG up to 10 MB</span><input type="file" accept="application/pdf,image/png,image/jpeg" onChange={(event) => setFile(event.target.files?.[0] || null)} className="hidden" /></label><div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end"><Button variant="outline" onClick={() => navigate("/dashboard")}>Skip for now</Button><Button onClick={review} disabled={busy} icon={FileSearch}>{busy ? "Analyzing..." : "Analyze and save"}</Button></div></> : <div className="space-y-5"><div className="flex items-center gap-3 rounded-xl bg-emerald-50 p-4 text-emerald-800"><CheckCircle2 className="h-5 w-5" /><span className="text-sm font-bold">Analysis saved to your account.</span></div><div className="rounded-xl border border-slate-200 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Extracted suggestion</p><pre className="mt-3 max-h-48 overflow-auto whitespace-pre-wrap text-sm text-slate-700">{JSON.stringify(result.suggestedProfile || {}, null, 2)}</pre></div><p className="text-xs leading-relaxed text-slate-500">These are OCR suggestions, not official verification. Review and save any correct values from your User Profile.</p><Button className="w-full" onClick={() => navigate("/dashboard")} icon={ArrowRight}>Continue to dashboard</Button></div>}</Card></div></div>;
}

export default OnboardingVerification;
