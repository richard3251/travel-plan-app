import React, { useState } from 'react';
import './AddPlaceForm.css';

const AddPlaceForm = ({ onAddPlace, dayId, currentOrder }) => {
  const [formData, setFormData] = useState({
    placeName: '',
    address: '',
    latitude: 37.5665, // 서울 기본 위치
    longitude: 126.9780,
    memo: '',
    placeId: '',
    visitTime: '12:00',
    visitOrder: currentOrder
  });
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };
  
  const handleSubmit = (e) => {
    e.preventDefault();
    onAddPlace(formData);
  };
  
  return (
    <div className="add-place-form-container">
      <form className="add-place-form" onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="placeName">장소명</label>
            <input
              type="text"
              id="placeName"
              name="placeName"
              value={formData.placeName}
              onChange={handleChange}
              placeholder="장소 이름을 입력하세요"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="visitTime">방문 시간</label>
            <input
              type="time"
              id="visitTime"
              name="visitTime"
              value={formData.visitTime}
              onChange={handleChange}
              required
            />
          </div>
        </div>
        
        <div className="form-group">
          <label htmlFor="address">주소</label>
          <input
            type="text"
            id="address"
            name="address"
            value={formData.address}
            onChange={handleChange}
            placeholder="주소를 입력하세요"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="memo">메모</label>
          <textarea
            id="memo"
            name="memo"
            value={formData.memo}
            onChange={handleChange}
            placeholder="방문 계획, 주의사항 등을 메모하세요"
            rows="2"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="visitOrder">방문 순서</label>
          <input
            type="number"
            id="visitOrder"
            name="visitOrder"
            value={formData.visitOrder}
            onChange={handleChange}
            min="1"
            required
          />
        </div>
        
        <div className="form-actions">
          <button type="submit" className="submit-button">
            장소 추가하기
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddPlaceForm; 