import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tripApi } from '../api/api';
import './NewTripPage.css'; // 같은 스타일 재사용

// 국내 주요 지역과 좌표 데이터
const regionOptions = [
  { name: '서울', lat: 37.5665, lng: 126.9780 },
  { name: '경기도', lat: 37.4138, lng: 127.5183 },
  { name: '인천', lat: 37.4563, lng: 126.7052 },
  { name: '강원도', lat: 37.8228, lng: 128.1555 },
  { name: '충청북도', lat: 36.8000, lng: 127.7000 },
  { name: '충청남도', lat: 36.5184, lng: 126.8000 },
  { name: '대전', lat: 36.3504, lng: 127.3845 },
  { name: '전라북도', lat: 35.8242, lng: 127.1489 },
  { name: '전라남도', lat: 34.8679, lng: 126.9910 },
  { name: '광주', lat: 35.1595, lng: 126.8526 },
  { name: '경상북도', lat: 36.4919, lng: 128.8889 },
  { name: '경상남도', lat: 35.4606, lng: 128.2132 },
  { name: '대구', lat: 35.8714, lng: 128.6014 },
  { name: '울산', lat: 35.5384, lng: 129.3114 },
  { name: '부산', lat: 35.1796, lng: 129.0756 },
  { name: '제주도', lat: 33.4996, lng: 126.5312 }
];

const EditTripPage = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: '',
    startDate: '',
    endDate: '',
    description: '',
    region: '',
    regionLat: '',
    regionLng: ''
  });

  useEffect(() => {
    const fetchTripDetail = async () => {
      try {
        const response = await tripApi.getTripDetail(tripId);
        const tripData = response.data;

        // 지역 정보가 없는 경우 빈 문자열로 설정
        setFormData({
          title: tripData.title || '',
          startDate: tripData.startDate || '',
          endDate: tripData.endDate || '',
          description: tripData.description || '',
          region: tripData.region || '',
          regionLat: tripData.regionLat || '',
          regionLng: tripData.regionLng || ''
        });
        
        setLoading(false);
      } catch (err) {
        console.error('여행 정보 가져오기 실패:', err);
        alert('여행 정보를 가져오는데 문제가 발생했습니다.');
        navigate('/');
      }
    };

    fetchTripDetail();
  }, [tripId, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleRegionChange = (e) => {
    const selectedRegionName = e.target.value;
    const selectedRegion = regionOptions.find(region => region.name === selectedRegionName);
    
    if (selectedRegion) {
      setFormData({
        ...formData,
        region: selectedRegion.name,
        regionLat: selectedRegion.lat,
        regionLng: selectedRegion.lng
      });
    } else {
      setFormData({
        ...formData,
        region: '',
        regionLat: '',
        regionLng: ''
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.startDate || !formData.endDate || !formData.region) {
      alert('제목, 여행 기간, 지역은 필수 입력 항목입니다.');
      return;
    }

    try {
      setLoading(true);
      await tripApi.updateTrip(tripId, formData);
      alert('여행 정보가 성공적으로 수정되었습니다!');
      navigate(`/trips/${tripId}`);
    } catch (err) {
      console.error('여행 수정 실패:', err);
      alert('여행 정보를 수정하는데 문제가 발생했습니다. 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">로딩 중...</div>;

  return (
    <div className="new-trip-container">
      <h2>여행 정보 수정</h2>
      <form className="trip-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">여행 제목</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="여행 제목을 입력하세요"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="region">여행 지역</label>
          <select
            id="region"
            name="region"
            value={formData.region}
            onChange={handleRegionChange}
            required
          >
            <option value="">지역을 선택하세요</option>
            {regionOptions.map(region => (
              <option key={region.name} value={region.name}>
                {region.name}
              </option>
            ))}
          </select>
        </div>
        
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="startDate">시작일</label>
            <input
              type="date"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="endDate">종료일</label>
            <input
              type="date"
              id="endDate"
              name="endDate"
              value={formData.endDate}
              onChange={handleChange}
              required
            />
          </div>
        </div>
        
        <div className="form-group">
          <label htmlFor="description">여행 설명 (선택사항)</label>
          <textarea
            id="description"
            name="description"
            value={formData.description || ''}
            onChange={handleChange}
            placeholder="여행에 대한 간단한 설명을 입력하세요"
            rows="3"
          />
        </div>
        
        <div className="form-actions">
          <button 
            type="button" 
            className="cancel-button"
            onClick={() => navigate(`/trips/${tripId}`)}
          >
            취소
          </button>
          <button 
            type="submit" 
            className="submit-button"
            disabled={loading}
          >
            {loading ? '저장 중...' : '변경사항 저장'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EditTripPage; 