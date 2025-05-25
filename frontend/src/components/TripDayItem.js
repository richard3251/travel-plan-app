import React, { useState, useEffect, useCallback } from 'react';
import { tripPlaceApi, placeApi } from '../api/api';
import TripPlaceItem from './TripPlaceItem';
import './TripDayItem.css';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';

const TripDayItem = ({ tripDay, tripId, places = [], onPlaceUpdate, onPlaceDelete }) => {
  const [tripPlaces, setTripPlaces] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editingPlace, setEditingPlace] = useState(null);
  const [editData, setEditData] = useState({
    memo: '',
    visitTime: ''
  });
  
  // props로 받은 places를 tripPlaces에 설정
  useEffect(() => {
    if (places && places.length >= 0) {
      setTripPlaces(places.sort((a, b) => a.visitOrder - b.visitOrder));
      setLoading(false);
    }
  }, [places]);
  
  const fetchPlaces = useCallback(async () => {
    if (places && places.length > 0) {
      // props로 데이터를 받은 경우 API 호출하지 않음
      return;
    }
    
    try {
      setLoading(true);
      const response = await tripPlaceApi.getTripPlaces(tripDay.id);
      const placesWithDayInfo = response.data.map(place => ({
        ...place,
        dayInfo: {
          dayId: tripDay.id,
          day: tripDay.day,
          date: tripDay.date
        }
      }));
      setTripPlaces(placesWithDayInfo);
      
      // 상위 컴포넌트에 업데이트된 장소 정보 전달
      if (onPlaceUpdate) {
        onPlaceUpdate(tripDay.id, placesWithDayInfo);
      }
      
      setLoading(false);
    } catch (err) {
      setLoading(false);
      console.error('장소 목록을 가져오는데 실패했습니다:', err);
    }
  }, [tripDay.id, places, onPlaceUpdate, tripDay.day, tripDay.date]);
  
  useEffect(() => {
    if (!places || places.length === 0) {
      fetchPlaces();
    }
  }, [tripDay.id, fetchPlaces, places]);
  
  const handleUpdatePlace = async (placeId, updatedData) => {
    try {
      const response = await tripPlaceApi.updateTripPlace(tripDay.id, placeId, updatedData);
      const updatedPlace = {
        ...response.data,
        dayInfo: {
          dayId: tripDay.id,
          day: tripDay.day,
          date: tripDay.date
        }
      };
      
      const updatedPlaces = tripPlaces.map(place => 
        place.id === placeId ? updatedPlace : place
      );
      
      setTripPlaces(updatedPlaces);
      
      // 상위 컴포넌트에 업데이트 전달
      if (onPlaceUpdate) {
        onPlaceUpdate(tripDay.id, updatedPlaces);
      }
    } catch (err) {
      alert('장소를 수정하는데 문제가 발생했습니다.');
    }
  };

  const handleDragEnd = async (result) => {
    if (!result.destination) return;
    if (result.destination.index === result.source.index) return;
    
    try {
      const sortedPlaces = [...tripPlaces].sort((a, b) => a.visitOrder - b.visitOrder);
      const draggedPlace = sortedPlaces[result.source.index];
      const newVisitOrder = result.destination.index + 1;
      
      await tripPlaceApi.updateTripPlaceOrder(
        tripDay.id, 
        draggedPlace.id, 
        { visitOrder: newVisitOrder }
      );
      
      // 순서 변경 후 데이터 다시 가져오기
      await fetchPlaces();
    } catch (err) {
      alert('장소 순서를 변경하는데 문제가 발생했습니다.');
      fetchPlaces();
    }
  };
  
  const handleDeletePlace = async (placeId) => {
    if (window.confirm('정말로 이 장소를 삭제하시겠습니까?')) {
      try {
        await tripPlaceApi.deleteTripPlace(tripDay.id, placeId);
        
        const updatedPlaces = tripPlaces.filter(place => place.id !== placeId);
        setTripPlaces(updatedPlaces);
        
        // 상위 컴포넌트에 삭제 전달
        if (onPlaceDelete) {
          onPlaceDelete(placeId);
        }
        
        alert('장소가 성공적으로 삭제되었습니다.');
      } catch (err) {
        alert('장소를 삭제하는데 문제가 발생했습니다.');
      }
    }
  };

  const handleEditPlace = (place) => {
    setEditingPlace(place.id);
    setEditData({
      memo: place.memo || '',
      visitTime: place.visitTime || ''
    });
  };

  const handleSavePlace = async (placeId) => {
    try {
      const place = tripPlaces.find(p => p.id === placeId);
      const updatedPlace = { 
        ...place, 
        memo: editData.memo,
        visitTime: editData.visitTime
      };
      await tripPlaceApi.updateTripPlace(tripDay.id, placeId, updatedPlace);
      setTripPlaces(tripPlaces.map(p => 
        p.id === placeId ? { ...p, memo: editData.memo, visitTime: editData.visitTime } : p
      ));
      setEditingPlace(null);
      setEditData({ memo: '', visitTime: '' });
      alert('장소 정보가 성공적으로 저장되었습니다.');
    } catch (err) {
      alert('장소 정보를 저장하는데 문제가 발생했습니다.');
    }
  };

  const handleCancelEdit = () => {
    setEditingPlace(null);
    setEditData({ memo: '', visitTime: '' });
  };

  const handleInputChange = (field, value) => {
    setEditData(prev => ({
      ...prev,
      [field]: value
    }));
  };
  
  return (
    <div className="day-card">
      <div className="day-header">
        <h4>DAY {tripDay.day} - {new Date(tripDay.date).toLocaleDateString()}</h4>
      </div>
      
      <div className="day-places">
        {loading ? (
          <p className="loading-places">장소를 불러오는 중...</p>
        ) : tripPlaces.length > 0 ? (
          <div>
            <p className="drag-instruction">
              드래그해서 순서를 변경하세요
            </p>
            
            <DragDropContext onDragEnd={handleDragEnd}>
              <Droppable droppableId="places">
                {(provided) => (
                  <div 
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className="places-container"
                  >
                    {tripPlaces
                      .sort((a, b) => a.visitOrder - b.visitOrder)
                      .map((place, index) => (
                        <Draggable 
                          key={place.id} 
                          draggableId={place.id.toString()} 
                          index={index}
                        >
                          {(provided, snapshot) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              className={`place-card ${snapshot.isDragging ? 'dragging' : ''}`}
                            >
                              <div className="place-header">
                                <div className="place-number">#{index + 1}</div>
                                <div className="place-time">{place.visitTime}</div>
                                <div className="place-actions">
                                  <button 
                                    className="edit-place-button"
                                    onClick={() => handleEditPlace(place)}
                                    title="장소 정보 수정"
                                  >
                                    장소 수정
                                  </button>
                                  <button 
                                    className="delete-place-button"
                                    onClick={() => handleDeletePlace(place.id)}
                                    title="장소 삭제"
                                  >
                                    장소 삭제
                                  </button>
                                </div>
                              </div>
                              
                              <div className="place-info">
                                <div className="place-name">{place.placeName}</div>
                                <div className="place-address">{place.address}</div>
                                
                                {editingPlace === place.id ? (
                                  <div className="place-edit-form">
                                    <div className="edit-field">
                                      <label htmlFor={`visitTime-${place.id}`}>방문 시간</label>
                                      <input
                                        type="time"
                                        id={`visitTime-${place.id}`}
                                        value={editData.visitTime}
                                        onChange={(e) => handleInputChange('visitTime', e.target.value)}
                                        className="time-input"
                                      />
                                    </div>
                                    <div className="edit-field">
                                      <label htmlFor={`memo-${place.id}`}>메모</label>
                                      <textarea
                                        id={`memo-${place.id}`}
                                        value={editData.memo}
                                        onChange={(e) => handleInputChange('memo', e.target.value)}
                                        placeholder="메모를 입력하세요..."
                                        className="memo-textarea"
                                        rows="3"
                                      />
                                    </div>
                                    <div className="edit-actions">
                                      <button 
                                        className="save-button"
                                        onClick={() => handleSavePlace(place.id)}
                                      >
                                        저장
                                      </button>
                                      <button 
                                        className="cancel-button"
                                        onClick={handleCancelEdit}
                                      >
                                        취소
                                      </button>
                                    </div>
                                  </div>
                                ) : (
                                  place.memo && (
                                    <div className="place-memo">
                                      💭 {place.memo}
                                    </div>
                                  )
                                )}
                              </div>
                            </div>
                          )}
                        </Draggable>
                      ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>
            </DragDropContext>
          </div>
        ) : (
          <div className="no-places-section">
            <p className="no-places">등록된 장소가 없습니다.</p>
            <p className="no-places-hint">오른쪽 지도에서 "장소 추가" 버튼을 사용해 장소를 추가해보세요!</p>
            <div className="visual-hint">
              <span className="hint-arrow">👆</span>
              <span className="hint-text">지도 하단의 "장소 추가" 버튼을 클릭하세요</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TripDayItem; 
