import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const AuthContext = createContext();

// 환경 변수에서 API URL을 가져오거나 기본값 사용
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내에서 사용되어야 합니다.');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 로컬 스토리지에서 토큰 관리
  const getStoredToken = () => localStorage.getItem('accessToken');
  const setStoredToken = (token) => {
    if (token) {
      localStorage.setItem('accessToken', token);
    } else {
      localStorage.removeItem('accessToken');
    }
  };

  // API 클라이언트 생성 (인증 헤더 포함)
  const createApiClient = (token = null) => {
    const client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true, // 쿠키 포함
    });

    // 토큰이 있으면 Authorization 헤더 추가
    if (token) {
      client.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }

    // 응답 인터셉터 - 401 에러 시 로그아웃 처리
    client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // 토큰 만료 시 리프레시 시도
          try {
            await refreshToken();
            // 원래 요청 재시도
            const originalRequest = error.config;
            const newToken = getStoredToken();
            if (newToken) {
              originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
              return client.request(originalRequest);
            }
          } catch (refreshError) {
            // 리프레시 실패 시 로그아웃
            logout();
          }
        }
        return Promise.reject(error);
      }
    );

    return client;
  };

  // 현재 사용자 정보 가져오기
  const getCurrentUser = useCallback(async () => {
    try {
      const token = getStoredToken();
      if (!token) {
        setLoading(false);
        return;
      }

      const client = createApiClient(token);
      const response = await client.get('/members/me');
      setUser(response.data);
      setError(null);
    } catch (err) {
      console.error('사용자 정보 가져오기 실패:', err);
      // 토큰이 유효하지 않으면 제거
      setStoredToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  // 로그인
  const login = async (email, password) => {
    try {
      setLoading(true);
      setError(null);

      const client = createApiClient();
      const response = await client.post('/members/login', {
        email,
        password,
      });

      // 백엔드에서 쿠키로 토큰을 설정하지만, 응답에서도 토큰을 받을 수 있도록 준비
      // 현재 백엔드 구현에서는 쿠키 방식을 사용하므로 별도 토큰 저장은 불필요할 수 있음
      
      // 사용자 정보 설정
      setUser(response.data);
      
      // 현재 사용자 정보 다시 가져오기 (최신 정보 확보)
      await getCurrentUser();

      return response.data;
    } catch (err) {
      console.error('로그인 실패:', err);
      const errorMessage = err.response?.data?.message || '로그인에 실패했습니다.';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 회원가입
  const signup = async (email, nickname, password) => {
    try {
      setLoading(true);
      setError(null);

      const client = createApiClient();
      const response = await client.post('/members/signup', {
        email,
        nickname,
        password,
      });

      return response.data;
    } catch (err) {
      console.error('회원가입 실패:', err);
      const errorMessage = err.response?.data?.message || '회원가입에 실패했습니다.';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 토큰 갱신
  const refreshToken = async () => {
    try {
      const client = createApiClient();
      await client.post('/members/refresh');
      // 쿠키 방식이므로 별도 토큰 처리 불필요
      return true;
    } catch (err) {
      console.error('토큰 갱신 실패:', err);
      throw err;
    }
  };

  // 로그아웃
  const logout = async () => {
    try {
      const client = createApiClient();
      const response = await client.post('/members/logout');
      console.log('로그아웃 응답:', response.data);
    } catch (err) {
      console.error('로그아웃 요청 실패:', err);
      // 에러가 발생해도 로컬 상태는 정리
    } finally {
      // 로컬 상태 정리
      setUser(null);
      setStoredToken(null);
      setError(null);
    }
  };

  // 전체 기기에서 로그아웃
  const logoutAll = async () => {
    try {
      const client = createApiClient();
      await client.post('/members/logout-all');
    } catch (err) {
      console.error('전체 로그아웃 실패:', err);
    } finally {
      // 로컬 상태 정리
      setUser(null);
      setStoredToken(null);
      setError(null);
    }
  };

  // 컴포넌트 마운트 시 사용자 정보 확인
  useEffect(() => {
    getCurrentUser();
  }, [getCurrentUser]);

  const value = {
    user,
    loading,
    error,
    login,
    signup,
    logout,
    logoutAll,
    refreshToken,
    getCurrentUser,
    isAuthenticated: !!user,
    createApiClient,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};