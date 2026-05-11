import api from "../api/axios";

export const createApp = (data) =>
  api.post("/developer/dashboard/apps", data);

export const getApps = () =>
  api.get("/developer/dashboard/apps");

export const rotateSecret = (id) =>
  api.patch(`/developer/dashboard/apps/${id}/rotate-secret`);

export const deleteApp = (id) =>
  api.delete(`/developer/dashboard/apps/${id}`);

/* ================= PROFILE ================= */

export const getProfile = () =>
  api.get("/developer/dashboard/profile");