import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Search, SlidersHorizontal } from "lucide-react";
import Card from "../../components/common/Card";
import Loading from "../../components/common/Loading";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import { scholarshipApi } from "../../lib/api";

function Scholarships() {
  const [records, setRecords] = useState([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    scholarshipApi.search({ query, page, size: 20 })
      .then(({ data }) => {
        if (!active) return;
        setRecords(data?.content || []);
        setPageInfo(data || null);
      })
      .catch((requestError) => {
        if (active) setError(requestError.response?.data?.message || "Unable to load stored scholarships.");
      })
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [query, page]);

  if (loading) return <Loading message="Loading scholarships from the database..." />;
  if (error) return <ErrorState title="Scholarships unavailable" message={error} onRetry={() => window.location.reload()} />;

  return (
    <div className="space-y-6 animate-fadeIn">
      <div>
        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100"><SlidersHorizontal className="w-3.5 h-3.5" /> Database catalog</span>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-2">Explore Scholarships</h1>
        <p className="text-slate-500 text-sm mt-1">Search runs on the backend; only one page of results is loaded at a time.</p>
      </div>
      <Card className="p-5" hover={false}>
        <div className="relative"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" /><input value={query} onChange={(event) => { setQuery(event.target.value); setPage(0); }} placeholder="Search stored scholarships" aria-label="Search stored scholarships" className="w-full rounded-xl border border-slate-200 bg-slate-50 pl-10 pr-4 py-2.5 text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20" /></div>
      </Card>
      {records.length ? <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">{records.map((item) => <Card key={item.id} className="p-6" hover><span className="text-xs font-bold text-blue-700">{item.category || "Unclassified"}</span><h2 className="text-lg font-bold text-slate-900 mt-2">{item.name || "Unnamed stored scholarship"}</h2><p className="text-xs text-slate-500 mt-1">{item.provider || "Provider not specified"}</p><p className="text-sm text-slate-600 mt-4 line-clamp-4">{item.description || "No description was returned by the source."}</p><div className="grid grid-cols-2 gap-3 mt-4 text-xs"><Info label="Amount" value={item.amount == null ? "Not specified" : `₹${Number(item.amount).toLocaleString("en-IN")}`} /><Info label="Deadline" value={item.applicationDeadline || "Not specified"} /></div><Link to={`/scholarships/${item.id}`} className="inline-flex mt-5 text-sm font-bold text-blue-600">View stored details →</Link></Card>)}</div> : <EmptyState icon={Search} title="No stored scholarships found" message="The database has no matching records yet." />}
      {pageInfo?.totalPages > 1 && <div className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-3"><button type="button" disabled={page === 0} onClick={() => setPage((value) => value - 1)} className="rounded-lg px-3 py-2 text-sm font-semibold disabled:opacity-40">Previous</button><span className="text-sm text-slate-500">Page {page + 1} of {pageInfo.totalPages}</span><button type="button" disabled={page + 1 >= pageInfo.totalPages} onClick={() => setPage((value) => value + 1)} className="rounded-lg px-3 py-2 text-sm font-semibold disabled:opacity-40">Next</button></div>}
    </div>
  );
}

function Info({ label, value }) { return <div className="rounded-xl bg-slate-50 border border-slate-100 p-3"><span className="text-slate-500">{label}</span><p className="font-bold text-slate-900 mt-1">{value}</p></div>; }
export default Scholarships;
