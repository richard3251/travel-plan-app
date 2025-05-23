import React, { useState, useEffect } from 'react';
import { placeApi } from '../api/api';
import './MapSearchModal.css';

// 실제 앱에서는 아래 주석을 해제하고 react-kakao-maps-sdk를 사용해야 합니다.
import { Map, MapMarker } from 'react-kakao-maps-sdk';

// 마커 이미지 URL
const MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";
const REGION_MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/2018/pc/img/marker_spot.png";

const MapSearchModal = ({ onClose, onSelectPlace, regionLat, regionLng, regionName, mapLevel, tripDayId }) => {
  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // 지역 좌표를 기본값으로 사용 (서울이 기본값)
  const [currentPosition, setCurrentPosition] = useState({
    lat: regionLat || 37.5665,
    lng: regionLng || 126.9780
  });

  // 선택된 장소 정보 상태 추가
  const [selectedPlaceData, setSelectedPlaceData] = useState(null);
  const [showPlaceForm, setShowPlaceForm] = useState(false);
  const [visitTime, setVisitTime] = useState('');
  const [memo, setMemo] = useState('');

  // 컴포넌트 마운트 시 전달받은 지역 위치로 중심점 설정
  useEffect(() => {
    // 지역 좌표가 있으면 해당 좌표로 설정
    if (regionLat && regionLng) {
      setCurrentPosition({
        lat: regionLat,
        lng: regionLng
      });
    }
  }, [regionLat, regionLng]);

  // 컴포넌트 마운트 시 현재 시간을 방문 시간 기본값으로 설정
  useEffect(() => {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    setVisitTime(`${hours}:${minutes}`);
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
  
  const handlePlaceClick = (place) => {
    // 카카오 API 응답 형식에 맞게 처리하여 선택된 장소 데이터 설정
    const placeData = {
      id: place.id,
      placeName: place.place_name,
      placeId: place.id,
      address: place.address_name,
      latitude: parseFloat(place.y),
      longitude: parseFloat(place.x),
      categoryName: place.category_name,
      phone: place.phone
    };
    
    console.log('선택한 장소:', placeData);
    console.log('전달받은 tripDayId:', tripDayId);
    
    setSelectedPlaceData(placeData);
    setShowPlaceForm(true);
  };

  const handleSubmitPlace = () => {
    if (!selectedPlaceData) return;
    
    // 최종 선택된 장소 정보에 방문 시간과 메모 추가
    const finalPlaceData = {
      ...selectedPlaceData,
      visitTime: visitTime,
      memo: memo
    };
    
    // 부모 컴포넌트로 최종 데이터 전달
    onSelectPlace(finalPlaceData);
  };
  
  const handleCancelPlaceForm = () => {
    setSelectedPlaceData(null);
    setShowPlaceForm(false);
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
        {!showPlaceForm ? (
          // 검색 및 결과 화면
          <>
            <div className="modal-header">
              <h3>{regionName || '서울'} 장소 검색</h3>
              <button className="close-button" onClick={onClose}>×</button>
            </div>
            
            <form className="search-form" onSubmit={handleSearch}>
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder={`${regionName || '서울'}에서 장소를 검색하세요 (예: 카페, 맛집, 관광지)`}
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
                  level={mapLevel || 3} // 지도 확대 레벨 (전달받거나 기본값 사용)
                  onClick={handleMapClick}
                >
                  {/* 현재 위치 마커 - 지역 중심점 표시 */}
                  <MapMarker
                    position={{ lat: currentPosition.lat, lng: currentPosition.lng }}
                    image={{
                      src: REGION_MARKER_IMAGE_URL,
                      size: { width: 28, height: 28 },
                    }}
                  />

                  {/* 검색 결과 마커들 */}
                  {searchResults.map((place, index) => (
                    <MapMarker
                      key={place.id}
                      position={{
                        lat: parseFloat(place.y),
                        lng: parseFloat(place.x)
                      }}
                      onClick={(e) => {
                        e.stopPropagation();
                        handlePlaceClick(place);
                      }}
                      image={{
                        src: MARKER_IMAGE_URL,
                        size: { width: 24, height: 35 },
                      }}
                    />
                  ))}
                </Map>
              </div>
              
              <div className="search-results">
                <h4>{regionName || '서울'} 지역 검색 결과</h4>
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
                          onClick={(e) => {
                            e.stopPropagation();
                            console.log('장소 선택 버튼 클릭:', place);
                            handlePlaceClick(place);
                          }}
                        >
                          선택
                        </button>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="no-results">
                    {keyword ? `'${keyword}' 검색 결과가 없습니다.` : `${regionName || '서울'} 지역에서 검색할 장소를 입력하세요`}
                  </div>
                )}
              </div>
            </div>
          </>
        ) : (
          // 장소 정보 입력 폼
          <div className="place-form-container">
            <div className="modal-header">
              <h3>방문 정보 입력</h3>
              <button className="close-button" onClick={handleCancelPlaceForm}>×</button>
            </div>
            
            <div className="selected-place-info">
              <h4>{selectedPlaceData.placeName}</h4>
              <p>{selectedPlaceData.address}</p>
              {selectedPlaceData.categoryName && <p className="category">{selectedPlaceData.categoryName}</p>}
            </div>
            
            <div className="place-form">
              <div className="form-group">
                <label htmlFor="visitTime">방문 시간</label>
                <input 
                  type="time" 
                  id="visitTime" 
                  value={visitTime}
                  onChange={(e) => setVisitTime(e.target.value)}
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="memo">메모</label>
                <textarea 
                  id="memo" 
                  value={memo}
                  onChange={(e) => setMemo(e.target.value)}
                  placeholder="이 장소에 대한 메모를 입력하세요 (선택사항)"
                  rows={4}
                />
              </div>
              
              <div className="form-actions">
                <button 
                  type="button" 
                  className="cancel-button"
                  onClick={handleCancelPlaceForm}
                >
                  취소
                </button>
                <button 
                  type="button" 
                  className="confirm-button"
                  onClick={handleSubmitPlace}
                >
                  확인
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MapSearchModal; 