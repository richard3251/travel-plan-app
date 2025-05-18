import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 여행 관련 API
const tripApi = {
  // 모든 여행 가져오기
  getAllTrips: () => {
    return api.get('/trips');
  },
  
  // 특정 여행 상세 정보 가져오기
  getTripDetail: (tripId) => {
    return api.get(`/trips/${tripId}`);
  },
  
  // 새 여행 생성하기
  createTrip: (tripData) => {
    return api.post('/trips', tripData);
  },
  
  // 여행 정보 수정하기
  updateTrip: (tripId, tripData) => {
    return api.put(`/trips/${tripId}`, tripData);
  },
  
  // 여행 삭제하기
  deleteTrip: (tripId) => {
    return api.delete(`/trips/${tripId}`);
  }
};

// 여행 날짜 관련 API
const tripDayApi = {
  // 여행에 날짜 추가하기
  createTripDay: (tripId, tripDayData) => {
    return api.post(`/trips/${tripId}/days`, tripDayData);
  },
  
  // 여행의 날짜 목록 가져오기
  getTripDays: (tripId) => {
    return api.get(`/trips/${tripId}/days`);
  },
  
  // 여행 날짜 삭제하기
  deleteTripDay: (tripId, dayId) => {
    return api.delete(`/trips/${tripId}/days/${dayId}`);
  }
};

// 여행 장소 관련 API
const tripPlaceApi = {
  // 날짜에 장소 추가하기
  createTripPlace: (tripDayId, placeData) => {
    return api.post(`/trip-days/${tripDayId}/places`, placeData);
  },
  
  // 날짜의 장소 목록 가져오기
  getTripPlaces: (tripDayId) => {
    return api.get(`/trip-days/${tripDayId}/places`);
  },
  
  // 장소 정보 수정하기
  updateTripPlace: (tripDayId, placeId, placeData) => {
    return api.put(`/trip-days/${tripDayId}/places/${placeId}`, placeData);
  },
  
  // 장소 방문 순서 변경하기
  updateTripPlaceOrder: (tripDayId, placeId, orderData) => {
    return api.patch(`/trip-days/${tripDayId}/places/${placeId}/order`, orderData);
  },
  
  // 장소 삭제하기
  deleteTripPlace: (tripDayId, placeId) => {
    return api.delete(`/trip-days/${tripDayId}/places/${placeId}`);
  }
};

// 장소 검색 관련 API
const placeApi = {
  // 장소 검색 (카카오맵 API 활용)
  searchPlaces: (keyword, lat, lng) => {
    return api.get(`/search/places?keyword=${encodeURIComponent(keyword)}&lat=${lat}&lng=${lng}`);
  },
  
  // 검색된 장소를 여행에 저장
  saveSearchedPlace: (placeData) => {
    return api.post('/search/save-to-trip', placeData);
  }
};

export { tripApi, tripDayApi, tripPlaceApi, placeApi }; 