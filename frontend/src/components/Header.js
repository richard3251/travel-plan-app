import React from 'react';
import { Link } from 'react-router-dom';
import './Header.css';

const Header = () => {
  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          여행 계획 앱
        </Link>
        <nav className="nav">
          <ul>
            <li>
              <Link to="/">내 여행</Link>
            </li>
            <li>
              <Link to="/trips/new">새 여행 만들기</Link>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  );
};

export default Header; 