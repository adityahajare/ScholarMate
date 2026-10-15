import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import OnboardingVerification from "./pages/auth/OnboardingVerification";

import Dashboard from "./pages/student/Dashboard";
import Profile from "./pages/student/Profile";
import Scholarships from "./pages/student/Scholarships";
import ScholarshipDetails from "./pages/student/ScholarshipDetails";
import Recommendations from "./pages/student/Recommendations";
import Prediction from "./pages/student/Prediction";
import Improvements from "./pages/student/Improvements";
import Applications from "./pages/student/Applications";
import ApplicationDetails from "./pages/student/ApplicationDetails";
import ApplicationForm from "./pages/student/ApplicationForm";
import ApplicationSuccess from "./pages/student/ApplicationSuccess";
import Reports from "./pages/student/Reports";
import NotFound from "./pages/NotFound";


import MainLayout from "./components/layout/MainLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import EducationTypeSelector from "./pages/profile/EducationTypeSelector";
import ProfileBuilderLayout from "./pages/profile/ProfileBuilderLayout";
import PersonalInfoForm from "./pages/profile/steps/PersonalInfoForm";
import ReviewSubmit from "./pages/profile/steps/ReviewSubmit";

import LandingPage from "./pages/LandingPage";
import Documents from "./pages/student/Documents";
import ForgotPassword from "./pages/auth/ForgotPassword";
import ResetPassword from "./pages/auth/ResetPassword";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Landing Page */}
        <Route path="/" element={<LandingPage />} />

        {/* Authentication */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/onboarding/verify" element={<ProtectedRoute><OnboardingVerification /></ProtectedRoute>} />

        {/* User Application */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Dashboard />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Profile Builder & Profile Routes */}
        <Route path="/profile/education" element={<ProtectedRoute><EducationTypeSelector /></ProtectedRoute>} />
        <Route path="/profile/builder" element={<ProtectedRoute><ProfileBuilderLayout /></ProtectedRoute>}>
          <Route index element={<PersonalInfoForm />} />
          <Route path="review" element={<ReviewSubmit />} />
        </Route>
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Profile />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Documents Vault */}
        <Route
          path="/documents"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Documents />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/scholarships"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Scholarships />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/scholarships/:id"
          element={
            <ProtectedRoute>
              <MainLayout>
                <ScholarshipDetails />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/recommendations"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Recommendations />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/prediction"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Prediction />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/improvements"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Improvements />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/applications"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Applications />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/applications/:id"
          element={
            <ProtectedRoute>
              <MainLayout>
                <ApplicationDetails />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/scholarships/:id/apply"
          element={
            <ProtectedRoute>
              <MainLayout>
                <ApplicationForm />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/application-success"
          element={
            <ProtectedRoute>
              <MainLayout>
                <ApplicationSuccess />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/reports"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Reports />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Fallback for unknown routes */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
