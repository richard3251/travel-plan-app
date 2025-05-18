import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { tripApi, tripDayApi, tripPlaceApi } from '../api/api';
import TripDayItem from '../components/TripDayItem';
import AddTripDayForm from '../components/AddTripDayForm';
import './TripDetailPage.css';

// 실제 앱에서는 아래 주석을 해제하고 react-kakao-maps-sdk를 사용해야 합니다.
import { Map, MapMarker } from 'react-kakao-maps-sdk';

const TripDetailPage = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [tripDays, setTripDays] = useState([]);
  const [allPlaces, setAllPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAddDayForm, setShowAddDayForm] = useState(false);
  const [showMap, setShowMap] = useState(false);

  useEffect(() => {
    const fetchTripDetail = async () => {
      try {
        const response = await tripApi.getTripDetail(tripId);
        setTrip(response.data);
        
        // 여행 날짜 정보도 함께 가져옴
        const daysResponse = await tripDayApi.getTripDays(tripId);
        setTripDays(daysResponse.data);
        
        setLoading(false);
        
        // 모든 날짜의 장소 정보 가져오기
        const allPlacesTemp = [];
        for (const day of daysResponse.data) {
          try {
            const placesResponse = await tripPlaceApi.getTripPlaces(day.id);
            // 날짜 정보를 포함하여 저장
            const placesWithDate = placesResponse.data.map(place => ({
              ...place,
              dayInfo: {
                day: day.day,
                date: day.date
              }
            }));
            allPlacesTemp.push(...placesWithDate);
          } catch (err) {
            console.error(`${day.day}일차 장소 정보 가져오기 실패:`, err);
          }
        }
        setAllPlaces(allPlacesTemp);
      } catch (err) {
        console.error('여행 상세 정보 가져오기 실패:', err);
        setError('여행 정보를 가져오는데 문제가 발생했습니다.');
        setLoading(false);
      }
    };

    fetchTripDetail();
  }, [tripId]);

  const handleDeleteTrip = async () => {
    if (window.confirm('정말로 이 여행을 삭제하시겠습니까?')) {
      try {
        await tripApi.deleteTrip(tripId);
        alert('여행이 성공적으로 삭제되었습니다.');
        navigate('/');
      } catch (err) {
        console.error('여행 삭제 실패:', err);
        alert('여행을 삭제하는데 문제가 발생했습니다.');
      }
    }
  };
  
  const handleAddTripDay = async (newTripDay) => {
    try {
      const response = await tripDayApi.createTripDay(tripId, newTripDay);
      setTripDays([...tripDays, response.data]);
      setShowAddDayForm(false);
    } catch (err) {
      console.error('여행 날짜 추가 실패:', err);
      alert('여행 날짜를 추가하는데 문제가 발생했습니다.');
    }
  };
  
  const handleDeleteTripDay = async (dayId) => {
    if (window.confirm('정말로 이 날짜를 삭제하시겠습니까? 모든 장소 정보가 함께 삭제됩니다.')) {
      try {
        await tripDayApi.deleteTripDay(tripId, dayId);
        setTripDays(tripDays.filter(day => day.id !== dayId));
        // 모든 장소 목록에서도 해당 날짜의 장소들 제거
        setAllPlaces(allPlaces.filter(place => place.dayInfo.day !== tripDays.find(d => d.id === dayId)?.day));
        alert('날짜가 성공적으로 삭제되었습니다.');
      } catch (err) {
        console.error('날짜 삭제 실패:', err);
        alert('날짜를 삭제하는데 문제가 발생했습니다.');
      }
    }
  };

  if (loading) return <div className="loading">로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!trip) return <div className="error">여행 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="trip-detail-container">
      <div className="trip-header">
        <h2>{trip.title}</h2>
        <div className="trip-actions">
          <button 
            onClick={() => setShowMap(!showMap)} 
            className="map-view-button"
          >
            {showMap ? '지도 닫기' : '지도로 보기'}
          </button>
          <Link to={`/trips/edit/${tripId}`} className="edit-button">
            수정
          </Link>
          <button onClick={handleDeleteTrip} className="delete-button">
            삭제
          </button>
        </div>
      </div>

      {/* 여행 지도 보기 */}
      {showMap && (
        <div className="trip-map-container">
          <Map
            center={{
              lat: allPlaces.length > 0 
                ? allPlaces[0].latitude 
                : 37.5665, // 서울 기본 위치
              lng: allPlaces.length > 0 
                ? allPlaces[0].longitude 
                : 126.9780,
            }}
            style={{ width: '100%', height: '400px', marginBottom: '20px' }}
            level={7} // 지도 확대 레벨
          >
            {allPlaces.map((place, index) => (
              <MapMarker
                key={place.id}
                position={{
                  lat: place.latitude,
                  lng: place.longitude
                }}
                title={place.placeName}
              />
            ))}
          </Map>
          
          <div className="place-list">
            <h4>등록된 모든 장소 ({allPlaces.length})</h4>
            {allPlaces.length > 0 ? (
              <ul className="all-places-list">
                {allPlaces.map((place, index) => (
                  <li key={place.id} className="map-place-item">
                    <span className="place-day-info">
                      Day {place.dayInfo.day} ({new Date(place.dayInfo.date).toLocaleDateString()})
                    </span>
                    <span className="place-name">{place.placeName}</span>
                    <span className="place-address">{place.address}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="no-places">등록된 장소가 없습니다.</p>
            )}
          </div>
        </div>
      )}

      <div className="trip-info-card">
        <div className="trip-dates">
          <div className="date-item">
            <span className="date-label">시작일</span>
            <span className="date-value">{new Date(trip.startDate).toLocaleDateString()}</span>
          </div>
          <div className="date-separator">~</div>
          <div className="date-item">
            <span className="date-label">종료일</span>
            <span className="date-value">{new Date(trip.endDate).toLocaleDateString()}</span>
          </div>
          <div className="trip-duration">
            총 {Math.ceil((new Date(trip.endDate) - new Date(trip.startDate)) / (1000 * 60 * 60 * 24)) + 1}일
          </div>
        </div>

        {trip.description && (
          <div className="trip-description">
            <h3>여행 설명</h3>
            <p>{trip.description}</p>
          </div>
        )}
      </div>

      <div className="trip-days-section">
        <div className="section-header">
          <h3>여행 일정</h3>
          <button 
            className="add-day-button"
            onClick={() => setShowAddDayForm(!showAddDayForm)}
          >
            {showAddDayForm ? '취소' : '날짜 추가'}
          </button>
        </div>
        
        {showAddDayForm && (
          <AddTripDayForm 
            onAddDay={handleAddTripDay} 
            tripStartDate={trip.startDate}
            tripEndDate={trip.endDate}
            existingDays={tripDays}
          />
        )}
        
        {tripDays.length > 0 ? (
          <div className="trip-days-list">
            {tripDays.map((day) => (
              <TripDayItem 
                key={day.id} 
                tripDay={day} 
                tripId={tripId}
                onDeleteDay={handleDeleteTripDay}
              />
            ))}
          </div>
        ) : (
          <p className="no-days">등록된 일정이 없습니다. 여행 날짜를 추가해보세요!</p>
        )}
      </div>
      
      <div className="back-button-container">
        <button onClick={() => navigate('/')} className="back-button">
          목록으로 돌아가기
        </button>
      </div>
    </div>
  );
};

export default TripDetailPage; 