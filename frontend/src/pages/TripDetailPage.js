import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { tripApi, tripDayApi, tripPlaceApi } from '../api/api';
import TripDayItem from '../components/TripDayItem';
import MapSearchModal from '../components/MapSearchModal';
import TripImageUpload from '../components/TripImageUpload';
import TripShareModal from '../components/TripShareModal';
import './TripDetailPage.css';

// 실제 앱에서는 아래 주석을 해제하고 react-kakao-maps-sdk를 사용해야 합니다.
import { Map, MapMarker, Polyline, CustomOverlayMap } from 'react-kakao-maps-sdk';

// 마커 이미지 URL (핀 스타일)
const MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";
const SELECTED_MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";
const REGION_MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/2018/pc/img/marker_spot.png";

// 국내 주요 지역과 좌표 데이터
const regionOptions = [
  { name: '서울', lat: 37.5665, lng: 126.9780, level: 7 },
  { name: '경기도', lat: 37.4138, lng: 127.5183, level: 9 },
  { name: '인천', lat: 37.4563, lng: 126.7052, level: 8 },
  { name: '강원도', lat: 37.8228, lng: 128.1555, level: 10 },
  { name: '충청북도', lat: 36.8000, lng: 127.7000, level: 9 },
  { name: '충청남도', lat: 36.5184, lng: 126.8000, level: 9 },
  { name: '대전', lat: 36.3504, lng: 127.3845, level: 7 },
  { name: '전라북도', lat: 35.8242, lng: 127.1489, level: 9 },
  { name: '전라남도', lat: 34.8679, lng: 126.9910, level: 9 },
  { name: '광주', lat: 35.1595, lng: 126.8526, level: 7 },
  { name: '경상북도', lat: 36.4919, lng: 128.8889, level: 9 },
  { name: '경상남도', lat: 35.4606, lng: 128.2132, level: 9 },
  { name: '대구', lat: 35.8714, lng: 128.6014, level: 7 },
  { name: '울산', lat: 35.5384, lng: 129.3114, level: 7 },
  { name: '부산', lat: 35.1796, lng: 129.0756, level: 7 },
  { name: '제주도', lat: 33.4996, lng: 126.5312, level: 9 }
];

