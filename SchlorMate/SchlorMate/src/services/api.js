const API_BASE_URL = import.meta.env.DEV
  ? import.meta.env.VITE_API_URL || "https://scholarmate-backend1.onrender.com"
  : "";

async function request(path, options = {}) {
  const token = localStorage.getItem("scholarmate_access_token");
  const headers = new Headers(options.headers || {});
  headers.set("Content-Type", "application/json");

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(body?.message || body?.error || `Request failed (${response.status})`);
  }

  return body;
}

export function loginUser(email, password) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function registerUser(payload) {
  return request("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getMyRecommendations() {
  return request("/api/ml/recommendations/me");
}

export function getMlStatus() {
  return request("/api/ml/recommendations/status");
}
