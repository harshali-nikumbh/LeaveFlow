
import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  withXSRFToken: true,
});

api.interceptors.request.use(async (config) => {
  const method = (config.method || "get").toLowerCase();
  const safeMethods = ["get", "head", "options"];

  if (
    !safeMethods.includes(method) &&
    !config.url?.endsWith("/auth/csrf")
  ) {
    const response = await api.get("/auth/csrf");

    config.headers["X-XSRF-TOKEN"] = response.data.token;
  }

  return config;
});

export default api;
