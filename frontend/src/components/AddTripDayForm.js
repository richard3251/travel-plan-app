import React, { useState } from 'react';
import './AddTripDayForm.css';

const AddTripDayForm = ({ tripId, onClose, onAddDay, existingDays }) => {
  const [formData, setFormData] = useState({
    date: '',
    day: existingDays.length + 1
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.date) {
      alert('날짜를 선택해주세요.');
      return;
    }

    // 중복 날짜 체크
    const isDuplicateDate = existingDays.some(day => day.date === formData.date);
    if (isDuplicateDate) {
      alert('이미 존재하는 날짜입니다.');
      return;
    }

    try {
      await onAddDay(formData);
      onClose();
    } catch (err) {
      console.error('날짜 추가 실패:', err);
      alert('날짜를 추가하는데 문제가 발생했습니다.');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>새 날짜 추가</h3>
          <button className="modal-close-btn" onClick={onClose}>
            ×
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="add-day-form">
          <div className="form-field">
            <label htmlFor="day">일차</label>
            <input
              type="number"
              id="day"
              name="day"
              value={formData.day}
              onChange={handleInputChange}
              min="1"
              required
            />
          </div>
          
          <div className="form-field">
            <label htmlFor="date">날짜</label>
            <input
              type="date"
              id="date"
              name="date"
              value={formData.date}
              onChange={handleInputChange}
              required
            />
          </div>
          
          <div className="form-actions">
            <button type="button" onClick={onClose} className="cancel-btn">
              취소
            </button>
            <button type="submit" className="submit-btn">
              추가
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddTripDayForm; 