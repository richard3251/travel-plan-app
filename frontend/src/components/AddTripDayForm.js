import React, { useState, useEffect } from 'react';
import './AddTripDayForm.css';

const AddTripDayForm = ({ onAddDay, tripStartDate, tripEndDate, existingDays }) => {
  const [formData, setFormData] = useState({
    day: 1,
    date: ''
  });
  const [availableDates, setAvailableDates] = useState([]);
  
  useEffect(() => {
    // 여행 기간 중 사용 가능한 날짜들 계산
    const start = new Date(tripStartDate);
    const end = new Date(tripEndDate);
    const dates = [];
    
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      // 이미 존재하는 날짜인지 확인
      const dateStr = d.toISOString().split('T')[0];
      const existingDay = existingDays.find(day => {
        const dayDate = new Date(day.date).toISOString().split('T')[0];
        return dayDate === dateStr;
      });
      
      if (!existingDay) {
        dates.push({
          date: dateStr,
          day: calculateDayNumber(d, start)
        });
      }
    }
    
    setAvailableDates(dates);
    
    // 사용 가능한 날짜가 있으면 첫 번째 날짜로 폼 초기화
    if (dates.length > 0) {
      setFormData({
        day: dates[0].day,
        date: dates[0].date
      });
    }
  }, [tripStartDate, tripEndDate, existingDays]);
  
  // 여행 시작일로부터 며칠째인지 계산
  const calculateDayNumber = (date, startDate) => {
    const diffTime = Math.abs(date - startDate);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  };
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    
    if (name === 'date') {
      // 선택한 날짜에 해당하는 day 값 자동 설정
      const selectedDate = availableDates.find(d => d.date === value);
      if (selectedDate) {
        setFormData({
          day: selectedDate.day,
          date: value
        });
      }
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  };
  
  const handleSubmit = (e) => {
    e.preventDefault();
    onAddDay(formData);
  };
  
  if (availableDates.length === 0) {
    return (
      <div className="add-day-form-container">
        <p className="no-available-dates">추가할 수 있는 날짜가 없습니다. 모든 여행 날짜가 이미 등록되었습니다.</p>
      </div>
    );
  }
  
  return (
    <div className="add-day-form-container">
      <form className="add-day-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="date">날짜 선택</label>
          <select
            id="date"
            name="date"
            value={formData.date}
            onChange={handleChange}
            required
          >
            {availableDates.map(d => (
              <option key={d.date} value={d.date}>
                Day {d.day} - {new Date(d.date).toLocaleDateString()}
              </option>
            ))}
          </select>
        </div>
        
        <div className="form-actions">
          <button type="submit" className="submit-button">
            날짜 추가하기
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddTripDayForm; 