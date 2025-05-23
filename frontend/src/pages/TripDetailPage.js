import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { tripApi, tripDayApi, tripPlaceApi } from '../api/api';
import TripDayItem from '../components/TripDayItem';
import AddTripDayForm from '../components/AddTripDayForm';
import MapSearchModal from '../components/MapSearchModal';
import './TripDetailPage.css';

// 실제 앱에서는 아래 주석을 해제하고 react-kakao-maps-sdk를 사용해야 합니다.
import { Map, MapMarker, Polyline, CustomOverlayMap } from 'react-kakao-maps-sdk';

// 마커 이미지 URL (핀 스타일)
const MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";
const SELECTED_MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png";
const REGION_MARKER_IMAGE_URL = "https://t1.daumcdn.net/localimg/localimages/07/2018/pc/img/marker_spot.png";

// 국내 주요 지역과 좌표 데이터 - NewTripPage와 동일한 데이터
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

// API 연결 문제 시 사용할 기본 데이터
const fallbackTrip = {
  id: 1,
  title: "제주도 여행",
  startDate: "2025-05-25",
  endDate: "2025-05-27",
  description: "5월 제주도 여행 계획",
  region: "제주도",
  regionLat: 33.499621,
  regionLng: 126.531188
};

const fallbackDays = [
  { id: 1, day: 1, date: "2025-05-25", tripId: 1 },
  { id: 2, day: 2, date: "2025-05-26", tripId: 1 },
  { id: 3, day: 3, date: "2025-05-27", tripId: 1 }
];

const fallbackPlaces = [
  { 
    id: 1, 
    dayId: 1, 
    placeName: "제주국제공항", 
    address: "제주특별자치도 제주시 공항로 2", 
    latitude: 33.507335, 
    longitude: 126.492810, 
    visitTime: "14:28", 
    visitOrder: 1,
    memo: "도착 예정 시간" 
  },
  { 
    id: 2, 
    dayId: 1, 
    placeName: "스위트호텔 제주", 
    address: "제주특별자치도 제주시 노형동 925", 
    latitude: 33.485569, 
    longitude: 126.480122, 
    visitTime: "15:30", 
    visitOrder: 2,
    memo: "체크인 및 짐 풀기" 
  },
  { 
    id: 3, 
    dayId: 1, 
    placeName: "성지코지", 
    address: "제주특별자치도 서귀포시 성산읍 성산리", 
    latitude: 33.458031, 
    longitude: 126.942222, 
    visitTime: "16:55", 
    visitOrder: 3 
  },
  { 
    id: 4, 
    dayId: 1, 
    placeName: "성산 일출봉", 
    address: "제주특별자치도 서귀포시 성산읍 일출로 284-12", 
    latitude: 33.459277, 
    longitude: 126.942604, 
    visitTime: "20:00", 
    visitOrder: 4 
  },
  { 
    id: 5, 
    dayId: 1, 
    placeName: "스위트호텔 제주", 
    address: "제주특별자치도 제주시 노형동 925", 
    latitude: 33.485569, 
    longitude: 126.480122, 
    visitTime: "22:30", 
    visitOrder: 5 
  },
  { 
    id: 6, 
    dayId: 2, 
    placeName: "한라산 국립공원", 
    address: "제주특별자치도 제주시 오등동 산 182-1", 
    latitude: 33.361900, 
    longitude: 126.529160, 
    visitTime: "09:00", 
    visitOrder: 1 
  },
  { 
    id: 7, 
    dayId: 2, 
    placeName: "천지연폭포", 
    address: "제주특별자치도 서귀포시 천지동 667-7", 
    latitude: 33.246944, 
    longitude: 126.554387, 
    visitTime: "14:00", 
    visitOrder: 2 
  }
];

