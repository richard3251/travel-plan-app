import React from 'react';
import { Link } from 'react-router-dom';
import './Header.css';

const Header = () => {
  return (
    <header className="header">
      <Link to="/" className="logo">
        planscanner
      </Link>
      
      <div className="nav-links">
        <Link to="/">내 여행 목록</Link>
      </div>
      
      <div className="user-actions">
        <Link to="/trips/new">
          <button>새 여행 만들기</button>
        </Link>
      </div>
    </header>
  );
};

export default Header; 