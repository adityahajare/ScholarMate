import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, ExternalLink, FileCheck2, FileText, LoaderCircle, ShieldCheck, Upload, XCircle } from "lucide-react";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import { documentReviewApi, scholarshipApi } from "../../lib/api";

function ApplicationForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [scholarship, setScholarship] = useState(null);
  const [loading, setLoading] = useState(true);
  const [text, setText] = useState("");
  const [selectedFile, setSelectedFile] = useState(null);
  const [review, setReview] = useState(null);
  const [reviewing, setReviewing] = useState(false);
  const [reviewHistory, setReviewHistory] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    scholarshipApi.get(id)
      .then(({ data }) => setScholarship(data))
      .catch(() => setError("We could not load this scholarship's official source details."))
      .finally(() => setLoading(false));
    documentReviewApi.history()
      .then(({ data }) => setReviewHistory(data || []))
      .catch(() => setReviewHistory([]));
  }, [id]);

  const analyze = async () => {
    if (!selectedFile && !text.trim()) {
      setError("Upload the completed form or paste its text before analyzing.");
      return;
    }
    setReviewing(true);
    setError("");
    try {
      const response = selectedFile
        ? await documentReviewApi.reviewFile(selectedFile)
        : await documentReviewApi.reviewText(text);
      setReview(response.data);
      const historyResponse = await documentReviewApi.history();
      setReviewHistory(historyResponse.data || []);
    } catch (requestError) {
      setError(requestError.response?.data?.message || "The analysis agent could not review this form.");
    } finally {
      setReviewing(false);
    }
  };

  if (loading) return <Loading message="Loading official scholarship source..." />;

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      <div>
        <Button variant="outline" size="sm" onClick={() => navigate(-1)} icon={ArrowLeft} className="mb-3">Back</Button>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Prepare your scholarship application</h1>
        <p className="text-slate-500 text-xs sm:text-sm mt-1">ScholarMate does not submit official applications. We guide you, open the official source, and check your completed form for missing information.</p>
      </div>

      {error && <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800 flex items-center gap-2"><XCircle className="w-4 h-4" />{error}</div>}

      <Card className="p-6 sm:p-8" hover={false}>
        <div className="flex items-start gap-3.5"><div className="w-10 h-10 rounded-xl bg-blue-600 text-white flex items-center justify-center shrink-0"><ShieldCheck className="w-5 h-5" /></div><div><h2 className="text-lg font-bold text-slate-900">Official source</h2><p className="text-sm text-slate-600 mt-1">{scholarship?.name || "Scholarship"}</p><p className="text-xs text-slate-500 mt-1">Verify the provider and complete the form only on the official portal.</p></div></div>
        {scholarship?.applicationUrl ? <a href={scholarship.applicationUrl} target="_blank" rel="noreferrer" className="mt-5 inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-bold text-white hover:bg-blue-700">Open official application portal <ExternalLink className="w-4 h-4" /></a> : <p className="mt-5 rounded-xl bg-amber-50 border border-amber-200 p-3 text-xs text-amber-800">No official application URL is available yet. Use the source link on the scholarship details page and verify the provider before continuing.</p>}
      </Card>

      <Card className="p-6 sm:p-8" hover={false}>
        <div className="flex items-start gap-3"><FileCheck2 className="w-5 h-5 text-emerald-600 mt-0.5" /><div><h2 className="text-lg font-bold text-slate-900">Check your completed form</h2><p className="text-sm text-slate-500 mt-1">After completing the official form, upload its PDF/image or paste the copied text. The agent only analyzes what you provide; it does not submit anything.</p></div></div>
        <label className="mt-5 flex cursor-pointer items-center gap-3 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-4 hover:border-blue-400"><Upload className="w-5 h-5 text-blue-600" /><span className="text-sm font-semibold text-slate-700">{selectedFile ? selectedFile.name : "Choose completed PDF or image"}</span><input type="file" accept="application/pdf,image/*,.txt" className="hidden" onChange={(event) => { setSelectedFile(event.target.files?.[0] || null); setReview(null); }} /></label>
        <div className="my-5 flex items-center gap-3 text-xs text-slate-400"><span className="h-px flex-1 bg-slate-200" />OR<span className="h-px flex-1 bg-slate-200" /></div>
        <textarea value={text} onChange={(event) => { setText(event.target.value); setSelectedFile(null); setReview(null); }} rows="7" placeholder="Paste the completed form text here..." className="w-full rounded-xl border border-slate-200 bg-slate-50/50 p-4 text-sm text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500" />
        <Button className="mt-4" onClick={analyze} disabled={reviewing} icon={reviewing ? LoaderCircle : FileText}>{reviewing ? "Analyzing..." : "Analyze completed form"}</Button>
      </Card>

      {review && <Card className="p-6 sm:p-8" hover={false}><h2 className="text-lg font-bold text-slate-900">Analysis result</h2><p className="text-xs text-slate-500 mt-1">Input reviewed using {review.inputMethod || "submitted content"}.</p><div className="mt-4 rounded-xl bg-slate-50 border border-slate-200 p-4"><p className="text-sm font-semibold text-slate-800">Warnings and missing information</p>{review.warnings?.length ? <ul className="mt-2 list-disc pl-5 text-sm text-amber-800 space-y-1">{review.warnings.map((warning) => <li key={warning}>{warning}</li>)}</ul> : <p className="mt-2 text-sm text-emerald-700">No warnings were detected in the provided content.</p>}</div><p className="mt-4 text-xs text-slate-500">You remain responsible for submitting the final application on the official portal.</p></Card>}

      <Card className="p-6 sm:p-8" hover={false}><h2 className="text-lg font-bold text-slate-900">Saved analysis history</h2><p className="text-xs text-slate-500 mt-1">Every completed analysis response is stored against your User account.</p>{reviewHistory.length ? <div className="mt-4 space-y-2">{reviewHistory.map((item) => <div key={item.id} className="flex items-center justify-between rounded-xl border border-slate-200 p-3 text-xs"><span className="font-semibold text-slate-700">{item.inputMethod}</span><span className="text-slate-500">{new Date(item.createdAt).toLocaleString()}</span></div>)}</div> : <p className="mt-4 text-sm text-slate-500">No saved analysis responses yet.</p>}</Card>
    </div>
  );
}

export default ApplicationForm;
