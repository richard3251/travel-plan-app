import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tripShareApi } from '../api/api';
import { Map, MapMarker, Polyline, CustomOverlayMap } from 'react-kakao-maps-sdk';
import './SharedTripPage.css';

const SharedTripPage = () => {
  const { shareToken } = useParams();
  const navigate = useNavigate();
  const [shareData, setShareData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState(null);

  useEffect(() => {
    loadSharedTrip();
  }, [shareToken]);

  const loadSharedTrip = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await tripShareApi.getSharedTrip(shareToken);
      setShareData(response.data);

      // 첫 번째 날짜를 기본 선택
      if (response.data.trip.tripDays && response.data.trip.tripDays.length > 0) {
        setSelectedDay(response.data.trip.tripDays[0].id);
      }
    } catch (err) {
      console.error('공유된 여행 조회 실패:', err);
      setError(err.response?.data?.message || '공유된 여행을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading-container">로딩 중...</div>;
  if (error) return (
    <div className="error-container">
      <div className="error-message">
        <h2>❌ 오류</h2>
        <p>{error}</p>
        <button onClick={() => navigate('/')} className="home-button">
          홈으로 돌아가기
        </button>
      </div>
    </div>
  );
  if (!shareData) return null;

  const { trip } = shareData;
  const activeDayData = trip.tripDays?.find(day => day.id === selectedDay);
  const activeDayPlaces = activeDayData?.tripPlaces?.sort((a, b) => a.visitOrder - b.visitOrder) || [];

  const getMapCenter = () => {
    if (activeDayPlaces.length > 0) {
      return {
        lat: activeDayPlaces[0].latitude,
        lng: activeDayPlaces[0].longitude
      };
    }
    return { lat: 37.5665, lng: 126.9780 };
  };

  return (
    <div className="shared-trip-container">
      {/* 헤더 */}
      <div className="shared-trip-header">
        <div className="header-content">
          <h1>{trip.title}</h1>
          <p className="trip-period">
            {new Date(trip.startDate).toLocaleDateString()} - {new Date(trip.endDate).toLocaleDateString()}
          </p>
          <div className="share-info">
            <span className="view-count">👁️ 조회수: {shareData.viewCount}</span>
            <span className="share-date">
              공유일: {new Date(shareData.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>
      </div>

      {/* 메인 컨텐츠 */}
      <div className="shared-trip-content">
        {/* 좌측 사이드바 */}
        <div className="shared-trip-sidebar">
          <h3>여행 일정</h3>
          
          {/* 날짜 탭들 */}
          <div className="day-tabs">
            {trip.tripDays?.map((day) => (
              <button
                key={day.id}
                className={`day-tab ${selectedDay === day.id ? 'active' : ''}`}
                onClick={() => setSelectedDay(day.id)}
              >
                <div className="day-number">{day.day}일차</div>
                <div className="day-date">{new Date(day.date).toLocaleDateString()}</div>
                <div className="places-count">
                  {day.tripPlaces?.length || 0}개 장소
                </div>
              </button>
            ))}
          </div>

          {/* 선택된 날짜의 장소 목록 */}
          <div className="places-list">
            <h4>{activeDayData?.day}일차 장소</h4>
            {activeDayPlaces.length === 0 ? (
              <div className="no-places">등록된 장소가 없습니다</div>
            ) : (
              <div className="places">
                {activeDayPlaces.map((place, index) => (
                  <div key={place.id} className="place-item">
                    <div className="place-number">{index + 1}</div>
                    <div className="place-info">
                      <div className="place-name">{place.placeName}</div>
                      <div className="place-address">{place.address}</div>
                      {place.visitTime && (
                        <div className="place-time">⏰ {place.visitTime}</div>
                      )}
                      {place.memo && (
                        <div className="place-memo">📝 {place.memo}</div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 우측 지도 */}
        <div className="shared-trip-map">
          <Map
            center={getMapCenter()}
            style={{ width: '100%', height: '100%' }}
            level={7}
          >
            {/* 장소 마커들 */}
            {activeDayPlaces.map((place, index) => (
              <React.Fragment key={place.id}>
                <MapMarker
                  position={{
                    lat: place.latitude,
                    lng: place.longitude
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

      {/* 푸터 */}
      <div className="shared-trip-footer">
        <button onClick={() => navigate('/')} className="home-button">
          내 여행 만들기
        </button>
      </div>
    </div>
  );
};

export default SharedTripPage;
