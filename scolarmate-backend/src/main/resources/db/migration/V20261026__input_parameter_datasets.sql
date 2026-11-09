-- ================================================================
-- ScholarMate: Input Parameter Datasets, Rules, Requirements & Discovery Sessions
-- ================================================================

-- 1. Master Fields
CREATE TABLE IF NOT EXISTS master_fields (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    field_key VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(200) NOT NULL,
    section VARCHAR(100) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    options_key VARCHAR(100),
    default_required BOOLEAN NOT NULL DEFAULT FALSE,
    display_condition VARCHAR(200),
    validation_rule VARCHAR(200),
    data_purpose VARCHAR(150)
);

-- 2. Field Option Sets & Options
CREATE TABLE IF NOT EXISTS field_option_sets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    option_key VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS field_options (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    option_set_id BIGINT NOT NULL REFERENCES field_option_sets(id) ON DELETE CASCADE,
    option_value VARCHAR(100) NOT NULL,
    option_label VARCHAR(200) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    UNIQUE (option_set_id, option_value)
);

-- 3. Conditional Rules
CREATE TABLE IF NOT EXISTS conditional_rules (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    field_key VARCHAR(100) NOT NULL REFERENCES master_fields(field_key) ON DELETE CASCADE,
    condition_json JSONB NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('SHOW','HIDE','REQUIRE','OPTIONAL')),
    rule_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT
);

-- 4. Scholarship Requirements
CREATE TABLE IF NOT EXISTS scholarship_requirements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    scholarship_key VARCHAR(100) NOT NULL,
    scholarship_name VARCHAR(250) NOT NULL,
    academic_year VARCHAR(20),
    condition_key VARCHAR(50) NOT NULL,
    field_key VARCHAR(100) NOT NULL REFERENCES master_fields(field_key),
    operator VARCHAR(50) NOT NULL,
    expected_value VARCHAR(500),
    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    source_url TEXT,
    source_document TEXT,
    source_section TEXT,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    effective_from DATE,
    effective_until DATE,
    UNIQUE (scholarship_key, condition_key, field_key)
);

-- 5. Discovery Sessions & Answers
CREATE TABLE IF NOT EXISTS discovery_sessions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    current_state JSONB,
    dataset_version VARCHAR(50) DEFAULT 'v1.0',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS discovery_session_answers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES discovery_sessions(id) ON DELETE CASCADE,
    field_key VARCHAR(100) NOT NULL REFERENCES master_fields(field_key),
    answer_value TEXT,
    answered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (session_id, field_key)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_sch_req_scholarship_key ON scholarship_requirements(scholarship_key);
CREATE INDEX IF NOT EXISTS idx_cond_rules_field_key ON conditional_rules(field_key);
CREATE INDEX IF NOT EXISTS idx_disc_session_user ON discovery_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_disc_answers_session ON discovery_session_answers(session_id);

-- ================================================================
-- SEED DATA: Field Option Sets
-- ================================================================
INSERT INTO field_option_sets (option_key, label) VALUES
('GENDER', 'Gender'),
('MARITAL_STATUS', 'Marital Status'),
('RELIGION', 'Religion'),
('INDIAN_STATES', 'Indian States and UTs'),
('CASTE_CATEGORY', 'Social / Caste Category'),
('EDUCATION_LEVELS', 'Current Education Levels'),
('COURSES', 'Courses Catalogue'),
('STUDY_YEARS', 'Current Year of Study'),
('ADMISSION_TYPES', 'Admission Types'),
('QUALIFICATION_TYPES', 'Previous Qualifications'),
('RESULT_TYPES', 'Previous Examination Results'),
('ACCOMMODATION_TYPES', 'Accommodation Types'),
('HOSTEL_TYPES', 'Hostel Types'),
('DISABILITY_TYPES', 'Disability Types'),
('DOCUMENT_TYPES', 'Required Document Types'),
('DOCUMENT_STATUSES', 'Document Verification Statuses')
ON CONFLICT (option_key) DO UPDATE SET label = EXCLUDED.label;

