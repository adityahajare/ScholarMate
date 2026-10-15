export const SCHOLARSHIPS = [
  {
    id: "aicte-pragati-demo",
    name: "AICTE Pragati Scholarship",
    provider: "AICTE",

    description:
      "Scholarship opportunity listed through the National Scholarship Portal.",

    benefitAmount: 50000,
    benefitPeriod: "per year",
    deadline: "30 Sep 2026",

    // Official portal links
    applicationPortal: "National Scholarship Portal",
    officialApplyUrl: "https://scholarships.gov.in/Students",
    officialDetailsUrl: "https://scholarships.gov.in/All-Scholarships",

    applicationInstructions:
      "Find the AICTE Pragati Scholarship Scheme for Girl Students (Technical Degree) on the official portal and review its eligibility requirements before applying.",

    eligibilityVerified: false,
    dataStatus: "demo",
    lastVerifiedAt: null,

    criteria: {
      educationLevels: [],
      branches: [],
      minimumScore: null,
      scoreType: null,
      maximumAnnualFamilyIncome: null,
      categories: [],
      states: [],
      genders: [],
      disabilityStatus: null,
    },

    requiredDocuments: [],
  },

  {
    id: "national-scholarship-demo",
    name: "National Scholarship",
    provider: "Government of India",

    description:
      "Explore scholarship schemes available through the National Scholarship Portal.",

    benefitAmount: 30000,
    benefitPeriod: "per year",
    deadline: "15 Oct 2026",

    // Official portal links
    applicationPortal: "National Scholarship Portal",
    officialApplyUrl: "https://scholarships.gov.in/Students",
    officialDetailsUrl: "https://scholarships.gov.in/All-Scholarships",

    applicationInstructions:
      "Search for the exact scholarship scheme on the official portal and check its current application requirements.",

    eligibilityVerified: false,
    dataStatus: "demo",
    lastVerifiedAt: null,

    criteria: {
      educationLevels: [],
      branches: [],
      minimumScore: null,
      scoreType: null,
      maximumAnnualFamilyIncome: null,
      categories: [],
      states: [],
      genders: [],
      disabilityStatus: null,
    },

    requiredDocuments: [],
  },

  {
    id: "maharashtra-state-scholarship-demo",
    name: "Maharashtra State Scholarship",
    provider: "Maharashtra State Government",

    description:
      "Explore Maharashtra scholarship schemes through the official MahaDBT portal.",

    benefitAmount: 25000,
    benefitPeriod: "per year",
    deadline: "31 Oct 2026",

    // Official portal links
    applicationPortal: "MahaDBT 2.0",
    officialApplyUrl: "https://mahadbt2.maharashtra.gov.in/",
    officialDetailsUrl: "https://mahadbt2.maharashtra.gov.in/",

    applicationInstructions:
      "Open MahaDBT 2.0, sign in or register as required, and find the exact scholarship scheme that applies to your profile.",

    eligibilityVerified: false,
    dataStatus: "demo",
    lastVerifiedAt: null,

    criteria: {
      educationLevels: [],
      branches: [],
      minimumScore: null,
      scoreType: null,
      maximumAnnualFamilyIncome: null,
      categories: [],
      states: [],
      genders: [],
      disabilityStatus: null,
    },

    requiredDocuments: [],
  },
];

export function getAllScholarships() {
  return SCHOLARSHIPS;
}

export function getScholarshipById(id) {
  return SCHOLARSHIPS.find((scholarship) => scholarship.id === id) || null;
}

const API_BASE_URL = import.meta.env.VITE_API_URL || "";

function formatDate(value) {
  if (!value) return "";

  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? String(value)
    : date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      });
}

function normalizeScholarship(record) {
  return {
    id: String(record.id),
    name: record.name || "Unnamed scholarship",
    provider: record.provider || "Unknown provider",
    description: record.description || "Scholarship details are being verified.",
    benefitAmount: record.amount,
    benefitPeriod: "",
    deadline: formatDate(record.applicationDeadline),
    officialApplyUrl: record.applicationUrl || "",
    officialDetailsUrl: record.sourceUrl || record.specificationUrl || "",
    applicationInstructions: "Review the official scholarship website before applying.",
    category: record.category || "",
    criteria: {
      branches: record.course ? [record.course] : [],
      states: record.state ? [record.state] : [],
      categories: record.eligibleCategories
        ? [record.eligibleCategories]
        : [],
      minimumScore: record.minimumCgpa,
      scoreType: record.minimumCgpa != null ? "CGPA" : null,
      maximumAnnualFamilyIncome: record.maximumFamilyIncome,
    },
    requiredDocuments: [],
    dataStatus: "scraped",
  };
}

export async function fetchAllScholarships() {
  const response = await fetch(`${API_BASE_URL}/api/scholarships`);
  if (!response.ok) {
    throw new Error(`Unable to load scholarships (${response.status})`);
  }

  const records = await response.json();
  return Array.isArray(records) ? records.map(normalizeScholarship) : [];
}

export async function fetchScholarshipById(id) {
  const response = await fetch(`${API_BASE_URL}/api/scholarships/${id}`);
  if (!response.ok) {
    throw new Error(`Unable to load scholarship (${response.status})`);
  }

  return normalizeScholarship(await response.json());
}