const TripDetailPage = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [tripDays, setTripDays] = useState([]);
  const [allPlaces, setAllPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeDay, setActiveDay] = useState(null);
  const [selectedPlace, setSelectedPlace] = useState(null);
  const [showMapSearch, setShowMapSearch] = useState(false);
  const [showRegionInfo, setShowRegionInfo] = useState(true);
  const [showImageUpload, setShowImageUpload] = useState(false);
  const [tripImages, setTripImages] = useState([]);
  const [showShareModal, setShowShareModal] = useState(false);

  // 초기 데이터 로딩
  useEffect(() => {
    const fetchTripDetail = async () => {
      try {
        setLoading(true);
        
        // 1. 여행 정보 가져오기
        console.log('여행 정보 로딩 시작:', tripId);
        const tripResponse = await tripApi.getTripDetail(tripId);
        const tripData = { ...tripResponse.data };
        
        // 지역 정보 처리
        if (!tripData.regionLat || !tripData.regionLng) {
          const selectedRegion = regionOptions.find(r => r.name === tripData.region);
          if (selectedRegion) {
            tripData.regionLat = selectedRegion.lat;
            tripData.regionLng = selectedRegion.lng;
          } else {
            tripData.regionLat = 37.5665;
            tripData.regionLng = 126.9780;
          }
        }
        
        setTrip(tripData);
        console.log('여행 정보 로딩 완료:', tripData);
        
        // 2. 여행 기간에 따른 날짜들 자동 생성
        await generateTripDays(tripId, tripData.startDate, tripData.endDate);
        
      } catch (err) {
        console.error('여행 상세 정보 가져오기 실패:', err);
        setError('여행 정보를 불러오는데 실패했습니다.');
        setLoading(false);
      }
    };

    if (tripId) {
      fetchTripDetail();
    }
  }, [tripId]);

  // 여행 기간에 따른 날짜들 자동 생성
  const generateTripDays = async (tripId, startDate, endDate) => {
    try {
      // 기존 날짜들 확인
      const existingDaysResponse = await tripDayApi.getTripDays(tripId);
      const existingDays = existingDaysResponse.data;
      
      // 여행 기간 계산
      const start = new Date(startDate);
      const end = new Date(endDate);
      const daysDiff = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
      
      console.log('여행 기간:', daysDiff, '일');
      
      // 필요한 날짜들 생성
      const daysToCreate = [];
      for (let i = 0; i < daysDiff; i++) {
        const currentDate = new Date(start);
        currentDate.setDate(start.getDate() + i);
        const dateString = currentDate.toISOString().split('T')[0];
        
        // 이미 존재하는 날짜인지 확인
        const existingDay = existingDays.find(day => day.date === dateString);
        if (!existingDay) {
          daysToCreate.push({
            day: i + 1,
            date: dateString
          });
        }
      }
      
      // 새로운 날짜들 생성
      for (const dayData of daysToCreate) {
        try {
          await tripDayApi.createTripDay(tripId, dayData);
          console.log(`${dayData.day}일차 (${dayData.date}) 생성 완료`);
        } catch (err) {
          console.error(`${dayData.day}일차 생성 실패:`, err);
        }
      }
      
      // 3. 업데이트된 여행 날짜 가져오기
      console.log('여행 날짜 로딩 시작');
      const daysResponse = await tripDayApi.getTripDays(tripId);
      const sortedDays = daysResponse.data.sort((a, b) => a.day - b.day);
      setTripDays(sortedDays);
      console.log('여행 날짜 로딩 완료:', sortedDays);
      
      // 첫 번째 날짜를 활성화
      if (sortedDays.length > 0) {
        setActiveDay(sortedDays[0].id);
      }
      
      // 4. 모든 날짜의 장소 정보 가져오기
      console.log('장소 정보 로딩 시작');
      const allPlacesTemp = [];
      for (const day of sortedDays) {
        try {
          const placesResponse = await tripPlaceApi.getTripPlaces(day.id);
          const placesWithDate = placesResponse.data.map(place => ({
            ...place,
            dayInfo: {
              dayId: day.id,
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
      console.log('장소 정보 로딩 완료:', allPlacesTemp);
      setLoading(false);
      
    } catch (err) {
      console.error('날짜 생성 실패:', err);
      setLoading(false);
    }
  };

  // 지역 정보 오버레이 자동 숨김
  useEffect(() => {
    if (trip) {
      const timer = setTimeout(() => {
        setShowRegionInfo(false);
      }, 3000);
      
      return () => clearTimeout(timer);
    }
  }, [trip]);

  // 활성 날짜의 장소들 가져오기
  const getActiveDayPlaces = () => {
    if (!activeDay) return [];
    const activePlaces = allPlaces.filter(place => place.dayInfo.dayId === activeDay);
    return activePlaces.sort((a, b) => a.visitOrder - b.visitOrder);
  };

  // 특정 날짜의 장소 개수 가져오기
  const getDayPlacesCount = (dayId) => {
    return allPlaces.filter(place => place.dayInfo.dayId === dayId).length;
  };

  // 지도 줌 레벨 계산
  const getMapLevel = () => {
    if (!trip) return 7;
    
    if (trip.region) {
      const selectedRegion = regionOptions.find(r => r.name === trip.region);
      if (selectedRegion) {
        return selectedRegion.level;
      }
    }
    return 7;
  };

  // 지도 중심점 계산
  const getMapCenter = () => {
    if (!trip) {
      return { lat: 37.5665, lng: 126.9780 };
    }
    
    const activePlaces = getActiveDayPlaces();
    if (selectedPlace && activePlaces.length > 0) {
      const selectedPlaceData = activePlaces.find(p => p.id === selectedPlace);
      if (selectedPlaceData) {
        return {
          lat: selectedPlaceData.latitude,
          lng: selectedPlaceData.longitude
        };
      }
    }
    
    if (trip.regionLat && trip.regionLng) {
      return { 
        lat: parseFloat(trip.regionLat), 
        lng: parseFloat(trip.regionLng) 
      };
    }
    
    return { lat: 37.5665, lng: 126.9780 };
  };

  // 여행 삭제
  const handleDeleteTrip = async () => {
    if (window.confirm('정말로 이 여행을 삭제하시겠습니까? 모든 일정과 장소 정보가 함께 삭제됩니다.')) {
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

  // 장소 추가
  const handleAddPlace = (newPlace) => {
    if (!activeDay) {
      alert('먼저 날짜를 선택해주세요.');
      return;
    }
    
    const activePlaces = getActiveDayPlaces();
    const lastVisitOrder = activePlaces.length > 0 
      ? Math.max(...activePlaces.map(p => p.visitOrder))
      : 0;
    
    const placeData = {
      placeName: newPlace.placeName,
      address: newPlace.address,
      latitude: newPlace.latitude,
      longitude: newPlace.longitude,
      placeId: newPlace.placeId || newPlace.id,
      visitOrder: lastVisitOrder + 1,
      visitTime: newPlace.visitTime || new Date().toLocaleTimeString().substring(0, 5),
      memo: newPlace.memo || ''
    };
    
    tripPlaceApi.createTripPlace(activeDay, placeData)
      .then(response => {
        const activeDayInfo = tripDays.find(day => day.id === activeDay);
        const newPlaceWithDayInfo = {
          ...response.data,
          dayInfo: {
            dayId: activeDayInfo.id,
            day: activeDayInfo.day,
            date: activeDayInfo.date
          }
        };
        
        setAllPlaces(prevPlaces => [...prevPlaces, newPlaceWithDayInfo]);
        setSelectedPlace(response.data.id);
        setShowMapSearch(false);
        alert('장소가 성공적으로 추가되었습니다.');
      })
      .catch(err => {
        console.error('장소 추가 실패:', err);
        alert('장소를 추가하는데 문제가 발생했습니다.');
      });
  };

  // 장소 업데이트
  const handlePlaceUpdate = (dayId, updatedPlaces) => {
    setAllPlaces(prevPlaces => {
      const otherDaysPlaces = prevPlaces.filter(place => place.dayInfo.dayId !== dayId);
      return [...otherDaysPlaces, ...updatedPlaces];
    });
  };

  // 장소 삭제
  const handlePlaceDelete = (placeId) => {
    setAllPlaces(prevPlaces => prevPlaces.filter(place => place.id !== placeId));
  };

  // 마커 클릭
  const handleMarkerClick = (placeId) => {
    setSelectedPlace(placeId);
  };

  // 이미지 변경 핸들러
  const handleImagesChange = (images) => {
    setTripImages(images);
  };

  if (loading) return <div className="loading">로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!trip) return <div className="error">여행 정보를 찾을 수 없습니다.</div>;

  const activeDayPlaces = getActiveDayPlaces();

  return (
    <div className="trip-detail-container">
      {/* 여행 헤더 */}
      <div className="trip-header">
        <h2>
          {trip.title}
          <span className="trip-date-range">
            {new Date(trip.startDate).toLocaleDateString()} - {new Date(trip.endDate).toLocaleDateString()}
          </span>
        </h2>
        <div className="trip-brand">
          <span className="brand-name">planscanner</span>
        </div>
        <div className="trip-actions">
          <button 
            onClick={() => setShowShareModal(true)} 
            className="share-button"
          >
            🔗 공유
          </button>
          <button 
            onClick={() => setShowImageUpload(!showImageUpload)} 
            className="image-button"
          >
            {showImageUpload ? '이미지 숨기기' : '이미지 관리'}
          </button>
          <Link to={`/trips/edit/${tripId}`} className="edit-button">
            수정
          </Link>
          <button onClick={handleDeleteTrip} className="delete-button">
            삭제
          </button>
        </div>
      </div>

      {/* 이미지 업로드 섹션 */}
      {showImageUpload && (
        <div className="trip-image-section">
          <TripImageUpload 
            tripId={tripId} 
            onImagesChange={handleImagesChange}
          />
        </div>
      )}

      {/* 메인 컨텐츠 */}
      <div className="trip-content">
        {/* 좌측 사이드바 */}
        <div className="trip-sidebar">
          {/* 날짜 탭들 */}
          <div className="trip-day-tabs">
            {tripDays.map((day) => (
              <button
                key={day.id}
                className={`trip-day-tab ${activeDay === day.id ? 'active' : ''}`}
                onClick={() => setActiveDay(day.id)}
              >
                <span className="day-number">{day.day}일차</span>
                <span className="day-date">{new Date(day.date).toLocaleDateString()}</span>
                <span className="places-count">{getDayPlacesCount(day.id)}개 장소</span>
              </button>
            ))}
          </div>

          {/* 선택된 날짜의 상세 정보 */}
          <div className="trip-day-detail">
            {activeDay && (
              <TripDayItem 
                tripDay={tripDays.find(day => day.id === activeDay)}
                tripId={tripId}
                places={activeDayPlaces}
                onPlaceUpdate={handlePlaceUpdate}
                onPlaceDelete={handlePlaceDelete}
              />
            )}
          </div>
          
          {/* 장소 추가 버튼 */}
          <div className="trip-map-controls-sidebar">
            <button 
              className="add-place-btn"
              onClick={() => setShowMapSearch(true)}
              disabled={!activeDay}
            >
              {activeDay ? '장소 추가' : '날짜를 먼저 선택하세요'}
            </button>
          </div>
        </div>

        {/* 우측 지도 패널 */}
        <div className="trip-map-panel">
          {trip?.region && (
            <div className="region-indicator">
              <span>여행 지역: {trip.region}</span>
            </div>
          )}
          
          <Map
            center={getMapCenter()}
            style={{ width: '100%', height: '100%' }}
            level={getMapLevel()}
            className="trip-map"
          >
            {/* 지역 마커 */}
            <MapMarker
              position={{
                lat: parseFloat(trip.regionLat || 37.5665),
                lng: parseFloat(trip.regionLng || 126.9780)
              }}
              image={{
                src: REGION_MARKER_IMAGE_URL,
                size: { width: 28, height: 28 },
              }}
            />
            
            {/* 지역 정보 오버레이 */}
            {showRegionInfo && trip?.region && (
              <CustomOverlayMap
                position={{
                  lat: parseFloat(trip.regionLat || 37.5665),
                  lng: parseFloat(trip.regionLng || 126.9780)
                }}
                yAnchor={1.5}
              >
                <div className="region-info-overlay">
                  <span>{trip.region} 여행</span>
                </div>
              </CustomOverlayMap>
            )}

            {/* 장소 마커들 */}
            {activeDayPlaces.map((place, index) => (
              <React.Fragment key={place.id}>
                <MapMarker
                  position={{
                    lat: place.latitude,
                    lng: place.longitude
                  }}
                  onClick={() => handleMarkerClick(place.id)}
                  image={{
                    src: selectedPlace === place.id 
                      ? SELECTED_MARKER_IMAGE_URL 
                      : MARKER_IMAGE_URL,
                    size: { width: 24, height: 35 },
                  }}
                />
                <CustomOverlayMap
                  position={{
                    lat: place.latitude,
                    lng: place.longitude
                  }}
                  yAnchor={2.5}
                >
                  <div className="place-marker-label">
                    <span className="place-marker-number">{index + 1}</span>
                  </div>
                </CustomOverlayMap>
              </React.Fragment>
            ))}
            
            {/* 장소들 연결 선 */}
            {activeDayPlaces.length > 1 && (
              <Polyline
                path={activeDayPlaces.map(place => ({
                  lat: place.latitude,
                  lng: place.longitude
                }))}
                strokeWeight={3}
                strokeColor="#3498db"
                strokeOpacity={0.7}
                strokeStyle="solid"
              />
            )}
          </Map>
        </div>
      </div>
      
      {/* 장소 검색 모달 */}
      {showMapSearch && (
        <MapSearchModal
          onClose={() => setShowMapSearch(false)}
          onSelectPlace={handleAddPlace}
          regionLat={parseFloat(trip.regionLat || 37.5665)}
          regionLng={parseFloat(trip.regionLng || 126.9780)}
          regionName={trip.region || '서울'}
          mapLevel={getMapLevel()}
          tripDayId={activeDay}
        />
      )}
      
      {/* 하단 버튼 */}
      <div className="back-button-container">
        <button onClick={() => navigate('/')} className="back-button">
          목록으로 돌아가기
        </button>
      </div>

      {/* 여행 공유 모달 */}
      {showShareModal && (
        <TripShareModal
          tripId={tripId}
          tripTitle={trip.title}
          onClose={() => setShowShareModal(false)}
        />
      )}
    </div>
  );
};

export default TripDetailPage; 