-- SEED DATA: Options for EDUCATION_LEVELS
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('SCHOOL', 'School', 1),
  ('ITI', 'ITI', 2),
  ('DIPLOMA', 'Diploma', 3),
  ('UNDERGRADUATE', 'Undergraduate (UG)', 4),
  ('POSTGRADUATE', 'Postgraduate (PG)', 5),
  ('RESEARCH', 'Doctorate / Research (Ph.D)', 6),
  ('OTHER', 'Other', 7)
) as v(val, lbl, ord)
WHERE s.option_key = 'EDUCATION_LEVELS'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for COURSES
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('BTECH', 'Bachelor of Technology (B.Tech)', 1),
  ('BE', 'Bachelor of Engineering (B.E.)', 2),
  ('MBBS', 'Bachelor of Medicine, Bachelor of Surgery (MBBS)', 3),
  ('BDS', 'Bachelor of Dental Surgery (BDS)', 4),
  ('BPHARM', 'Bachelor of Pharmacy (B.Pharm)', 5),
  ('BSC', 'Bachelor of Science (B.Sc)', 6),
  ('BA', 'Bachelor of Arts (B.A.)', 7),
  ('BCOM', 'Bachelor of Commerce (B.Com)', 8),
  ('BBA', 'Bachelor of Business Administration (BBA)', 9),
  ('BCA', 'Bachelor of Computer Applications (BCA)', 10),
  ('DIPLOMA_ENGINEERING', 'Diploma in Engineering / Polytechnic', 11),
  ('ITI_COURSE', 'ITI Trade Course', 12),
  ('MTECH', 'Master of Technology (M.Tech)', 13),
  ('MSC', 'Master of Science (M.Sc)', 14),
  ('MBA', 'Master of Business Administration (MBA)', 15),
  ('OTHER', 'Other Course', 16)
) as v(val, lbl, ord)
WHERE s.option_key = 'COURSES'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for STUDY_YEARS
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('1', '1st Year', 1),
  ('2', '2nd Year', 2),
  ('3', '3rd Year', 3),
  ('4', '4th Year', 4),
  ('5', '5th Year', 5)
) as v(val, lbl, ord)
WHERE s.option_key = 'STUDY_YEARS'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for CASTE_CATEGORY
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('OPEN', 'General / Open', 1),
  ('OBC', 'Other Backward Class (OBC)', 2),
  ('SC', 'Scheduled Caste (SC)', 3),
  ('ST', 'Scheduled Tribe (ST)', 4),
  ('EWS', 'Economically Weaker Section (EWS)', 5),
  ('VJNT', 'Vimukta Jati & Nomadic Tribes (VJNT)', 6),
  ('SBC', 'Special Backward Category (SBC)', 7)
) as v(val, lbl, ord)
WHERE s.option_key = 'CASTE_CATEGORY'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for GENDER
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('MALE', 'Male', 1),
  ('FEMALE', 'Female', 2),
  ('TRANSGENDER', 'Transgender', 3),
  ('OTHER', 'Other / Prefer not to say', 4)
) as v(val, lbl, ord)
WHERE s.option_key = 'GENDER'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for ACCOMMODATION_TYPES
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('DAY_SCHOLAR', 'Day Scholar (Living with parents/relatives)', 1),
  ('HOSTELLER', 'Hosteller (College or Private Hostel / PG)', 2),
  ('OTHER', 'Other', 3)
) as v(val, lbl, ord)
WHERE s.option_key = 'ACCOMMODATION_TYPES'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for HOSTEL_TYPES
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('GOVERNMENT', 'Government Hostel', 1),
  ('INSTITUTE', 'Institute / College Run Hostel', 2),
  ('PRIVATE', 'Private Hostel / PG / Rented Room', 3)
) as v(val, lbl, ord)
WHERE s.option_key = 'HOSTEL_TYPES'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for INDIAN_STATES
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('MAHARASHTRA', 'Maharashtra', 1),
  ('DELHI', 'Delhi', 2),
  ('KARNATAKA', 'Karnataka', 3),
  ('TAMIL_NADU', 'Tamil Nadu', 4),
  ('GUJARAT', 'Gujarat', 5),
  ('UTTAR_PRADESH', 'Uttar Pradesh', 6),
  ('MADHYA_PRADESH', 'Madhya Pradesh', 7),
  ('RAJASTHAN', 'Rajasthan', 8),
  ('WEST_BENGAL', 'West Bengal', 9),
  ('ANDHRA_PRADESH', 'Andhra Pradesh', 10),
  ('TELANGANA', 'Telangana', 11),
  ('KERALA', 'Kerala', 12),
  ('BIHAR', 'Bihar', 13),
  ('PUNJAB', 'Punjab', 14),
  ('HARYANA', 'Haryana', 15),
  ('ALL_INDIA', 'All India / Central', 16)
) as v(val, lbl, ord)
WHERE s.option_key = 'INDIAN_STATES'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- SEED DATA: Options for DOCUMENT_TYPES
INSERT INTO field_options (option_set_id, option_value, option_label, sort_order)
SELECT s.id, v.val, v.lbl, v.ord
FROM field_option_sets s,
(VALUES 
  ('INCOME_CERTIFICATE', 'Income Certificate', 1),
  ('DOMICILE_CERTIFICATE', 'Domicile / Residence Certificate', 2),
  ('CASTE_CERTIFICATE', 'Caste / Category Certificate', 3),
  ('MARKSHEET', 'Previous Examination Marksheet', 4),
  ('ADMISSION_LETTER', 'College Admission Letter / Fee Receipt', 5),
  ('BONAFIDE_CERTIFICATE', 'Bonafide Certificate', 6),
  ('HOSTEL_CERTIFICATE', 'Hostel Certificate / Rent Agreement', 7),
  ('DISABILITY_CERTIFICATE', 'Disability / PwD Certificate', 8),
  ('OTHER', 'Other Official Document', 9)
) as v(val, lbl, ord)
WHERE s.option_key = 'DOCUMENT_TYPES'
ON CONFLICT (option_set_id, option_value) DO NOTHING;

