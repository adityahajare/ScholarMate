import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const credentials = localStorage.getItem("scholarmate_credentials");
  if (credentials) {
    config.headers.Authorization = `Basic ${credentials}`;
  }
  return config;
});

export const authApi = {
  login: (email, password) => api.post("/auth/login", { email, password }),
  register: (payload) => api.post("/auth/register", payload),
};

export const scholarshipApi = {
  list: () => api.get("/scholarships"),
  search: ({ query = "", page = 0, size = 20 } = {}) => api.get("/scholarships/search", { params: { q: query, page, size } }),
  get: (id) => api.get(`/scholarships/${id}`),
  catalogStatus: () => api.get("/scholarships/catalog-status"),
};

export const profileApi = {
  getMine: () => api.get("/profiles/me"),
  updateMine: (profile) => api.put("/profiles/me", profile),
};

export const educationApi = {
  list: () => api.get('/profile/education'),
  create: (record) => api.post('/profile/education', record),
  update: (id, record) => api.put(`/profile/education/${id}`, record),
  remove: (id) => api.delete(`/profile/education/${id}`),
};

export const eligibilityApi = {
  evaluateMine: (scholarshipId) => api.get(`/eligibility/scholarships/${scholarshipId}/me`),
};

export const requirementsApi = {
  get: (scholarshipId) => api.get(`/profile/requirements/${scholarshipId}`),
};

export const profileChildApi = {
  family: {
    list: () => api.get('/profile/family'),
    create: (payload) => api.post('/profile/family', payload),
    update: (id, payload) => api.put(`/profile/family/${id}`, payload),
    remove: (id) => api.delete(`/profile/family/${id}`),
  },
  addresses: {
    list: () => api.get('/profile/addresses'),
    create: (payload) => api.post('/profile/addresses', payload),
    update: (id, payload) => api.put(`/profile/addresses/${id}`, payload),
    remove: (id) => api.delete(`/profile/addresses/${id}`),
  },
  documents: {
    list: () => api.get('/profile/documents'),
    create: (payload) => api.post('/profile/documents', payload),
    upload: (documentType, file, onUploadProgress) => {
      const body = new FormData();
      body.append('documentType', documentType);
      body.append('file', file);
      return api.post('/profile/documents/upload', body, { headers: { 'Content-Type': 'multipart/form-data' }, onUploadProgress });
    },
    download: (id) => api.get(`/profile/documents/${id}/download`, { responseType: 'blob' }),
    update: (id, payload) => api.put(`/profile/documents/${id}`, payload),
    remove: (id) => api.delete(`/profile/documents/${id}`),
  },
};

export const recommendationApi = {
  getMine: () => api.get("/ml/recommendations/me"),
};

export const mlApi = {
  status: () => api.get('/ml/recommendations/status'),
};

export const documentReviewApi = {
  reviewText: (text) => api.post("/profile-documents/me/review/text", { text }),
  reviewFile: (file) => {
    const body = new FormData();
    body.append("file", file);
    return api.post("/profile-documents/me/review/file", body, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  history: () => api.get("/profile-documents/me/reviews"),
};

export default api;
