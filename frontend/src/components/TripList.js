import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { tripApi, fileApi } from '../api/api';
import './TripList.css';

const TripList = () => {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tripCoverImages, setTripCoverImages] = useState({});

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        const response = await tripApi.getAllTrips();
        const tripsData = response.data;
        setTrips(tripsData);
        
        // 각 여행의 커버 이미지 로드
        const coverImages = {};
        for (const trip of tripsData) {
          try {
            const coverResponse = await fileApi.getTripCoverImage(trip.id);
            if (coverResponse.data) {
              coverImages[trip.id] = coverResponse.data;
            }
          } catch (err) {
            // 커버 이미지가 없는 경우는 무시
            console.log(`여행 ${trip.id}의 커버 이미지가 없습니다.`);
          }
        }
        setTripCoverImages(coverImages);
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
              {/* 커버 이미지 */}
              <div className="trip-cover-image">
                {tripCoverImages[trip.id] ? (
                  <img 
                    src={tripCoverImages[trip.id].thumbnailUrl || tripCoverImages[trip.id].s3Url} 
                    alt={trip.title}
                    className="cover-image"
                    onError={(e) => {
                      e.target.src = tripCoverImages[trip.id].s3Url; // 썸네일 로드 실패 시 원본 이미지 사용
                    }}
                  />
                ) : (
                  <div className="no-cover-image">
                    <span className="no-image-icon">🖼️</span>
                    <span className="no-image-text">이미지 없음</span>
                  </div>
                )}
              </div>
              
              <div className="trip-info">
                <h3>{trip.title}</h3>
                <p className="trip-dates">
                  {new Date(trip.startDate).toLocaleDateString()} - {new Date(trip.endDate).toLocaleDateString()}
                </p>
                <p className="trip-description">{trip.description || '설명 없음'}</p>
                {trip.region && (
                  <p className="trip-region">📍 {trip.region}</p>
                )}
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