import React, { useState, useEffect } from 'react';
import { tripPlaceApi } from '../api/api';
import TripPlaceItem from './TripPlaceItem';
import MapSearchModal from './MapSearchModal';
import './TripDayItem.css';

const TripDayItem = ({ tripDay, tripId, onDeleteDay }) => {
  const [tripPlaces, setTripPlaces] = useState([]);
  const [showMapSearchModal, setShowMapSearchModal] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    // 처음 로드시 장소 목록 가져오기
    const fetchPlaces = async () => {
      try {
        const response = await tripPlaceApi.getTripPlaces(tripDay.id);
        setTripPlaces(response.data);
        setLoading(false);
      } catch (err) {
        console.error('장소 목록 가져오기 실패:', err);
        setLoading(false);
      }
    };
    
    fetchPlaces();
  }, [tripDay.id]);
  
  const handleAddPlace = async (placeData) => {
    try {
      const response = await tripPlaceApi.createTripPlace(tripDay.id, placeData);
      setTripPlaces([...tripPlaces, response.data]);
    } catch (err) {
      console.error('장소 추가 실패:', err);
      alert('장소를 추가하는데 문제가 발생했습니다.');
    }
  };
  
  const handleUpdatePlace = async (placeId, updatedData) => {
    try {
      const response = await tripPlaceApi.updateTripPlace(tripDay.id, placeId, updatedData);
      setTripPlaces(tripPlaces.map(place => 
        place.id === placeId ? response.data : place
      ));
    } catch (err) {
      console.error('장소 수정 실패:', err);
      alert('장소를 수정하는데 문제가 발생했습니다.');
    }
  };
  
  const handleUpdatePlaceOrder = async (placeId, newOrder) => {
    try {
      await tripPlaceApi.updateTripPlaceOrder(tripDay.id, placeId, { order: newOrder });
      
      // 순서 변경 후 다시 로드
      const response = await tripPlaceApi.getTripPlaces(tripDay.id);
      setTripPlaces(response.data);
    } catch (err) {
      console.error('장소 순서 변경 실패:', err);
      alert('장소 순서를 변경하는데 문제가 발생했습니다.');
    }
  };
  
  const handleDeletePlace = async (placeId) => {
    if (window.confirm('정말로 이 장소를 삭제하시겠습니까?')) {
      try {
        await tripPlaceApi.deleteTripPlace(tripDay.id, placeId);
        setTripPlaces(tripPlaces.filter(place => place.id !== placeId));
      } catch (err) {
        console.error('장소 삭제 실패:', err);
        alert('장소를 삭제하는데 문제가 발생했습니다.');
      }
    }
  };
  
  const handleDeleteDay = () => {
    if (window.confirm('정말로 이 날짜를 삭제하시겠습니까? 모든 장소 정보가 함께 삭제됩니다.')) {
      onDeleteDay(tripDay.id);
    }
  };
  
  const handleAddSearchedPlace = (searchedPlace) => {
    const placeData = {
      placeName: searchedPlace.placeName,
      address: searchedPlace.address,
      latitude: searchedPlace.latitude,
      longitude: searchedPlace.longitude,
      placeId: searchedPlace.id,
      visitTime: "12:00", // 기본 방문 시간
      visitOrder: tripPlaces.length + 1
    };
    
    handleAddPlace(placeData);
    setShowMapSearchModal(false);
  };
  
  return (
    <div className="day-card">
      <div className="day-header">
        <h4>DAY {tripDay.day} - {new Date(tripDay.date).toLocaleDateString()}</h4>
        <div className="day-actions">
          <button 
            className="map-search-button"
            onClick={() => setShowMapSearchModal(true)}
          >
            지도에서 장소 추가
          </button>
          <button 
            className="delete-day-button"
            onClick={handleDeleteDay}
          >
            날짜 삭제
          </button>
        </div>
      </div>
      
      {showMapSearchModal && (
        <MapSearchModal
          onClose={() => setShowMapSearchModal(false)}
          onSelectPlace={handleAddSearchedPlace}
        />
      )}
      
      <div className="day-places">
        {loading ? (
          <p className="loading-places">장소를 불러오는 중...</p>
        ) : tripPlaces.length > 0 ? (
          <ul className="places-list">
            {tripPlaces.sort((a, b) => a.visitOrder - b.visitOrder).map((place) => (
              <TripPlaceItem 
                key={place.id}
                place={place}
                onUpdatePlace={handleUpdatePlace}
                onUpdateOrder={handleUpdatePlaceOrder}
                onDeletePlace={handleDeletePlace}
                totalPlaces={tripPlaces.length}
              />
            ))}
          </ul>
        ) : (
          <p className="no-places">등록된 장소가 없습니다. 지도에서 장소를 추가해보세요!</p>
        )}
      </div>
    </div>
  );
};

export default TripDayItem; 