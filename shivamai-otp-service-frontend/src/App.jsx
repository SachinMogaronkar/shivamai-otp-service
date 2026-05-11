import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

/* ================= AUTH ================= */
import Auth from "./pages/Auth";
import VerifyOtp from "./pages/VerifyOtp";
import VerifyLogin from "./pages/VerifyLogin";

/* ================= LAYOUTS ================= */
import AdminLayout from "./layout/AdminLayout";
import UserLayout from "./layout/UserLayout";
import ProtectedRoute from "./components/ProtectedRoute";

/* ================= ADMIN ================= */
import AdminDashboard from "./pages/AdminDashboard";
import PendingDevelopers from "./pages/PendingDevelopers";
import AllDevelopers from "./pages/AllDevelopers";
import AdminApps from "./pages/AdminApps";
import AdminDeveloperDetails from "./pages/AdminDeveloperDetails";
import AdminAppDetails from "./pages/AdminAppDetails";
import WebhookLogs from "./pages/WebhookLogs";
import AdminUsage from "./pages/AdminUsage";

/* ================= USER ================= */
import Dashboard from "./pages/Dashboard";
import CreateApp from "./pages/CreateApp";
import Apps from "./pages/Apps";
import ClientTest from "./pages/ClientTest";
import DeveloperProfile from "./pages/DeveloperProfile";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* ================= AUTH ================= */}

        <Route path="/auth" element={<Auth />} />
        <Route path="/login" element={<Navigate to="/auth" />} />
        <Route path="/register" element={<Navigate to="/auth" />} />
        <Route path="/verify" element={<VerifyOtp />} />
        <Route path="/verify-login" element={<VerifyLogin />} />

        {/* ================= ADMIN ================= */}

        <Route
          path="/admin"
          element={
            <ProtectedRoute role="ADMIN">
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          {/* 🔥 DEFAULT */}
          <Route index element={<Navigate to="dashboard" replace />} />

          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="pending" element={<PendingDevelopers />} />
          <Route path="all" element={<AllDevelopers />} />
          <Route path="apps" element={<AdminApps />} />
          <Route path="developers/:id" element={<AdminDeveloperDetails />} />
          <Route path="apps/:id" element={<AdminAppDetails />} />
          <Route path="webhooks" element={<WebhookLogs />} />
          <Route path="usage" element={<AdminUsage />} />
        </Route>

        {/* ================= DEVELOPER ================= */}

        <Route
          element={
            <ProtectedRoute role="DEVELOPER">
              <UserLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/apps" element={<Apps />} />
          <Route path="/create-app" element={<CreateApp />} />
          <Route path="/profile" element={<DeveloperProfile />} />
        </Route>

        {/* ================= CLIENT TOOL ================= */}

        <Route path="/client-test" element={<ClientTest />} />

        {/* ================= DEFAULT ================= */}

        <Route path="/" element={<Navigate to="/auth" />} />
        <Route path="*" element={<Navigate to="/auth" />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;