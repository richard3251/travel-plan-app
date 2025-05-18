import React, { useState } from 'react';
import { placeApi } from '../api/api';
import './PlaceSearchModal.css';

const PlaceSearchModal = ({ onClose, onSelectPlace }) => {
  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!keyword.trim()) {
      alert('검색어를 입력해주세요.');
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      
      // 서울 시청 좌표를 기본값으로 사용 (lat: 37.5662952, lng: 126.9779451)
      const response = await placeApi.searchPlaces(keyword, 37.5662952, 126.9779451);
      setSearchResults(response.data);
      
      setLoading(false);
    } catch (err) {
      console.error('장소 검색 실패:', err);
      setError('장소를 검색하는데 문제가 발생했습니다.');
      setLoading(false);
    }
  };
  
  const handleSelectPlace = (place) => {
    onSelectPlace(place);
  };
  
  return (
    <div className="modal-overlay">
      <div className="place-search-modal">
        <div className="modal-header">
          <h3>장소 검색</h3>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="검색어를 입력하세요 (예: 경복궁, 명동 맛집, 제주도 관광지)"
          />
          <button type="submit" disabled={loading}>
            {loading ? '검색 중...' : '검색'}
          </button>
        </form>
        
        {error && <div className="search-error">{error}</div>}
        
        <div className="search-results">
          {loading ? (
            <div className="loading-results">검색 결과를 불러오는 중...</div>
          ) : searchResults.length > 0 ? (
            <ul className="results-list">
              {searchResults.map((place) => (
                <li key={place.id} className="result-item">
                  <div className="result-info">
                    <div className="result-name">{place.placeName}</div>
                    <div className="result-address">{place.address}</div>
                  </div>
                  <button 
                    className="select-place-button"
                    onClick={() => handleSelectPlace(place)}
                  >
                    선택
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <div className="no-results">
              {keyword ? '검색 결과가 없습니다.' : '장소를 검색해보세요!'}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PlaceSearchModal; 