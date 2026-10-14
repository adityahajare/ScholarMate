export const EDUCATION_OPTIONS = [
  { id: 'SCHOOL', title: 'School', subtitle: 'Primary and Classes 8–10', icon: '▣' },
  { id: 'CLASS_11_12', title: 'Class 11–12', subtitle: 'Stream and qualifying exam', icon: '▤' },
  { id: 'DIPLOMA', title: 'Diploma', subtitle: 'Polytechnic and technical courses', icon: '⌘' },
  { id: 'ITI_VOCATIONAL', title: 'ITI / vocational', subtitle: 'Skill and trade courses', icon: '⚙' },
  { id: 'UNDERGRADUATE', title: 'Undergraduate', subtitle: 'Engineering, medical and degrees', icon: '◆' },
  { id: 'POSTGRADUATE', title: 'Postgraduate', subtitle: 'M.Tech, MBA, MCA, M.Sc. and more', icon: '◇' },
  { id: 'PHD_RESEARCH', title: 'PhD / research', subtitle: 'Research, fellowship and doctorate', icon: '⌬' },
  { id: 'PROFESSIONAL_OTHER', title: 'Other', subtitle: 'Professional and eligible courses', icon: '▦' },
];

const commonSections = [
  { id: 'personal', label: 'Personal information', fields: [
    { key: 'fullName', label: 'Full name', type: 'text', required: true },
    { key: 'dateOfBirth', label: 'Date of birth', type: 'date' },
    { key: 'gender', label: 'Gender', type: 'select', options: ['Female', 'Male', 'Other', 'Prefer not to say'] },
    { key: 'mobile', label: 'Mobile number', type: 'tel' },
    { key: 'nationality', label: 'Nationality', type: 'text', hint: 'Ask only when a scheme requires it.' },
  ] },
  { id: 'category', label: 'Category and eligibility', fields: [
    { key: 'category', label: 'Category', type: 'select', options: ['Open', 'OBC', 'SC', 'ST', 'EWS', 'Other', 'Unknown'] },
    { key: 'subCategory', label: 'Sub-category', type: 'text' },
    { key: 'caste', label: 'Caste / community', type: 'text', hint: 'Optional; only where relevant.' },
    { key: 'disabilityStatus', label: 'Disability status', type: 'select', options: ['No', 'Yes', 'Not applicable', 'Unknown'] },
  ] },
  { id: 'class10', label: 'Class 10 history', show: ['SCHOOL', 'CLASS_11_12', 'DIPLOMA', 'ITI_VOCATIONAL', 'UNDERGRADUATE', 'POSTGRADUATE', 'PHD_RESEARCH'], fields: [
    { key: 'class10.board', label: 'Board', type: 'text' }, { key: 'class10.school', label: 'School', type: 'text' },
    { key: 'class10.passingYear', label: 'Passing year', type: 'number', min: 1950, max: 2100 },
    { key: 'class10.obtainedMarks', label: 'Obtained marks', type: 'number', min: 0 }, { key: 'class10.maximumMarks', label: 'Maximum marks', type: 'number', min: 1 },
    { key: 'class10.result', label: 'Result', type: 'select', options: ['PASS', 'FAIL', 'APPEARING', 'UNKNOWN'] },
  ] },
  { id: 'qualifying', label: 'Class 12 / qualifying exam', show: ['CLASS_11_12', 'DIPLOMA', 'ITI_VOCATIONAL', 'UNDERGRADUATE', 'POSTGRADUATE', 'PHD_RESEARCH'], fields: [
    { key: 'qualifying.board', label: 'Board / university', type: 'text' }, { key: 'qualifying.stream', label: 'Stream', type: 'text' },
    { key: 'qualifying.exam', label: 'Qualifying examination', type: 'text' }, { key: 'qualifying.passingYear', label: 'Passing year', type: 'number', min: 1950, max: 2100 },
    { key: 'qualifying.obtainedMarks', label: 'Obtained marks', type: 'number', min: 0 }, { key: 'qualifying.maximumMarks', label: 'Maximum marks', type: 'number', min: 1 },
  ] },
  { id: 'qualifications', label: 'Other past qualifications', fields: [] },
  { id: 'course', label: 'Current course', show: ['DIPLOMA', 'ITI_VOCATIONAL', 'UNDERGRADUATE', 'POSTGRADUATE', 'PHD_RESEARCH', 'PROFESSIONAL_OTHER'], fields: [
    { key: 'course.degree', label: 'Degree / course', type: 'text', required: true }, { key: 'course.branch', label: 'Branch / stream', type: 'text' },
    { key: 'course.institution', label: 'Institution', type: 'text', required: true }, { key: 'course.university', label: 'University', type: 'text' },
    { key: 'course.yearOrSemester', label: 'Year / semester', type: 'text' }, { key: 'course.durationYears', label: 'Course duration (years)', type: 'number', min: 1, max: 15 },
    { key: 'course.admissionDate', label: 'Admission date', type: 'date' }, { key: 'course.accreditation', label: 'Accreditation / approval', type: 'text' },
  ] },
  { id: 'admission', label: 'Admission details', show: ['DIPLOMA', 'UNDERGRADUATE', 'POSTGRADUATE'], fields: [
    { key: 'admission.type', label: 'Admission type', type: 'select', options: ['CAP', 'CENTRALIZED', 'INSTITUTE_LEVEL', 'MANAGEMENT', 'OTHER', 'NOT_APPLICABLE'] },
    { key: 'admission.category', label: 'Admission category', type: 'text' }, { key: 'admission.round', label: 'Round', type: 'text' },
    { key: 'admission.allotmentNumber', label: 'Allotment number', type: 'text' }, { key: 'admission.confirmed', label: 'Admission confirmed', type: 'select', options: ['Yes', 'No', 'Unknown'] },
  ] },
  { id: 'family', label: 'Family and income', fields: [
    { key: 'family.annualIncome', label: 'Annual family income (INR)', type: 'number', min: 0 }, { key: 'family.incomeCertificateNumber', label: 'Income certificate number', type: 'text' },
    { key: 'family.parentOccupation', label: 'Parent / guardian occupation', type: 'text' }, { key: 'family.dependents', label: 'Dependents', type: 'number', min: 0 },
    { key: 'family.siblings', label: 'Siblings / children', type: 'number', min: 0 },
  ] },
  { id: 'address', label: 'Address and domicile', fields: [
    { key: 'address.permanent', label: 'Permanent address', type: 'textarea' }, { key: 'address.current', label: 'Current address', type: 'textarea' },
    { key: 'address.state', label: 'State', type: 'text', required: true }, { key: 'address.district', label: 'District', type: 'text' },
    { key: 'address.taluka', label: 'Taluka', type: 'text' }, { key: 'address.village', label: 'Village / city', type: 'text' }, { key: 'address.pinCode', label: 'PIN code', type: 'text' },
  ] },
  { id: 'stay', label: 'Stay / hostel', show: ['DIPLOMA', 'UNDERGRADUATE', 'POSTGRADUATE', 'PHD_RESEARCH'], fields: [
    { key: 'stay.type', label: 'Stay type', type: 'select', options: ['Hosteller', 'Day scholar', 'Unknown'] }, { key: 'stay.hostelType', label: 'Hostel type', type: 'text' },
    { key: 'stay.institution', label: 'Hostel / institution', type: 'text' }, { key: 'stay.monthlyRent', label: 'Monthly rent (INR)', type: 'number', min: 0 },
  ] },
  { id: 'progress', label: 'Marks and academic progress', show: ['UNDERGRADUATE', 'POSTGRADUATE', 'PHD_RESEARCH'], fields: [
    { key: 'progress.previousMarks', label: 'Previous semester marks', type: 'number', min: 0 }, { key: 'progress.cgpa', label: 'CGPA', type: 'number', min: 0, max: 10, step: 0.01 },
    { key: 'progress.backlogs', label: 'Backlogs', type: 'number', min: 0 }, { key: 'progress.attendance', label: 'Attendance (%)', type: 'number', min: 0, max: 100 },
    { key: 'progress.gaps', label: 'Education gap years', type: 'number', min: 0 }, { key: 'progress.promotion', label: 'Promotion status', type: 'select', options: ['PROMOTED', 'NOT_PROMOTED', 'ONGOING', 'UNKNOWN'] },
  ] },
  { id: 'documents', label: 'Documents', fields: [] },
  { id: 'review', label: 'Review and submit', fields: [] },
];

export const getSections = (educationType) => commonSections.filter((section) => !section.show || section.show.includes(educationType));
