import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { tripApi } from '../api/api';
import './TripList.css';

const TripList = () => {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        const response = await tripApi.getAllTrips();
        setTrips(response.data);
        setLoading(false);
      } catch (err) {
        console.error('여행 목록 가져오기 실패:', err);
        setError('여행 목록을 가져오는데 문제가 발생했습니다.');
        setLoading(false);
      }
    };

    fetchTrips();
  }, []);

  const handleDeleteTrip = async (tripId) => {
    if (window.confirm('정말로 이 여행을 삭제하시겠습니까?')) {
      try {
        await tripApi.deleteTrip(tripId);
        setTrips(trips.filter(trip => trip.id !== tripId));
      } catch (err) {
        console.error('여행 삭제 실패:', err);
        alert('여행을 삭제하는데 문제가 발생했습니다.');
      }
    }
  };

  if (loading) return <div className="loading">로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="trip-list-container">
      <h2>내 여행 목록</h2>
      
      {trips.length === 0 ? (
        <div className="no-trips">
          <p>아직 계획된 여행이 없습니다.</p>
          <Link to="/trips/new" className="create-trip-button">
            새 여행 만들기
          </Link>
        </div>
      ) : (
        <div className="trips-grid">
          {trips.map((trip) => (
            <div className="trip-card" key={trip.id}>
              <div className="trip-info">
                <h3>{trip.title}</h3>
                <p>
                  {new Date(trip.startDate).toLocaleDateString()} - {new Date(trip.endDate).toLocaleDateString()}
                </p>
                <p>{trip.description || '설명 없음'}</p>
              </div>
              <div className="trip-actions">
                <Link to={`/trips/${trip.id}`} className="view-trip">
                  세부 정보
                </Link>
                <Link to={`/trips/edit/${trip.id}`} className="edit-trip">
                  수정하기
                </Link>
                <button 
                  onClick={() => handleDeleteTrip(trip.id)} 
                  className="delete-trip"
                >
                  삭제하기
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TripList; 