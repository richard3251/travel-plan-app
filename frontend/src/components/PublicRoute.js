import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const PublicRoute = ({ children, redirectTo = '/' }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  // 로딩 중일 때는 로딩 화면 표시
  if (loading) {
    return (
      <div className="loading">
        <div className="loading-spinner">
          <div className="spinner"></div>
        </div>
        <p>인증 정보를 확인하는 중...</p>
      </div>
    );
  }

  // 이미 인증된 경우 지정된 경로로 리디렉트
  if (isAuthenticated) {
    const from = location.state?.from?.pathname || redirectTo;
    return <Navigate to={from} replace />;
  }

  // 인증되지 않은 경우 자식 컴포넌트 렌더링
  return children;
};

export default PublicRoute;