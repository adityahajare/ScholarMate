import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { CheckCircle2, FileText, Plus, Save, Trash2, UploadCloud } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Loading from '../../components/common/Loading';
import ErrorState from '../../components/common/ErrorState';
import { useProfile } from '../../context/ProfileContext';
import { documentReviewApi, educationApi, profileChildApi, requirementsApi } from '../../lib/api';
import { getSections } from './profileSchema';

const getValue = (object, path) => path.split('.').reduce((current, key) => current?.[key], object) ?? '';
const setValue = (object, path, value) => { const keys = path.split('.'); const next = { ...object }; let target = next; keys.forEach((key, index) => { if (index === keys.length - 1) target[key] = value; else { target[key] = { ...(target[key] ?? {}) }; target = target[key]; } }); return next; };
const percentage = (draft, field) => { const value = Number(getValue(draft, field)); const max = Number(getValue(draft, field.replace('obtainedMarks', 'maximumMarks'))); return value > 0 && max > 0 ? Math.round((value / max) * 10000) / 100 : null; };

function ProfileBuilder() {
  const { draft, loading, saveStep } = useProfile();
  const educationType = draft?.educationType?.value;
  const sections = useMemo(() => getSections(educationType), [educationType]);
  const [active, setActive] = useState(0);
  const [localDraft, setLocalDraft] = useState(draft || {});
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [ocrState, setOcrState] = useState('');
  const [searchParams] = useSearchParams();
  const scholarshipId = searchParams.get('scholarshipId');
  const [requirements, setRequirements] = useState(null);
  const [requirementsError, setRequirementsError] = useState('');
  const [familyMembers, setFamilyMembers] = useState([]);
  const [educationRecords, setEducationRecords] = useState([]);
  const [childError, setChildError] = useState('');
  const [memberForm, setMemberForm] = useState({ relationship: '', name: '', occupation: '', annualIncome: '', dependent: true, beneficiaryCount: '' });
  const [qualificationForm, setQualificationForm] = useState({ level: 'OTHER', institution: '', boardOrUniversity: '', stream: '', passingYear: '', obtainedMarks: '', maximumMarks: '', completionStatus: 'COMPLETED' });
  const [editingFamilyId, setEditingFamilyId] = useState(null);
  const [editingQualificationId, setEditingQualificationId] = useState(null);
  const workingDraft = Object.keys(localDraft).length ? localDraft : (draft || {});
  const section = sections[active];

  useEffect(() => {
    if (!scholarshipId) return;
    requirementsApi.get(scholarshipId).then((response) => setRequirements(response.data)).catch(() => setRequirementsError('Scholarship requirements could not be loaded.'));
  }, [scholarshipId]);

  useEffect(() => {
    Promise.all([profileChildApi.family.list(), educationApi.list()])
      .then(([familyResponse, educationResponse]) => {
        setFamilyMembers(familyResponse.data || []);
        setEducationRecords(educationResponse.data || []);
      })
      .catch(() => setChildError('Existing family or qualification records could not be loaded.'));
  }, []);

  if (loading) return <Loading message="Loading your saved profile draft…" />;
  if (!educationType) return <ErrorState title="Choose an education path first" message="Select your education level to generate the right questions." onRetry={() => { window.location.href = '/profile/setup'; }} />;
  if (!section) return null;

  const update = (key, value) => setLocalDraft((current) => setValue(Object.keys(current).length ? current : (draft || {}), key, value));
  const saveCurrent = async () => { setSaving(true); setError(''); try { const missing = section.fields.filter((field) => field.required && String(getValue(workingDraft, field.key)).trim() === ''); if (missing.length) { setError(`Complete required fields: ${missing.map((field) => field.label).join(', ')}`); return false; } const stepData = section.fields.reduce((result, field) => setValue(result, field.key, getValue(workingDraft, field.key)), {}); const answers = section.id === 'review' ? getValue(workingDraft, 'requirements.answers') : undefined; await saveStep(section.id, { ...stepData, ...(answers ? { requirements: { answers } } : {}), state: 'KNOWN' }); return true; } catch { setError('Could not save this section. Please try again.'); return false; } finally { setSaving(false); } };
  const next = async () => { if (await saveCurrent()) setActive((current) => Math.min(current + 1, sections.length - 1)); };
  const reviewDocument = async (file) => {
    if (!file) return;
    if (!['application/pdf', 'image/jpeg', 'image/png'].includes(file.type) || file.size > 10 * 1024 * 1024) {
      setOcrState('Only PDF, JPG or PNG files up to 10 MB are allowed.');
      return;
    }
    setOcrState('Reviewing document…');
    try {
      const uploaded = await profileChildApi.documents.upload('PROFILE_SUPPORTING_DOCUMENT', file);
      const response = await documentReviewApi.reviewFile(file);
      await profileChildApi.documents.update(uploaded.data.id, { status: 'OCR_EXTRACTED', extractedData: JSON.stringify(response.data) });
      update('documents.latestReview', { state: 'OCR_EXTRACTED', fileName: file.name, response: response.data });
      setOcrState('OCR extracted and saved as unconfirmed. Review the fields before confirming.');
    } catch {
      setOcrState('OCR review failed. You can still save the document as unconfirmed.');
    }
  };
  const addFamilyMember = async (event) => {
    event.preventDefault();
    setChildError('');
    try {
      const payload = { ...memberForm, annualIncome: memberForm.annualIncome === '' ? null : Number(memberForm.annualIncome), beneficiaryCount: memberForm.beneficiaryCount === '' ? null : Number(memberForm.beneficiaryCount) };
      const response = editingFamilyId ? await profileChildApi.family.update(editingFamilyId, payload) : await profileChildApi.family.create(payload);
      setFamilyMembers((current) => editingFamilyId ? current.map((member) => member.id === editingFamilyId ? response.data : member) : [...current, response.data]);
      setEditingFamilyId(null);
      setMemberForm({ relationship: '', name: '', occupation: '', annualIncome: '', dependent: true, beneficiaryCount: '' });
    } catch (requestError) { setChildError(requestError.response?.data?.message || 'Could not add this family member.'); }
  };
  const removeFamilyMember = async (id) => {
    try { await profileChildApi.family.remove(id); setFamilyMembers((current) => current.filter((member) => member.id !== id)); }
    catch (requestError) { setChildError(requestError.response?.data?.message || 'Could not remove this family member.'); }
  };
  const addQualification = async (event) => {
    event.preventDefault();
    setChildError('');
    try {
      const payload = { ...qualificationForm, passingYear: qualificationForm.passingYear === '' ? null : Number(qualificationForm.passingYear), obtainedMarks: qualificationForm.obtainedMarks === '' ? null : Number(qualificationForm.obtainedMarks), maximumMarks: qualificationForm.maximumMarks === '' ? null : Number(qualificationForm.maximumMarks), currentRecord: false };
      const response = editingQualificationId ? await educationApi.update(editingQualificationId, payload) : await educationApi.create(payload);
      setEducationRecords((current) => editingQualificationId ? current.map((record) => record.id === editingQualificationId ? response.data : record) : [...current, response.data]);
      setEditingQualificationId(null);
      setQualificationForm({ level: 'OTHER', institution: '', boardOrUniversity: '', stream: '', passingYear: '', obtainedMarks: '', maximumMarks: '', completionStatus: 'COMPLETED' });
    } catch (requestError) { setChildError(requestError.response?.data?.message || 'Could not add this qualification.'); }
  };
  const removeQualification = async (id) => {
    try { await educationApi.remove(id); setEducationRecords((current) => current.filter((record) => record.id !== id)); }
    catch (requestError) { setChildError(requestError.response?.data?.message || 'Could not remove this qualification.'); }
  };

  return <div className="space-y-5 animate-fadeIn">
    <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-end"><div><span className="text-xs font-bold uppercase tracking-wider text-blue-700">{educationType.replaceAll('_', ' ')}</span><h1 className="mt-1 text-2xl font-extrabold text-slate-900">Build your profile</h1><p className="mt-1 text-sm text-slate-500">Unknown answers stay unknown until you confirm them.</p></div><div className="min-w-52"><div className="mb-1 flex justify-between text-[11px] font-bold text-slate-500"><span>Profile completion</span><span>{Math.round(((active + 1) / sections.length) * 100)}%</span></div><div className="h-2 rounded-full bg-slate-200"><div className="h-2 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 transition-all" style={{ width: `${((active + 1) / sections.length) * 100}%` }} /></div></div></div>
    <div className="grid gap-5 lg:grid-cols-[220px_1fr]">
      <Card className="h-fit p-3" hover={false}><nav aria-label="Profile sections" className="space-y-1">{sections.map((item, index) => <button key={item.id} type="button" onClick={() => setActive(index)} className={`flex w-full items-center gap-2 rounded-xl px-3 py-2 text-left text-xs font-semibold ${index === active ? 'bg-blue-50 text-blue-700' : 'text-slate-500 hover:bg-slate-50'}`}><span className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[10px] ${index < active ? 'bg-emerald-100 text-emerald-700' : index === active ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-400'}`}>{index < active ? '✓' : index + 1}</span>{item.label}</button>)}</nav></Card>
      <Card className="p-5 sm:p-7" hover={false}><div className="mb-6 flex items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">Section {active + 1} of {sections.length}</p><h2 className="mt-1 text-xl font-bold text-slate-900">{section.label}</h2></div><CheckCircle2 className="h-5 w-5 text-emerald-500" aria-label="Saved section status" /></div>
        {(error || childError) && <div role="alert" className="mb-4 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-semibold text-rose-700">{error || childError}</div>}
        <div className="grid gap-4 sm:grid-cols-2">{section.fields.map((field) => <label key={field.key} className={`${field.type === 'textarea' ? 'sm:col-span-2' : ''} block text-xs font-bold text-slate-700`}><span>{field.label}{field.required && <span className="ml-1 text-rose-500">*</span>}</span>{field.type === 'textarea' ? <textarea value={getValue(workingDraft, field.key)} onChange={(event) => update(field.key, event.target.value)} rows={3} className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-normal outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15" /> : field.type === 'select' ? <select value={getValue(workingDraft, field.key)} onChange={(event) => update(field.key, event.target.value)} className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-normal outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15"><option value="">Select</option>{field.options.map((option) => <option key={option}>{option}</option>)}</select> : <input type={field.type} min={field.min} max={field.max} step={field.step} value={getValue(workingDraft, field.key)} onChange={(event) => update(field.key, event.target.value)} className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-normal outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15" />}{field.hint && <span className="mt-1 block text-[11px] font-normal text-slate-400">{field.hint}</span>}{field.key.endsWith('obtainedMarks') && percentage(workingDraft, field.key) !== null && <span className="mt-1 block text-[11px] font-semibold text-blue-600">Calculated percentage: {percentage(workingDraft, field.key)}%</span>}</label>)}</div>
        {section.id === 'qualifications' && <RepeatableQualificationPanel form={qualificationForm} setForm={setQualificationForm} records={educationRecords} onAdd={addQualification} onRemove={removeQualification} editingId={editingQualificationId} onEdit={(record) => { setEditingQualificationId(record.id); setQualificationForm({ level: record.level || 'OTHER', institution: record.institution || '', boardOrUniversity: record.boardOrUniversity || '', stream: record.stream || '', passingYear: record.passingYear || '', obtainedMarks: record.obtainedMarks || '', maximumMarks: record.maximumMarks || '', completionStatus: record.completionStatus || 'COMPLETED' }); }} />}
        {section.id === 'family' && <RepeatableFamilyPanel form={memberForm} setForm={setMemberForm} members={familyMembers} onAdd={addFamilyMember} onRemove={removeFamilyMember} editingId={editingFamilyId} onEdit={(member) => { setEditingFamilyId(member.id); setMemberForm({ relationship: member.relationship || '', name: member.name || '', occupation: member.occupation || '', annualIncome: member.annualIncome || '', dependent: member.dependent ?? true, beneficiaryCount: member.beneficiaryCount || '' }); }} />}
        {section.id === 'documents' && <div className="mt-6 rounded-2xl border border-dashed border-blue-200 bg-blue-50/50 p-5"><div className="flex items-start gap-3"><UploadCloud className="h-5 w-5 text-blue-600" /><div><p className="text-sm font-bold text-slate-800">Upload a document for optional OCR review</p><p className="mt-1 text-xs text-slate-500">PDF, JPG and PNG are supported. OCR is a suggestion until you confirm it.</p><input type="file" accept="application/pdf,image/jpeg,image/png" onChange={(event) => reviewDocument(event.target.files?.[0])} className="mt-3 block w-full text-xs" />{ocrState && <p className="mt-2 text-xs font-semibold text-blue-700">{ocrState}</p>}</div></div></div>}
        {section.id === 'review' && <div className="mt-6 space-y-4"><div className="rounded-2xl border border-emerald-100 bg-emerald-50 p-4 text-xs text-emerald-800"><FileText className="mb-2 h-5 w-5" /><p className="font-bold">Review before submitting</p><p className="mt-1">Fields marked UNKNOWN or SKIPPED will not be treated as eligible or ineligible automatically.</p></div>{scholarshipId && <div className="rounded-2xl border border-indigo-100 bg-indigo-50/50 p-4 text-xs text-slate-700"><p className="font-bold text-indigo-800">Scholarship-specific requirements</p>{requirementsError && <p className="mt-2 text-rose-700">{requirementsError}</p>}{requirements && <div className="mt-3 space-y-3"><p><span className="font-semibold">Required documents:</span> {requirements.requiredDocuments?.map((item) => item.documentType).join(', ') || 'None listed'}</p>{requirements.criteria?.map((criterion) => <label key={criterion.id} className="block"><span className="font-semibold">{criterion.criterionType} {criterion.operator} {criterion.value}</span><select value={getValue(workingDraft, `requirements.answers.${criterion.id}`)} onChange={(event) => update(`requirements.answers.${criterion.id}`, event.target.value)} className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-normal"><option value="">Unknown</option><option value="KNOWN">Known / satisfied</option><option value="NOT_APPLICABLE">Not applicable</option><option value="SKIPPED">Skipped</option></select></label>)}{requirements.evidence?.map((item) => <a key={item.id} href={item.sourceUrl} target="_blank" rel="noreferrer" className="block text-indigo-700 underline">{item.title || item.sourceUrl}</a>)}</div>}</div>}</div>}
        <div className="mt-8 flex flex-wrap justify-between gap-3 border-t border-slate-100 pt-5"><Button variant="outline" onClick={() => setActive((current) => Math.max(current - 1, 0))} disabled={active === 0}>← Back</Button><div className="flex gap-2"><Button variant="outline" onClick={saveCurrent} disabled={saving} icon={Save}>{saving ? 'Saving…' : 'Save as draft'}</Button>{active < sections.length - 1 && <Button onClick={next}>Next →</Button>}</div></div>
      </Card>
    </div>
  </div>;
}

export default ProfileBuilder;

const fieldClass = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15';
function RepeatableFamilyPanel({ form, setForm, members, onAdd, onRemove, onEdit, editingId }) {
  return <div className="mt-6 space-y-4"><div className="rounded-2xl border border-slate-200 p-4"><h3 className="text-sm font-bold text-slate-900">Family members</h3><p className="mt-1 text-xs text-slate-500">Add only people relevant to the scholarship requirements.</p><form onSubmit={onAdd} className="mt-4 grid gap-3 sm:grid-cols-2"><input required aria-label="Relationship" placeholder="Relationship" value={form.relationship} onChange={(event) => setForm({ ...form, relationship: event.target.value })} className={fieldClass} /><input required aria-label="Name" placeholder="Name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className={fieldClass} /><input aria-label="Occupation" placeholder="Occupation" value={form.occupation} onChange={(event) => setForm({ ...form, occupation: event.target.value })} className={fieldClass} /><input type="number" min="0" aria-label="Annual income" placeholder="Annual income (INR)" value={form.annualIncome} onChange={(event) => setForm({ ...form, annualIncome: event.target.value })} className={fieldClass} /><input type="number" min="0" aria-label="Beneficiary count" placeholder="Other beneficiaries" value={form.beneficiaryCount} onChange={(event) => setForm({ ...form, beneficiaryCount: event.target.value })} className={fieldClass} /><label className="flex items-center gap-2 text-xs font-semibold text-slate-600"><input type="checkbox" checked={form.dependent} onChange={(event) => setForm({ ...form, dependent: event.target.checked })} /> Dependent</label><Button type="submit" className="sm:col-span-2" icon={Plus}>{editingId ? 'Update family member' : 'Add family member'}</Button></form></div>{members.map((member) => <div key={member.id} className="flex items-center justify-between gap-3 rounded-xl bg-slate-50 px-3 py-2 text-xs"><span><strong>{member.name}</strong> · {member.relationship}{member.occupation ? ` · ${member.occupation}` : ''}</span><span className="flex gap-2"><button type="button" aria-label={`Edit ${member.name}`} onClick={() => onEdit(member)} className="font-semibold text-blue-600">Edit</button><button type="button" aria-label={`Remove ${member.name}`} onClick={() => onRemove(member.id)} className="text-rose-600"><Trash2 className="h-4 w-4" /></button></span></div>)}</div>;
}
function RepeatableQualificationPanel({ form, setForm, records, onAdd, onRemove, onEdit, editingId }) {
  return <div className="mt-6 space-y-4"><div className="rounded-2xl border border-slate-200 p-4"><h3 className="text-sm font-bold text-slate-900">Past qualifications</h3><form onSubmit={onAdd} className="mt-4 grid gap-3 sm:grid-cols-2"><select aria-label="Qualification level" value={form.level} onChange={(event) => setForm({ ...form, level: event.target.value })} className={fieldClass}><option>OTHER</option><option>CLASS_10</option><option>CLASS_12</option><option>DIPLOMA</option><option>ITI_VOCATIONAL</option><option>UG</option><option>PG</option><option>PHD_RESEARCH</option></select><input required aria-label="Institution" placeholder="Institution" value={form.institution} onChange={(event) => setForm({ ...form, institution: event.target.value })} className={fieldClass} /><input aria-label="Board or university" placeholder="Board / university" value={form.boardOrUniversity} onChange={(event) => setForm({ ...form, boardOrUniversity: event.target.value })} className={fieldClass} /><input aria-label="Course or stream" placeholder="Course / stream" value={form.stream} onChange={(event) => setForm({ ...form, stream: event.target.value })} className={fieldClass} /><input type="number" min="1950" max="2100" aria-label="Passing year" placeholder="Passing year" value={form.passingYear} onChange={(event) => setForm({ ...form, passingYear: event.target.value })} className={fieldClass} /><select aria-label="Completion status" value={form.completionStatus} onChange={(event) => setForm({ ...form, completionStatus: event.target.value })} className={fieldClass}><option>COMPLETED</option><option>ONGOING</option><option>NOT_COMPLETED</option></select><input type="number" min="0" aria-label="Obtained marks" placeholder="Obtained marks" value={form.obtainedMarks} onChange={(event) => setForm({ ...form, obtainedMarks: event.target.value })} className={fieldClass} /><input type="number" min="1" aria-label="Maximum marks" placeholder="Maximum marks" value={form.maximumMarks} onChange={(event) => setForm({ ...form, maximumMarks: event.target.value })} className={fieldClass} /><Button type="submit" className="sm:col-span-2" icon={Plus}>{editingId ? 'Update qualification' : 'Add qualification'}</Button></form></div>{records.filter((record) => !record.currentRecord).map((record) => <div key={record.id} className="flex items-center justify-between gap-3 rounded-xl bg-slate-50 px-3 py-2 text-xs"><span><strong>{record.level}</strong> · {record.institution}{record.passingYear ? ` · ${record.passingYear}` : ''}</span><span className="flex gap-2"><button type="button" aria-label={`Edit ${record.institution}`} onClick={() => onEdit(record)} className="font-semibold text-blue-600">Edit</button><button type="button" aria-label={`Remove ${record.institution}`} onClick={() => onRemove(record.id)} className="text-rose-600"><Trash2 className="h-4 w-4" /></button></span></div>)}</div>;
}