-- ================================================================
-- SEED DATA: Master Fields (Datasets A)
-- ================================================================
INSERT INTO master_fields (field_key, label, section, data_type, options_key, default_required, display_condition, validation_rule, data_purpose) VALUES
-- Personal
('full_name', 'Full Name', 'PERSONAL', 'TEXT', NULL, TRUE, 'ALWAYS', 'NOT_EMPTY', 'IDENTIFICATION'),
('date_of_birth', 'Date of Birth', 'PERSONAL', 'DATE', NULL, TRUE, 'ALWAYS', 'VALID_DATE', 'AGE_ELIGIBILITY'),
('gender', 'Gender', 'PERSONAL', 'DROPDOWN', 'GENDER', FALSE, 'ALWAYS', 'VALID_OPTION', 'SCHEME_MATCHING_IF_REQUIRED'),
('marital_status', 'Marital Status', 'PERSONAL', 'DROPDOWN', 'MARITAL_STATUS', FALSE, 'ALWAYS', 'VALID_OPTION', 'SCHEME_MATCHING_IF_REQUIRED'),
('religion', 'Religion', 'PERSONAL', 'DROPDOWN', 'RELIGION', FALSE, 'ALWAYS', 'VALID_OPTION', 'SCHEME_MATCHING_IF_REQUIRED'),
('mobile_number', 'Mobile Number', 'PERSONAL', 'PHONE', NULL, TRUE, 'ACCOUNT_SETUP', 'VALID_PHONE', 'CONTACT'),
('email', 'Email Address', 'PERSONAL', 'EMAIL', NULL, TRUE, 'ACCOUNT_SETUP', 'VALID_EMAIL', 'CONTACT'),

