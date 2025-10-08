import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Header.css';

const Header = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [showUserMenu, setShowUserMenu] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
    setShowUserMenu(false);
  };

  const toggleUserMenu = () => {
    setShowUserMenu(!showUserMenu);
  };

  return (
    <header className="header">
      <Link to="/" className="logo">
        planscanner
      </Link>
      
      {isAuthenticated && (
        <div className="nav-links">
          <Link to="/">내 여행 목록</Link>
          <Link to="/demo/file-upload">파일 업로드 데모</Link>
        </div>
      )}
      
      <div className="user-actions">
        {isAuthenticated ? (
          <>
            <Link to="/trips/new">
              <button className="new-trip-button">새 여행 만들기</button>
            </Link>
            <div className="user-menu">
              <button className="user-menu-button" onClick={toggleUserMenu}>
                <span className="user-avatar">
                  {user?.nickname?.charAt(0)?.toUpperCase() || 'U'}
                </span>
                <span className="user-name">{user?.nickname || '사용자'}</span>
                <span className="dropdown-arrow">▼</span>
              </button>
              
              {showUserMenu && (
                <div className="user-menu-dropdown">
                  <div className="user-info">
                    <p className="user-email">{user?.email}</p>
                    <p className="user-nickname">{user?.nickname}</p>
                  </div>
                  <hr className="menu-divider" />
                  <button className="menu-item" onClick={() => {
                    navigate('/profile');
                    setShowUserMenu(false);
                  }}>
                    프로필 설정
                  </button>
                  <button className="menu-item logout" onClick={handleLogout}>
                    로그아웃
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="auth-buttons">
            <Link to="/login">
              <button className="login-button">로그인</button>
            </Link>
            <Link to="/signup">
              <button className="signup-button">회원가입</button>
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header; 