const TripDetailPage = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [tripDays, setTripDays] = useState([]);
  const [allPlaces, setAllPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAddDayForm, setShowAddDayForm] = useState(false);
  const [activeDay, setActiveDay] = useState(null);
  const [selectedPlace, setSelectedPlace] = useState(null);
  const [isApiFailed, setIsApiFailed] = useState(false);
  const [showMapSearch, setShowMapSearch] = useState(false);
  const [showRegionInfo, setShowRegionInfo] = useState(true); // 지역 정보 표시 여부

  useEffect(() => {
    const fetchTripDetail = async () => {
      try {
        // API 호출 시도
        const response = await tripApi.getTripDetail(tripId);
        
        // API 응답에서 지역 정보 처리
        const tripData = { ...response.data };
        
        console.log('서버에서 받은 여행 데이터:', tripData); // 디버깅용
        
        // 지역 정보 유효성 검사 및 처리
        if (!tripData.region || !tripData.regionLat || !tripData.regionLng) {
          console.log('지역 정보가 없거나 불완전합니다. 보정합니다.');
          
          // 지역 이름만 있는 경우 좌표 찾기
          if (tripData.region) {
            const selectedRegion = regionOptions.find(r => r.name === tripData.region);
            if (selectedRegion) {
              console.log(`'${tripData.region}' 지역의 좌표를 설정합니다.`);
              tripData.regionLat = selectedRegion.lat;
              tripData.regionLng = selectedRegion.lng;
            }
          } else {
            // 지역 정보가 전혀 없는 경우 기본값 (서울)으로 설정
            console.log('지역 정보가 없어 서울로 기본 설정합니다.');
            tripData.region = '서울';
            tripData.regionLat = 37.5665;
            tripData.regionLng = 126.9780;
          }
        } else {
          // 숫자형으로 변환 및 유효성 검사
          try {
            const lat = typeof tripData.regionLat === 'string' ? parseFloat(tripData.regionLat) : tripData.regionLat;
            const lng = typeof tripData.regionLng === 'string' ? parseFloat(tripData.regionLng) : tripData.regionLng;
            
            if (isNaN(lat) || isNaN(lng)) {
              console.log('유효하지 않은 좌표입니다. 지역명으로 보정합니다.');
              const selectedRegion = regionOptions.find(r => r.name === tripData.region);
              if (selectedRegion) {
                tripData.regionLat = selectedRegion.lat;
                tripData.regionLng = selectedRegion.lng;
              } else {
                // 지역 정보도 유효하지 않으면 서울로 설정
                tripData.region = '서울';
                tripData.regionLat = 37.5665;
                tripData.regionLng = 126.9780;
              }
            } else {
              // 유효한 숫자면 업데이트
              tripData.regionLat = lat;
              tripData.regionLng = lng;
            }
          } catch (e) {
            console.error('지역 좌표 변환 오류:', e);
            // 오류 발생 시 기본값 설정
            const selectedRegion = regionOptions.find(r => r.name === tripData.region);
            if (selectedRegion) {
              tripData.regionLat = selectedRegion.lat;
              tripData.regionLng = selectedRegion.lng;
            } else {
              tripData.region = '서울';
              tripData.regionLat = 37.5665;
              tripData.regionLng = 126.9780;
            }
          }
          
          console.log(`여행 지역: ${tripData.region}, 좌표: ${tripData.regionLat}, ${tripData.regionLng}`);
        }
        
        setTrip(tripData);
        
        // 여행 날짜 정보도 함께 가져옴
        const daysResponse = await tripDayApi.getTripDays(tripId);
        const sortedDays = daysResponse.data.sort((a, b) => a.day - b.day);
        setTripDays(sortedDays);
        
        if (sortedDays.length > 0) {
          setActiveDay(sortedDays[0].id); // 첫 번째 날짜를 활성화
        }
        
        // 모든 날짜의 장소 정보 가져오기
        const allPlacesTemp = [];
        for (const day of sortedDays) {
          try {
            const placesResponse = await tripPlaceApi.getTripPlaces(day.id);
            // 날짜 정보를 포함하여 저장
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
        setLoading(false);
      } catch (err) {
        console.error('여행 상세 정보 가져오기 실패:', err);
        
        // API 실패 시 폴백 데이터 사용
        console.log('폴백 데이터를 사용합니다.');
        setIsApiFailed(true);
        setTrip(fallbackTrip);
        setTripDays(fallbackDays);
        
        // 폴백 장소 데이터를 활성화된 날짜에 맞게 필터링하여 설정
        const processedPlaces = fallbackPlaces.map(place => ({
          ...place,
          dayInfo: {
            dayId: place.dayId,
            day: fallbackDays.find(d => d.id === place.dayId)?.day || 1,
            date: fallbackDays.find(d => d.id === place.dayId)?.date || fallbackTrip.startDate
          }
        }));
        
        setAllPlaces(processedPlaces);
        setActiveDay(fallbackDays[0].id); // 첫 번째 날짜 활성화
        setLoading(false);
      }
    };

    fetchTripDetail();
  }, [tripId]);

  const handleDeleteTrip = async () => {
    if (isApiFailed) {
      alert('현재 API 연결이 원활하지 않아 이 기능을 사용할 수 없습니다.');
      return;
    }
    
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
    if (isApiFailed) {
      alert('현재 API 연결이 원활하지 않아 이 기능을 사용할 수 없습니다.');
      setShowAddDayForm(false);
      return;
    }
    
    try {
      const response = await tripDayApi.createTripDay(tripId, newTripDay);
      const newDays = [...tripDays, response.data].sort((a, b) => a.day - b.day);
      setTripDays(newDays);
      setActiveDay(response.data.id); // 새로 추가된 날짜를 활성화
      setShowAddDayForm(false);
    } catch (err) {
      console.error('여행 날짜 추가 실패:', err);
      alert('여행 날짜를 추가하는데 문제가 발생했습니다.');
    }
  };
  
  const handleDeleteTripDay = async (dayId) => {
    if (isApiFailed) {
      alert('현재 API 연결이 원활하지 않아 이 기능을 사용할 수 없습니다.');
      return;
    }
    
    if (window.confirm('정말로 이 날짜를 삭제하시겠습니까? 모든 장소 정보가 함께 삭제됩니다.')) {
      try {
        await tripDayApi.deleteTripDay(tripId, dayId);
        const updatedDays = tripDays.filter(day => day.id !== dayId);
        setTripDays(updatedDays);
        
        // 모든 장소 목록에서도 해당 날짜의 장소들 제거
        setAllPlaces(allPlaces.filter(place => place.dayInfo.dayId !== dayId));
        
        // 삭제된 날짜가 활성화된 날짜였다면 다른 날짜로 변경
        if (activeDay === dayId) {
          if (updatedDays.length > 0) {
            setActiveDay(updatedDays[0].id);
          } else {
            setActiveDay(null);
          }
        }
        
        alert('날짜가 성공적으로 삭제되었습니다.');
      } catch (err) {
        console.error('날짜 삭제 실패:', err);
        alert('날짜를 삭제하는데 문제가 발생했습니다.');
      }
    }
  };

  // 선택된 날짜에 해당하는 장소 목록을 가져옴
  const getActiveDayPlaces = () => {
    if (!activeDay) return [];
    return allPlaces.filter(place => place.dayInfo.dayId === activeDay);
  };

  // 지역에 따른 지도 확대 레벨 가져오기
  const getMapLevel = () => {
    if (!trip) return 7; // trip이 null이면 기본값 반환
    
    if (trip.region) {
      const selectedRegion = regionOptions.find(r => r.name === trip.region);
      if (selectedRegion) {
        return selectedRegion.level;
      }
    }
    return 7; // 기본값
  };

  // 지도의 중심점 계산
  const getMapCenter = () => {
    if (!trip) {
      // trip이 null이면 서울 좌표 반환
      return {
        lat: 37.5665,
        lng: 126.9780
      };
    }
    
    // 선택된 장소가 있으면 해당 장소 중심
    const activePlaces = getActiveDayPlaces();
    if (selectedPlace && activePlaces.length > 0) {
      const selectedPlaceIndex = activePlaces.findIndex(p => p.id === selectedPlace);
      if (selectedPlaceIndex !== -1) {
        return {
          lat: activePlaces[selectedPlaceIndex].latitude,
          lng: activePlaces[selectedPlaceIndex].longitude
        };
      }
    }
    
    // 선택된 장소가 없으면 여행 지역 중심
    if (trip.regionLat && trip.regionLng) {
      try {
        // 문자열이든 숫자든 안전하게 처리
        const lat = typeof trip.regionLat === 'string' ? parseFloat(trip.regionLat) : trip.regionLat;
        const lng = typeof trip.regionLng === 'string' ? parseFloat(trip.regionLng) : trip.regionLng;
        
        // NaN 체크
        if (!isNaN(lat) && !isNaN(lng)) {
          return { lat, lng };
        }
      } catch (e) {
        console.error('지역 좌표 변환 오류:', e);
      }
    }
    
    // 지역 이름으로 좌표 찾기
    if (trip.region) {
      const selectedRegion = regionOptions.find(r => r.name === trip.region);
      if (selectedRegion) {
        return {
          lat: selectedRegion.lat,
          lng: selectedRegion.lng
        };
      }
    }
    
    // 기본값은 서울 좌표
    return {
      lat: 37.5665,
      lng: 126.9780
    };
  };

  // 마커 클릭 이벤트 핸들러
  const handleMarkerClick = (placeId) => {
    setSelectedPlace(placeId);
  };

  // 장소 삭제 기능
  const handleDeletePlace = (placeId) => {
    if (window.confirm('정말로 이 장소를 삭제하시겠습니까?')) {
      if (!isApiFailed) {
        // API 연결이 정상일 때는 API 호출
        try {
          const placeToDelete = allPlaces.find(place => place.id === placeId);
          if (placeToDelete) {
            tripPlaceApi.deleteTripPlace(placeToDelete.dayInfo.dayId, placeId)
              .then(() => {
                setAllPlaces(allPlaces.filter(place => place.id !== placeId));
                alert('장소가 성공적으로 삭제되었습니다.');
              })
              .catch((err) => {
                console.error('장소 삭제 실패:', err);
                alert('장소를 삭제하는데 실패했습니다.');
              });
          }
        } catch (err) {
          console.error('장소 삭제 처리 중 오류 발생:', err);
          alert('장소 삭제 중 오류가 발생했습니다.');
        }
      } else {
        // API 연결 실패 시 로컬 상태에서만 삭제
        setAllPlaces(allPlaces.filter(place => place.id !== placeId));
        alert('장소가 목록에서 삭제되었습니다. (API 연결이 없어 임시 저장됨)');
      }
    }
  };
  
  // 장소 메모 수정 기능
  const handleEditPlaceMemo = (placeId) => {
    const placeToEdit = allPlaces.find(place => place.id === placeId);
    if (!placeToEdit) return;
    
    const newMemo = window.prompt('장소 메모를 입력하세요:', placeToEdit.memo || '');
    
    if (newMemo !== null) { // 취소를 누르지 않았을 경우만 처리
      if (!isApiFailed) {
        // API 연결이 정상일 때는 API 호출
        try {
          const updatedPlace = { ...placeToEdit, memo: newMemo };
          tripPlaceApi.updateTripPlace(placeToEdit.dayInfo.dayId, placeId, updatedPlace)
            .then(() => {
              setAllPlaces(allPlaces.map(place => 
                place.id === placeId ? { ...place, memo: newMemo } : place
              ));
              alert('메모가 성공적으로 수정되었습니다.');
            })
            .catch((err) => {
              console.error('장소 메모 수정 실패:', err);
              alert('메모를 수정하는데 실패했습니다.');
            });
        } catch (err) {
          console.error('메모 수정 처리 중 오류 발생:', err);
          alert('메모 수정 중 오류가 발생했습니다.');
        }
      } else {
        // API 연결 실패 시 로컬 상태에서만 수정
        setAllPlaces(allPlaces.map(place => 
          place.id === placeId ? { ...place, memo: newMemo } : place
        ));
        alert('메모가 수정되었습니다. (API 연결이 없어 임시 저장됨)');
      }
    }
  };

  // 장소 추가 처리 함수
  const handleAddPlace = (newPlace) => {
    console.log('선택된 장소 정보:', newPlace);
    console.log('현재 선택된 날짜:', activeDay);
    
    if (!activeDay) {
      alert('먼저 날짜를 선택해주세요.');
      return;
    }
    
    // 방문 순서는 현재 선택된 날짜의 마지막 장소 다음 순서로 설정
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
      visitTime: newPlace.visitTime || new Date().toLocaleTimeString().substring(0, 5), // 전달받은 방문 시간 사용
      memo: newPlace.memo || ''
    };
    
    if (!isApiFailed) {
      // API 연결이 정상일 때는 API 호출
      console.log('API 호출 전 데이터:', { tripDayId: activeDay, placeData });
      try {
        tripPlaceApi.createTripPlace(activeDay, placeData)
          .then(response => {
            console.log('API 응답:', response.data);
            // 새로 추가된 장소 정보에 날짜 정보 추가
            const activeDayInfo = tripDays.find(day => day.id === activeDay);
            if (!activeDayInfo) {
              console.error('활성 날짜 정보를 찾을 수 없습니다:', activeDay);
              alert('날짜 정보를 찾을 수 없어 장소 추가에 문제가 발생했습니다.');
              return;
            }
            
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
            alert('장소가 성공적으로 추가되었습니다.');
          })
          .catch(err => {
            console.error('장소 추가 실패:', err);
            if (err.response) {
              console.error('응답 데이터:', err.response.data);
              console.error('응답 상태:', err.response.status);
            }
            alert(`장소를 추가하는데 문제가 발생했습니다: ${err.message || '알 수 없는 오류'}`);
          });
      } catch (err) {
        console.error('API 호출 준비 중 오류:', err);
        alert('장소 추가 요청 중 오류가 발생했습니다.');
      }
    } else {
      // API 연결 실패 시 로컬 상태에만 추가
      const newId = Math.max(...allPlaces.map(p => p.id), 0) + 1; // 임의의 새 ID 생성
      const activeDayInfo = tripDays.find(day => day.id === activeDay);
      
      const newPlaceWithDayInfo = {
        ...placeData,
        id: newId,
        dayInfo: {
          dayId: activeDayInfo.id,
          day: activeDayInfo.day,
          date: activeDayInfo.date
        }
      };
      
      setAllPlaces([...allPlaces, newPlaceWithDayInfo]);
      setSelectedPlace(newId);
      alert('장소가 추가되었습니다. (API 연결이 없어 임시 저장됨)');
    }
    
    setShowMapSearch(false);
  };

  // 컴포넌트 마운트 후 3초 후에 지역 정보 표시 숨기기
  useEffect(() => {
    if (trip) {
      const timer = setTimeout(() => {
        setShowRegionInfo(false);
      }, 3000);
      
      return () => clearTimeout(timer);
    }
  }, [trip]);

  if (loading) return <div className="loading">로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!trip) return <div className="error">여행 정보를 찾을 수 없습니다.</div>;

  const activeDayPlaces = getActiveDayPlaces().sort((a, b) => a.visitOrder - b.visitOrder);

  return (
    <div className="trip-detail-container">
      {isApiFailed && (
        <div className="api-error-banner">
          <p>API 연결이 원활하지 않아 일부 기능이 제한됩니다. 기본 데이터를 표시합니다.</p>
        </div>
      )}
      
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
          <Link to={`/trips/edit/${tripId}`} className="edit-button">
            수정
          </Link>
          <button onClick={handleDeleteTrip} className="delete-button">
            삭제
          </button>
        </div>
      </div>

      <div className="trip-content">
        {/* 좌측 패널: 날짜 및 일정 목록 */}
        <div className="trip-sidebar">
          <div className="trip-day-tabs">
            {tripDays.map((day) => (
              <button
                key={day.id}
                className={`trip-day-tab ${activeDay === day.id ? 'active' : ''}`}
                onClick={() => setActiveDay(day.id)}
              >
                <span className="day-number">{day.day}일차</span>
                <span className="day-date">{new Date(day.date).toLocaleDateString()}</span>
              </button>
            ))}
            <button 
              className="add-day-tab"
              onClick={() => setShowAddDayForm(!showAddDayForm)}
            >
              + 날짜 추가
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

          <div className="trip-day-detail">
            {activeDay && (
              <TripDayItem 
                tripDay={tripDays.find(day => day.id === activeDay)}
                tripId={tripId}
                onDeleteDay={handleDeleteTripDay}
              />
            )}
          </div>
          
          <div className="trip-map-controls-sidebar">
            <button 
              className="add-place-btn"
              onClick={() => setShowMapSearch(true)}
            >
              장소 추가
            </button>
          </div>
        </div>

        {/* 우측 패널: 지도 */}
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
            {/* 지역 중심 마커 - 항상 표시 */}
            <MapMarker
              position={{
                lat: (trip?.regionLat && !isNaN(parseFloat(String(trip.regionLat)))) 
                  ? parseFloat(String(trip.regionLat)) 
                  : (regionOptions.find(r => r.name === trip?.region)?.lat || 37.5665),
                lng: (trip?.regionLng && !isNaN(parseFloat(String(trip.regionLng)))) 
                  ? parseFloat(String(trip.regionLng)) 
                  : (regionOptions.find(r => r.name === trip?.region)?.lng || 126.9780)
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
                  lat: (trip?.regionLat && !isNaN(parseFloat(String(trip.regionLat)))) 
                    ? parseFloat(String(trip.regionLat)) 
                    : (regionOptions.find(r => r.name === trip?.region)?.lat || 37.5665),
                  lng: (trip?.regionLng && !isNaN(parseFloat(String(trip.regionLng)))) 
                    ? parseFloat(String(trip.regionLng)) 
                    : (regionOptions.find(r => r.name === trip?.region)?.lng || 126.9780)
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
                {/* 장소 번호와 이름 표시 오버레이 */}
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
            
            {/* 경로 선 그리기 */}
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
          regionLat={trip?.regionLat || (regionOptions.find(r => r.name === trip?.region)?.lat || 37.5665)}
          regionLng={trip?.regionLng || (regionOptions.find(r => r.name === trip?.region)?.lng || 126.9780)}
          regionName={trip?.region || '서울'}
          mapLevel={getMapLevel()}
          tripDayId={activeDay}
        />
      )}
      
      <div className="back-button-container">
        <button onClick={() => navigate('/')} className="back-button">
          목록으로 돌아가기
        </button>
      </div>
    </div>
  );
};

export default TripDetailPage; 