-- Address and Domicile
('permanent_state', 'Permanent State', 'ADDRESS', 'DROPDOWN', 'INDIAN_STATES', TRUE, 'ALWAYS', 'VALID_OPTION', 'RESIDENCE'),
('permanent_district', 'Permanent District', 'ADDRESS', 'DROPDOWN', NULL, TRUE, 'ALWAYS', 'VALID_OPTION', 'RESIDENCE'),
('permanent_pincode', 'Permanent PIN Code', 'ADDRESS', 'TEXT', NULL, TRUE, 'ALWAYS', 'VALID_PINCODE', 'RESIDENCE'),
('domicile_state', 'Domicile State', 'ADDRESS', 'DROPDOWN', 'INDIAN_STATES', FALSE, 'ALWAYS', 'VALID_OPTION', 'DOMICILE_ELIGIBILITY'),
('has_domicile_certificate', 'Have Domicile Certificate', 'ADDRESS', 'BOOLEAN', NULL, FALSE, 'IF_REQUIRED', 'VALID_BOOLEAN', 'DOCUMENT_READINESS'),
('correspondence_address', 'Correspondence Address', 'ADDRESS', 'TEXT', NULL, FALSE, 'IF_NEEDED', 'VALID_LENGTH', 'CONTACT'),

-- Family and Financial
('annual_family_income', 'Annual Family Income (in INR)', 'FAMILY', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'NON_NEGATIVE', 'INCOME_ELIGIBILITY'),
('income_certificate_available', 'Income Certificate Available', 'FAMILY', 'BOOLEAN', NULL, FALSE, 'IF_REQUIRED', 'VALID_BOOLEAN', 'DOCUMENT_READINESS'),
('father_occupation', 'Father or Guardian Occupation', 'FAMILY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'FAMILY_CONDITION'),
('mother_occupation', 'Mother Occupation', 'FAMILY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'FAMILY_CONDITION'),
('siblings_count', 'Number of Siblings', 'FAMILY', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'NON_NEGATIVE', 'FAMILY_CONDITION'),

-- Social Category & Special Eligibility
('caste_category', 'Social Category', 'SPECIAL_ELIGIBILITY', 'DROPDOWN', 'CASTE_CATEGORY', FALSE, 'IF_RELEVANT', 'VALID_OPTION', 'SCHEME_MATCHING'),
('caste_name', 'Caste Name', 'SPECIAL_ELIGIBILITY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'SCHEME_MATCHING'),
('sub_caste', 'Sub-caste', 'SPECIAL_ELIGIBILITY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'SCHEME_MATCHING'),
('disability_status', 'Disability Status (PwD)', 'SPECIAL_ELIGIBILITY', 'BOOLEAN', NULL, FALSE, 'IF_RELEVANT', 'VALID_BOOLEAN', 'SCHEME_MATCHING'),
('disability_type', 'Disability Type', 'SPECIAL_ELIGIBILITY', 'DROPDOWN', 'DISABILITY_TYPES', FALSE, 'DISABILITY_STATUS_TRUE', 'VALID_OPTION', 'SCHEME_MATCHING'),

-- Current Education
('education_level', 'Current Education Level', 'EDUCATION', 'DROPDOWN', 'EDUCATION_LEVELS', TRUE, 'ALWAYS', 'VALID_OPTION', 'COURSE_MATCHING'),
('course_name', 'Current Course', 'EDUCATION', 'DROPDOWN', 'COURSES', TRUE, 'EDUCATION_SELECTED', 'VALID_OPTION', 'COURSE_MATCHING'),
('specialization', 'Branch or Specialization', 'EDUCATION', 'DROPDOWN', NULL, FALSE, 'IF_APPLICABLE', 'VALID_OPTION', 'COURSE_MATCHING'),
('current_year', 'Current Year of Study', 'EDUCATION', 'DROPDOWN', 'STUDY_YEARS', TRUE, 'EDUCATION_SELECTED', 'VALID_OPTION', 'COURSE_MATCHING'),
('admission_year', 'Admission Year', 'EDUCATION', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'VALID_YEAR', 'ACADEMIC_ELIGIBILITY'),
('institution_name', 'College or Institution Name', 'EDUCATION', 'SEARCH', NULL, TRUE, 'EDUCATION_SELECTED', 'VALID_INSTITUTION', 'INSTITUTION_ELIGIBILITY'),
('university_name', 'University Name', 'EDUCATION', 'SEARCH', NULL, FALSE, 'IF_REQUIRED', 'VALID_INSTITUTION', 'INSTITUTION_ELIGIBILITY'),
('admission_type', 'Admission Type', 'EDUCATION', 'DROPDOWN', 'ADMISSION_TYPES', FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'ADMISSION_ELIGIBILITY'),

-- Previous Academic Performance
('previous_qualification', 'Previous Qualification', 'ACADEMIC_HISTORY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'MERIT_ELIGIBILITY'),
('previous_percentage', 'Previous Examination Percentage (or % equivalent)', 'ACADEMIC_HISTORY', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'RANGE_0_TO_100', 'MERIT_ELIGIBILITY'),
('passing_year', 'Passing Year', 'ACADEMIC_HISTORY', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'VALID_YEAR', 'MERIT_ELIGIBILITY'),
('previous_result', 'Previous Examination Result', 'ACADEMIC_HISTORY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'ACADEMIC_ELIGIBILITY'),
('education_gap', 'Education Gap (in years)', 'ACADEMIC_HISTORY', 'BOOLEAN', NULL, FALSE, 'IF_REQUIRED', 'VALID_BOOLEAN', 'SCHEME_MATCHING'),
('entrance_exam_name', 'Entrance Examination', 'ACADEMIC_HISTORY', 'DROPDOWN', NULL, FALSE, 'IF_REQUIRED', 'VALID_OPTION', 'ADMISSION_ELIGIBILITY'),
('entrance_exam_score', 'Entrance Examination Score / Rank', 'ACADEMIC_HISTORY', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'VALID_SCORE', 'ADMISSION_ELIGIBILITY'),

-- Accommodation
('accommodation_type', 'Accommodation Type', 'ACCOMMODATION', 'DROPDOWN', 'ACCOMMODATION_TYPES', FALSE, 'ALWAYS', 'VALID_OPTION', 'ACCOMMODATION_ELIGIBILITY'),
('hostel_name', 'Hostel Name', 'ACCOMMODATION', 'TEXT', NULL, FALSE, 'ACCOMMODATION_HOSTELLER', 'VALID_LENGTH', 'DOCUMENT_READINESS'),
('hostel_type', 'Hostel Type', 'ACCOMMODATION', 'DROPDOWN', 'HOSTEL_TYPES', FALSE, 'ACCOMMODATION_HOSTELLER', 'VALID_OPTION', 'ACCOMMODATION_ELIGIBILITY'),
('monthly_hostel_rent', 'Monthly Hostel Rent / Expenses', 'ACCOMMODATION', 'NUMBER', NULL, FALSE, 'IF_REQUIRED', 'NON_NEGATIVE', 'EXPENSE_ESTIMATION'),
('hostel_certificate_available', 'Hostel Certificate Available', 'ACCOMMODATION', 'BOOLEAN', NULL, FALSE, 'IF_REQUIRED', 'VALID_BOOLEAN', 'DOCUMENT_READINESS'),

-- Documents
('document_type', 'Document Type', 'DOCUMENTS', 'DROPDOWN', 'DOCUMENT_TYPES', FALSE, 'WHEN_NEEDED', 'VALID_OPTION', 'DOCUMENT_READINESS'),
('document_available', 'Document Available', 'DOCUMENTS', 'BOOLEAN', NULL, FALSE, 'WHEN_NEEDED', 'VALID_BOOLEAN', 'DOCUMENT_READINESS'),
('document_verification_status', 'Document Verification Status', 'DOCUMENTS', 'DROPDOWN', NULL, FALSE, 'AFTER_UPLOAD', 'VALID_OPTION', 'VERIFICATION')
ON CONFLICT (field_key) DO UPDATE SET
  label = EXCLUDED.label,
  section = EXCLUDED.section,
  data_type = EXCLUDED.data_type,
  options_key = EXCLUDED.options_key,
  default_required = EXCLUDED.default_required,
  display_condition = EXCLUDED.display_condition,
  validation_rule = EXCLUDED.validation_rule,
  data_purpose = EXCLUDED.data_purpose;

-- ================================================================
-- SEED DATA: Conditional Rules (Dataset C)
-- ================================================================
INSERT INTO conditional_rules (field_key, condition_json, action, rule_code, description) VALUES
('course_name', '{"all":[{"field":"education_level","operator":"NOT_EMPTY"}]}', 'SHOW', 'R001', 'Display course selection when education level is selected'),
('specialization', '{"all":[{"field":"course_name","operator":"IN","value":["BTECH","BE","MTECH","DIPLOMA_ENGINEERING"]}]}', 'SHOW', 'R002', 'Display branch/specialization when technical course is chosen'),
('hostel_name', '{"all":[{"field":"accommodation_type","operator":"EQUALS","value":"HOSTELLER"}]}', 'SHOW', 'R003A', 'Display hostel name when student is a hosteller'),
('hostel_type', '{"all":[{"field":"accommodation_type","operator":"EQUALS","value":"HOSTELLER"}]}', 'SHOW', 'R003B', 'Display hostel type when student is a hosteller'),
('monthly_hostel_rent', '{"all":[{"field":"accommodation_type","operator":"EQUALS","value":"HOSTELLER"}]}', 'SHOW', 'R003C', 'Display hostel rent when student is a hosteller'),
('disability_type', '{"all":[{"field":"disability_status","operator":"EQUALS","value":true}]}', 'SHOW', 'R004', 'Display disability details when disability status is Yes'),
('annual_family_income', '{"all":[{"field":"scholarship_requires_income","operator":"EQUALS","value":true}]}', 'SHOW', 'R005', 'Ask for annual family income when candidate schemes require income criteria'),
('previous_percentage', '{"all":[{"field":"scholarship_requires_merit","operator":"EQUALS","value":true}]}', 'SHOW', 'R006', 'Ask for previous examination marks when candidate schemes evaluate merit'),
('has_domicile_certificate', '{"all":[{"field":"domicile_state","operator":"NOT_EMPTY"}]}', 'SHOW', 'R007', 'Ask about domicile certificate when state quota is applicable'),
('entrance_exam_score', '{"all":[{"field":"entrance_exam_name","operator":"NOT_EMPTY"}]}', 'SHOW', 'R008', 'Ask for entrance score when entrance exam name is provided')
ON CONFLICT (rule_code) DO UPDATE SET
  field_key = EXCLUDED.field_key,
  condition_json = EXCLUDED.condition_json,
  action = EXCLUDED.action,
  description = EXCLUDED.description;

-- ================================================================
-- SEED DATA: Verified Scholarship Requirements (Dataset D)
-- Representative verified schemes from official portals (MahaDBT & Central NSP)
-- ================================================================
-- Scheme 1: Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Yojna (EBC Maharashtra)
INSERT INTO scholarship_requirements 
(scholarship_key, scholarship_name, academic_year, condition_key, field_key, operator, expected_value, mandatory, source_url, source_document, source_section, verification_status)
VALUES
('MAHA_EBC_01', 'Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Yojna (EBC)', '2026-27', 'DOMICILE_MAHA', 'domicile_state', 'EQUALS', 'MAHARASHTRA', TRUE, 'https://mahadbt.maharashtra.gov.in', 'Directorate of Higher Education Guideline', 'Eligibility Criteria Sec 1', 'VERIFIED'),
('MAHA_EBC_01', 'Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Yojna (EBC)', '2026-27', 'INCOME_MAX_8L', 'annual_family_income', 'LESS_THAN_OR_EQUAL', '800000', TRUE, 'https://mahadbt.maharashtra.gov.in', 'GR No. EBC-2016/CR-221', 'Income Threshold Clause', 'VERIFIED'),
('MAHA_EBC_01', 'Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Yojna (EBC)', '2026-27', 'MIN_MARKS_50', 'previous_percentage', 'GREATER_THAN_OR_EQUAL', '50.0', TRUE, 'https://mahadbt.maharashtra.gov.in', 'Directorate of Technical Education Guidelines', 'Academic Merit Clause', 'VERIFIED'),
('MAHA_EBC_01', 'Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Yojna (EBC)', '2026-27', 'COURSE_PROFESSIONAL', 'education_level', 'IN', '["UNDERGRADUATE","POSTGRADUATE"]', TRUE, 'https://mahadbt.maharashtra.gov.in', 'DHE Scheme Details', 'Applicable Courses', 'VERIFIED')
ON CONFLICT (scholarship_key, condition_key, field_key) DO UPDATE SET
  operator = EXCLUDED.operator,
  expected_value = EXCLUDED.expected_value,
  mandatory = EXCLUDED.mandatory,
  verification_status = EXCLUDED.verification_status;

-- Scheme 2: Post Matric Scholarship for SC Students (Government of India / MahaDBT)
INSERT INTO scholarship_requirements 
(scholarship_key, scholarship_name, academic_year, condition_key, field_key, operator, expected_value, mandatory, source_url, source_document, source_section, verification_status)
VALUES
('POST_MATRIC_SC_01', 'Post Matric Scholarship for SC Students', '2026-27', 'CATEGORY_SC', 'caste_category', 'EQUALS', 'SC', TRUE, 'https://scholarships.gov.in', 'Ministry of Social Justice Guidelines', 'Eligibility Criteria', 'VERIFIED'),
('POST_MATRIC_SC_01', 'Post Matric Scholarship for SC Students', '2026-27', 'INCOME_MAX_2_5L', 'annual_family_income', 'LESS_THAN_OR_EQUAL', '250000', TRUE, 'https://scholarships.gov.in', 'MoSJE Official Notification', 'Income Ceiling Clause', 'VERIFIED'),
('POST_MATRIC_SC_01', 'Post Matric Scholarship for SC Students', '2026-27', 'EDU_POST_MATRIC', 'education_level', 'IN', '["DIPLOMA","UNDERGRADUATE","POSTGRADUATE","RESEARCH"]', TRUE, 'https://scholarships.gov.in', 'MoSJE Guidelines', 'Course Level', 'VERIFIED')
ON CONFLICT (scholarship_key, condition_key, field_key) DO UPDATE SET
  operator = EXCLUDED.operator,
  expected_value = EXCLUDED.expected_value,
  mandatory = EXCLUDED.mandatory,
  verification_status = EXCLUDED.verification_status;

-- Scheme 3: AICTE Pragati Scholarship for Girl Students (Technical Degree)
INSERT INTO scholarship_requirements 
(scholarship_key, scholarship_name, academic_year, condition_key, field_key, operator, expected_value, mandatory, source_url, source_document, source_section, verification_status)
VALUES
('AICTE_PRAGATI_01', 'AICTE Pragati Scholarship for Girl Students (Degree)', '2026-27', 'GENDER_FEMALE', 'gender', 'EQUALS', 'FEMALE', TRUE, 'https://www.aicte-india.org', 'AICTE Pragati Scheme Guidelines', 'Clause 1: Eligibility', 'VERIFIED'),
('AICTE_PRAGATI_01', 'AICTE Pragati Scholarship for Girl Students (Degree)', '2026-27', 'INCOME_MAX_8L', 'annual_family_income', 'LESS_THAN_OR_EQUAL', '800000', TRUE, 'https://www.aicte-india.org', 'AICTE Notification', 'Clause 2: Family Income', 'VERIFIED'),
('AICTE_PRAGATI_01', 'AICTE Pragati Scholarship for Girl Students (Degree)', '2026-27', 'COURSE_TECHNICAL', 'course_name', 'IN', '["BTECH","BE","BPHARM"]', TRUE, 'https://www.aicte-india.org', 'AICTE Guidelines', 'Eligible Technical Programs', 'VERIFIED'),
('AICTE_PRAGATI_01', 'AICTE Pragati Scholarship for Girl Students (Degree)', '2026-27', 'ADMISSION_FIRST_YEAR', 'current_year', 'EQUALS', '1', TRUE, 'https://www.aicte-india.org', 'AICTE Guidelines', 'Year of Admission Clause', 'VERIFIED')
ON CONFLICT (scholarship_key, condition_key, field_key) DO UPDATE SET
  operator = EXCLUDED.operator,
  expected_value = EXCLUDED.expected_value,
  mandatory = EXCLUDED.mandatory,
  verification_status = EXCLUDED.verification_status;
