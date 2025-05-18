import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripApi } from '../api/api';
import './NewTripPage.css';

const NewTripPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    startDate: '',
    endDate: '',
    description: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.startDate || !formData.endDate) {
      alert('제목과 여행 기간은 필수 입력 항목입니다.');
      return;
    }

    try {
      setLoading(true);
      await tripApi.createTrip(formData);
      alert('여행이 성공적으로 생성되었습니다!');
      navigate('/');
    } catch (err) {
      console.error('여행 생성 실패:', err);
      alert('여행을 생성하는데 문제가 발생했습니다. 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="new-trip-container">
      <h2>새 여행 계획 만들기</h2>
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
          <label htmlFor="description">여행 설명</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="여행에 대한 간단한 설명을 입력하세요"
            rows="4"
          />
        </div>
        
        <div className="form-actions">
          <button 
            type="button" 
            className="cancel-button"
            onClick={() => navigate('/')}
          >
            취소
          </button>
          <button 
            type="submit" 
            className="submit-button"
            disabled={loading}
          >
            {loading ? '생성 중...' : '여행 생성하기'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default NewTripPage; 