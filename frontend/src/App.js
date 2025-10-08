import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Header from './components/Header';
import ProtectedRoute from './components/ProtectedRoute';
import PublicRoute from './components/PublicRoute';
import TripList from './components/TripList';
import NewTripPage from './pages/NewTripPage';
import TripDetailPage from './pages/TripDetailPage';
import EditTripPage from './pages/EditTripPage';
import FileUploadDemo from './pages/FileUploadDemo';
import SharedTripPage from './pages/SharedTripPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  );
}

function AppContent() {
  const authContext = useAuth();

  useEffect(() => {
    // AuthContext를 API 클라이언트에 연결 (동적 import 사용)
    import('./api/api').then(({ setAuthContextRef }) => {
      setAuthContextRef(authContext);
    });
  }, [authContext]);

  return (
    <div className="app">
      <Header />
      <main className="main-content">
        <Routes>
          {/* 공개 라우트 (인증되지 않은 사용자만 접근 가능) */}
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/signup" 
            element={
              <PublicRoute>
                <SignupPage />
              </PublicRoute>
            } 
          />
          
          {/* 보호된 라우트 (인증된 사용자만 접근 가능) */}
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <TripList />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/trips/new" 
            element={
              <ProtectedRoute>
                <NewTripPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/trips/:tripId" 
            element={
              <ProtectedRoute>
                <TripDetailPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/trips/edit/:tripId" 
            element={
              <ProtectedRoute>
                <EditTripPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/demo/file-upload" 
            element={
              <ProtectedRoute>
                <FileUploadDemo />
              </ProtectedRoute>
            } 
          />
          
          {/* 공유된 여행 보기 (인증 불필요) */}
          <Route 
            path="/shared/:shareToken" 
            element={<SharedTripPage />} 
          />
          
          {/* 404 페이지나 기본 리디렉트 */}
          <Route 
            path="*" 
            element={
              <ProtectedRoute>
                <TripList />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </main>
    </div>
  );
}

export default App; 