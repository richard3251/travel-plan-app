import React, { useState } from 'react';
import './TripPlaceItem.css';

const TripPlaceItem = ({ place, onUpdatePlace, onUpdateOrder, onDeletePlace, totalPlaces }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({
    placeName: place.placeName,
    address: place.address,
    memo: place.memo || '',
    visitTime: place.visitTime,
    latitude: place.latitude,
    longitude: place.longitude,
    placeId: place.placeId,
    visitOrder: place.visitOrder
  });
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setEditData({
      ...editData,
      [name]: value
    });
  };
  
  const handleSubmit = (e) => {
    e.preventDefault();
    onUpdatePlace(place.id, editData);
    setIsEditing(false);
  };
  
  const moveUp = () => {
    if (place.visitOrder > 1) {
      onUpdateOrder(place.id, place.visitOrder - 1);
    }
  };
  
  const moveDown = () => {
    if (place.visitOrder < totalPlaces) {
      onUpdateOrder(place.id, place.visitOrder + 1);
    }
  };
  
  React.useEffect(() => {
    setEditData({
      placeName: place.placeName,
      address: place.address,
      memo: place.memo || '',
      visitTime: place.visitTime,
      latitude: place.latitude,
      longitude: place.longitude,
      placeId: place.placeId,
      visitOrder: place.visitOrder
    });
  }, [place]);
  
  return (
    <li className="place-item">
      {isEditing ? (
        <form className="edit-place-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="placeName">장소명</label>
              <input
                type="text"
                id="placeName"
                name="placeName"
                value={editData.placeName}
                onChange={handleChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="visitTime">방문 시간</label>
              <input
                type="time"
                id="visitTime"
                name="visitTime"
                value={editData.visitTime}
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
              value={editData.address}
              onChange={handleChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="memo">메모</label>
            <textarea
              id="memo"
              name="memo"
              value={editData.memo}
              onChange={handleChange}
              rows="2"
            />
          </div>
          
          <div className="edit-actions">
            <button type="button" className="cancel-button" onClick={() => setIsEditing(false)}>
              취소
            </button>
            <button type="submit" className="save-button">
              저장
            </button>
          </div>
        </form>
      ) : (
        <>
          <div className="place-order">
            <span className="order-number">{place.visitOrder}</span>
            <div className="order-controls">
              <button 
                className="move-up" 
                onClick={moveUp}
                disabled={place.visitOrder <= 1}
                title="위로 이동"
              >
                ↑
              </button>
              <button 
                className="move-down" 
                onClick={moveDown}
                disabled={place.visitOrder >= totalPlaces}
                title="아래로 이동"
              >
                ↓
              </button>
            </div>
          </div>
          
          <div className="place-time">
            {place.visitTime.substring(0, 5)}
          </div>
          
          <div className="place-info">
            <div className="place-name">{place.placeName}</div>
            <div className="place-address">{place.address}</div>
            {place.memo && <div className="place-memo">{place.memo}</div>}
          </div>
          
          <div className="place-actions">
            <button 
              className="edit-place" 
              onClick={() => setIsEditing(true)}
              title="장소 정보 수정"
            >
              수정
            </button>
            <button 
              className="delete-place" 
              onClick={() => onDeletePlace(place.id)}
              title="장소 삭제"
            >
              삭제
            </button>
          </div>
        </>
      )}
    </li>
  );
};

export default TripPlaceItem; 