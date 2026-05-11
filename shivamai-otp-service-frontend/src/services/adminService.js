import api from "../api/axios";

/* ================= AUTH ================= */

export const adminLogin = (data) =>
  api.post("/admin/auth/login", data);

export const adminLogout = () =>
  api.post("/admin/auth/logout");

/* ================= DEVELOPERS ================= */

export const getPendingDevelopers = () =>
  api.get("/admin/developers/pending");

export const getAllDevelopers = () =>
  api.get("/admin/developers");

export const getDeveloperById = (id) =>
  api.get(`/admin/developers/${id}`);

export const approveDeveloper = (id) =>
  api.patch(`/admin/developers/${id}/approve`);

export const suspendDeveloper = (id) =>
  api.patch(`/admin/developers/${id}/suspend`);

/* ================= APPS ================= */

export const getAdminApps = () =>
  api.get("/admin/apps");

export const getAdminAppById = (id) =>
  api.get(`/admin/apps/${id}`);

export const activateApp = (id) =>
  api.patch(`/admin/apps/${id}/activate`);

export const suspendApp = (id) =>
  api.patch(`/admin/apps/${id}/suspend`);

/* ================= WEBHOOKS ================= */

export const getWebhookLogs = () =>
  api.get("/admin/webhooks");

/* ================= USAGE ================= */

export const getUsage = () =>
  api.get("/admin/usage");

export const getUsageByClient = (clientId) =>
  api.get(`/admin/usage/${clientId}`);

/* ================= SYSTEM STATUS ================= */

export const getSystemStatus = () =>
  api.get("/status");