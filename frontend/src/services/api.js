
import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  withXSRFToken: true,
});

// Obtain the CSRF cookie before every request that changes data.
api.interceptors.request.use(async (config) => {
  const method = (config.method || "get").toLowerCase();

  const safeMethods = ["get", "head", "options"];

  if (!safeMethods.includes(method) && !config.url?.endsWith("/auth/csrf")) {
    await api.get("/auth/csrf");
  }

  return config;
});

export default api;
