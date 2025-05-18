import React, { useState, useEffect } from 'react';
import { placeApi } from '../api/api';
import './MapSearchModal.css';

// 실제 앱에서는 아래 주석을 해제하고 react-kakao-maps-sdk를 사용해야 합니다.
import { Map, MapMarker } from 'react-kakao-maps-sdk';

const MapSearchModal = ({ onClose, onSelectPlace }) => {
  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentPosition, setCurrentPosition] = useState({
    lat: 37.5662952, // 서울 시청 기본 좌표
    lng: 126.9779451
  });

  // 컴포넌트 마운트 시 사용자 위치 가져오기 시도
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setCurrentPosition({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          console.error('위치 정보를 가져오는데 실패했습니다:', error);
        }
      );
    }
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!keyword.trim()) {
      alert('검색어를 입력해주세요.');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await placeApi.searchPlaces(
        keyword, 
        currentPosition.lat, 
        currentPosition.lng
      );
      
      setSearchResults(response.data.documents || []);
      setLoading(false);
    } catch (err) {
      console.error('장소 검색 실패:', err);
      setError('장소를 검색하는데 문제가 발생했습니다.');
      setLoading(false);
    }
  };
  
  const handleSelectPlace = (place) => {
    // 카카오 API 응답 형식에 맞게 처리
    const selectedPlace = {
      id: place.id,
      placeName: place.place_name,
      placeId: place.id,
      address: place.address_name,
      latitude: parseFloat(place.y),
      longitude: parseFloat(place.x),
      categoryName: place.category_name,
      phone: place.phone
    };
    
    onSelectPlace(selectedPlace);
  };
  
  const handleMapClick = (target, mouseEvent) => {
    // 지도 클릭 시 해당 좌표로 위치 이동
    setCurrentPosition({
      lat: mouseEvent.latLng.getLat(),
      lng: mouseEvent.latLng.getLng()
    });
  };
  
  return (
    <div className="map-search-modal-overlay">
      <div className="map-search-modal">
        <div className="modal-header">
          <h3>지도에서 장소 검색</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="장소를 검색하세요 (예: 카페, 맛집, 관광지)"
          />
          <button type="submit" disabled={loading}>
            {loading ? '검색 중...' : '검색'}
          </button>
        </form>
        
        <div className="map-search-container">
          <div className="map-container">
            <Map
              center={{ lat: currentPosition.lat, lng: currentPosition.lng }}
              style={{ width: '100%', height: '100%' }}
              level={3} // 지도 확대 레벨
              onClick={handleMapClick}
            >
              {/* 현재 위치 마커 */}
              <MapMarker
                position={{ lat: currentPosition.lat, lng: currentPosition.lng }}
              />

              {/* 검색 결과 마커들 */}
              {searchResults.map((place) => (
                <MapMarker
                  key={place.id}
                  position={{
                    lat: parseFloat(place.y),
                    lng: parseFloat(place.x)
                  }}
                  onClick={() => handleSelectPlace(place)}
                />
              ))}
            </Map>
          </div>
          
          <div className="search-results">
            <h4>검색 결과</h4>
            {loading ? (
              <div className="loading-results">검색 결과를 불러오는 중...</div>
            ) : error ? (
              <div className="search-error">{error}</div>
            ) : searchResults.length > 0 ? (
              <ul className="results-list">
                {searchResults.map((place) => (
                  <li key={place.id} className="result-item">
                    <div className="result-info">
                      <div className="result-name">{place.place_name}</div>
                      <div className="result-category">{place.category_name}</div>
                      <div className="result-address">{place.address_name}</div>
                      {place.phone && <div className="result-phone">{place.phone}</div>}
                    </div>
                    <button 
                      className="select-place-button"
                      onClick={() => handleSelectPlace(place)}
                    >
                      선택
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="no-results">
                {keyword ? '검색 결과가 없습니다.' : '지도에서 위치를 선택하거나 검색어를 입력하세요'}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MapSearchModal; 