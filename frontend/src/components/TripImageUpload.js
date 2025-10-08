import React, { useState, useEffect } from 'react';
import useFileUpload from '../hooks/useFileUpload';
import { fileApi } from '../api/api';
import './TripImageUpload.css';

/**
 * 여행 이미지 업로드 및 관리 컴포넌트
 */
const TripImageUpload = ({ tripId, onImagesChange }) => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { uploading, progress, uploadTripImage, reset } = useFileUpload();

  // 여행 이미지 목록 로드
  const loadTripImages = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fileApi.getTripImages(tripId);
      const imageList = response.data || [];
      
      setImages(imageList);
      onImagesChange?.(imageList);
      
    } catch (err) {
      console.error('여행 이미지 로드 오류:', err);
      setError('이미지를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 이미지 로드
  useEffect(() => {
    if (tripId) {
      loadTripImages();
    }
  }, [tripId]);

  // 파일 선택 핸들러
  const handleFileSelect = async (event) => {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    for (const file of files) {
      try {
        const result = await uploadTripImage(file, tripId, {
          displayOrder: images.length + 1
        });

        if (result.success) {
          // 업로드 성공 시 이미지 목록 새로고침
          await loadTripImages();
        }
      } catch (err) {
        console.error('이미지 업로드 오류:', err);
        setError(`${file.name} 업로드에 실패했습니다.`);
      }
    }

    // 파일 입력 초기화
    event.target.value = '';
  };

  // 커버 이미지 설정
  const handleSetCoverImage = async (imageId) => {
    try {
      // 기존 커버 이미지 해제 후 새로운 커버 이미지 설정
      // 백엔드에서 자동으로 처리되므로 여기서는 단순히 새로운 커버 이미지 업로드
      const image = images.find(img => img.id === imageId);
      if (!image) return;

      // 새로운 커버 이미지로 업로드 (기존 이미지를 커버로 변경하는 API가 필요할 수 있음)
      // 현재는 이미지 목록을 새로고침하여 변경사항 반영
      await loadTripImages();
      
    } catch (err) {
      console.error('커버 이미지 설정 오류:', err);
      setError('커버 이미지 설정에 실패했습니다.');
    }
  };

  // 이미지 삭제
  const handleDeleteImage = async (imageId) => {
    if (!window.confirm('이 이미지를 삭제하시겠습니까?')) return;

    try {
      await fileApi.deleteTripImage(imageId);
      await loadTripImages(); // 목록 새로고침
    } catch (err) {
      console.error('이미지 삭제 오류:', err);
      setError('이미지 삭제에 실패했습니다.');
    }
  };

  // 이미지 순서 변경
  const handleReorderImage = async (imageId, newOrder) => {
    try {
      await fileApi.updateTripImageOrder(imageId, newOrder);
      await loadTripImages(); // 목록 새로고침
    } catch (err) {
      console.error('이미지 순서 변경 오류:', err);
      setError('이미지 순서 변경에 실패했습니다.');
    }
  };

  // 에러 초기화
  const clearError = () => {
    setError(null);
    reset();
  };

  if (loading) {
    return (
      <div className="trip-image-upload">
        <div className="loading">이미지를 불러오는 중...</div>
      </div>
    );
  }

  return (
    <div className="trip-image-upload">
      <div className="upload-header">
        <h3>여행 이미지</h3>
        <div className="image-count">
          {images.length}개의 이미지
        </div>
      </div>

      {/* 파일 업로드 영역 */}
      <div className="upload-section">
        <input
          type="file"
          id="trip-image-input"
          accept="image/*"
          multiple
          onChange={handleFileSelect}
          disabled={uploading}
          style={{ display: 'none' }}
        />
        
        <label 
          htmlFor="trip-image-input" 
          className={`upload-button ${uploading ? 'uploading' : ''}`}
        >
          {uploading ? (
            <div className="upload-progress">
              <div className="progress-bar">
                <div 
                  className="progress-fill" 
                  style={{ width: `${progress}%` }}
                />
              </div>
              <span>{progress}% 업로드 중...</span>
            </div>
          ) : (
            <>
              <span className="upload-icon">📷</span>
              <span>이미지 추가</span>
            </>
          )}
        </label>
      </div>

      {/* 에러 메시지 */}
      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          <span>{error}</span>
          <button onClick={clearError} className="error-close">✕</button>
        </div>
      )}

      {/* 이미지 그리드 */}
      <div className="image-grid">
        {images.map((image, index) => (
          <div key={image.id} className="image-item">
            <div className="image-container">
              <img 
                src={image.thumbnailUrl || image.s3Url} 
                alt={image.originalName}
                className="image-thumbnail"
                onError={(e) => {
                  e.target.src = image.s3Url; // 썸네일 로드 실패 시 원본 이미지 사용
                }}
              />
              
              {image.isCoverImage && (
                <div className="cover-badge">커버</div>
              )}
              
              <div className="image-overlay">
                <div className="image-actions">
                  {!image.isCoverImage && (
                    <button
                      onClick={() => handleSetCoverImage(image.id)}
                      className="action-button cover-button"
                      title="커버 이미지로 설정"
                    >
                      ⭐
                    </button>
                  )}
                  
                  <button
                    onClick={() => handleDeleteImage(image.id)}
                    className="action-button delete-button"
                    title="이미지 삭제"
                  >
                    🗑️
                  </button>
                </div>
                
                <div className="image-order">
                  {index > 0 && (
                    <button
                      onClick={() => handleReorderImage(image.id, image.displayOrder - 1)}
                      className="order-button"
                      title="앞으로 이동"
                    >
                      ⬅️
                    </button>
                  )}
                  
                  <span className="order-number">{image.displayOrder}</span>
                  
                  {index < images.length - 1 && (
                    <button
                      onClick={() => handleReorderImage(image.id, image.displayOrder + 1)}
                      className="order-button"
                      title="뒤로 이동"
                    >
                      ➡️
                    </button>
                  )}
                </div>
              </div>
            </div>
            
            <div className="image-info">
              <div className="image-name" title={image.originalName}>
                {image.originalName}
              </div>
              {image.caption && (
                <div className="image-caption">{image.caption}</div>
              )}
              <div className="image-date">
                {new Date(image.uploadedAt).toLocaleDateString()}
              </div>
            </div>
          </div>
        ))}
      </div>

      {images.length === 0 && !loading && (
        <div className="empty-state">
          <div className="empty-icon">🖼️</div>
          <div className="empty-text">
            <div>아직 업로드된 이미지가 없습니다</div>
            <div>첫 번째 이미지를 추가해보세요!</div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TripImageUpload;
