import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import TripList from './components/TripList';
import NewTripPage from './pages/NewTripPage';
import TripDetailPage from './pages/TripDetailPage';
import EditTripPage from './pages/EditTripPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <Header />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<TripList />} />
            <Route path="/trips/new" element={<NewTripPage />} />
            <Route path="/trips/:tripId" element={<TripDetailPage />} />
            <Route path="/trips/edit/:tripId" element={<EditTripPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App; 