import axios from 'axios';

// 환경 변수에서 API URL을 가져오거나 기본값 사용
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

// AuthContext에서 사용할 수 있도록 API 클라이언트 생성 함수
let authContextRef = null;

export const setAuthContextRef = (authContext) => {
  authContextRef = authContext;
};

// 인증된 API 클라이언트 생성 함수
const createAuthenticatedApi = () => {
  if (authContextRef && authContextRef.createApiClient) {
    return authContextRef.createApiClient();
  }
  
  // AuthContext가 없는 경우 기본 클라이언트 반환
  return axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // 쿠키 포함
  });
};

// 기본 Axios 인스턴스 생성 (인증이 필요없는 요청용)
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 포함
});

// 요청 인터셉터 추가 - 디버깅용
api.interceptors.request.use(
  config => {
    console.log('API 요청:', config.method.toUpperCase(), config.url, config.data);
    return config;
  },
  error => {
    console.error('API 요청 오류:', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터 추가 - 디버깅용
api.interceptors.response.use(
  response => {
    console.log('API 응답:', response.status, response.data);
    return response;
  },
  error => {
    console.error('API 응답 오류:', error.response ? error.response.status : 'No response', 
                 error.response ? error.response.data : error.message);
    return Promise.reject(error);
  }
);

// 여행 관련 API (인증 필요)
const tripApi = {
  // 모든 여행 가져오기
  getAllTrips: () => {
    const authApi = createAuthenticatedApi();
    return authApi.get('/trips');
  },
  
  // 특정 여행 상세 정보 가져오기
  getTripDetail: (tripId) => {
    const authApi = createAuthenticatedApi();
    return authApi.get(`/trips/${tripId}`);
  },
  
  // 새 여행 생성하기
  createTrip: (tripData) => {
    const authApi = createAuthenticatedApi();
    return authApi.post('/trips', tripData);
  },
  
  // 여행 정보 수정하기
  updateTrip: (tripId, tripData) => {
    const authApi = createAuthenticatedApi();
    return authApi.put(`/trips/${tripId}`, tripData);
  },
  
  // 여행 삭제하기
  deleteTrip: (tripId) => {
    const authApi = createAuthenticatedApi();
    return authApi.delete(`/trips/${tripId}`);
  }
};

// 여행 날짜 관련 API (인증 필요)
const tripDayApi = {
  // 여행에 날짜 추가하기
  createTripDay: (tripId, tripDayData) => {
    const authApi = createAuthenticatedApi();
    return authApi.post(`/trips/${tripId}/days`, tripDayData);
  },
  
  // 여행의 날짜 목록 가져오기
  getTripDays: (tripId) => {
    const authApi = createAuthenticatedApi();
    return authApi.get(`/trips/${tripId}/days`);
  },
  
  // 여행 날짜 삭제하기
  deleteTripDay: (tripId, dayId) => {
    const authApi = createAuthenticatedApi();
    return authApi.delete(`/trips/${tripId}/days/${dayId}`);
  }
};

// 여행 장소 관련 API (인증 필요)
const tripPlaceApi = {
  // 날짜에 장소 추가하기
  createTripPlace: (tripDayId, placeData) => {
    const authApi = createAuthenticatedApi();
    return authApi.post(`/trip-days/${tripDayId}/places`, placeData);
  },
  
  // 날짜의 장소 목록 가져오기
  getTripPlaces: (tripDayId) => {
    const authApi = createAuthenticatedApi();
    return authApi.get(`/trip-days/${tripDayId}/places`);
  },
  
  // 장소 정보 수정하기
  updateTripPlace: (tripDayId, placeId, placeData) => {
    const authApi = createAuthenticatedApi();
    return authApi.put(`/trip-days/${tripDayId}/places/${placeId}`, placeData);
  },
  
  // 장소 방문 순서 변경하기
  updateTripPlaceOrder: (tripDayId, placeId, orderData) => {
    const authApi = createAuthenticatedApi();
    return authApi.patch(`/trip-days/${tripDayId}/places/${placeId}/order`, orderData);
  },
  
  // 장소 삭제하기
  deleteTripPlace: (tripDayId, placeId) => {
    const authApi = createAuthenticatedApi();
    return authApi.delete(`/trip-days/${tripDayId}/places/${placeId}`);
  }
};

// 장소 검색 관련 API (인증 필요)
const placeApi = {
  // 장소 검색 (카카오맵 API 활용)
  searchPlaces: (keyword, lat, lng, page = 1, size = 15, tripId = null) => {
    const authApi = createAuthenticatedApi();
    let url = `/search/places?keyword=${encodeURIComponent(keyword)}&lat=${lat}&lng=${lng}&page=${page}&size=${size}`;
    if (tripId) {
      url += `&tripId=${tripId}`;
    }
    return authApi.get(url);
  },
  
  // 검색된 장소를 여행에 저장
  saveSearchedPlace: (placeData) => {
    const authApi = createAuthenticatedApi();
    return authApi.post('/search/save-to-trip', placeData);
  }
};

// 인증 관련 API (인증 불필요)
const authApi = {
  // 로그인
  login: (email, password) => {
    return api.post('/members/login', { email, password });
  },
  
  // 회원가입
  signup: (email, nickname, password) => {
    return api.post('/members/signup', { email, nickname, password });
  },
  
  // 토큰 갱신
  refresh: () => {
    return api.post('/members/refresh');
  },
  
  // 로그아웃
  logout: () => {
    return api.post('/members/logout');
  },
  
  // 전체 로그아웃
  logoutAll: () => {
    return api.post('/members/logout-all');
  },
  
  // 현재 사용자 정보 조회
  getCurrentUser: () => {
    const authApi = createAuthenticatedApi();
    return authApi.get('/members/me');
  }
};

export { tripApi, tripDayApi, tripPlaceApi, placeApi, authApi };