import React, { useState, useEffect } from 'react';
import { tripShareApi } from '../api/api';
import './TripShareModal.css';

const TripShareModal = ({ tripId, tripTitle, onClose }) => {
  const [shareData, setShareData] = useState({
    isPublic: false,
    expiresAt: ''
  });
  const [shareInfo, setShareInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  // 기존 공유 정보 확인
  useEffect(() => {
    checkExistingShare();
  }, [tripId]);

  const checkExistingShare = async () => {
    try {
      const response = await tripShareApi.getMySharedTrips();
      const existing = response.data.find(share => share.trip.id === tripId);
      if (existing) {
        setShareInfo(existing);
        setShareData({
          isPublic: existing.isPublic,
          expiresAt: existing.expiresAt ? existing.expiresAt.split('T')[0] : ''
        });
      }
    } catch (err) {
      console.error('기존 공유 정보 확인 실패:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setShareData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleCreateOrUpdate = async () => {
    try {
      setLoading(true);
      setError(null);

      const payload = {
        isPublic: shareData.isPublic,
        expiresAt: shareData.expiresAt || null
      };

      let response;
      if (shareInfo) {
        // 수정
        response = await tripShareApi.updateTripShare(tripId, payload);
        alert('공유 설정이 수정되었습니다!');
      } else {
        // 생성
        response = await tripShareApi.createTripShare(tripId, payload);
        alert('공유 링크가 생성되었습니다!');
      }

      setShareInfo(response.data);
    } catch (err) {
      console.error('공유 링크 생성/수정 실패:', err);
      setError(err.response?.data?.message || '공유 설정에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('공유 링크를 삭제하시겠습니까?')) return;

    try {
      setLoading(true);
      await tripShareApi.deleteTripShare(tripId);
      setShareInfo(null);
      setShareData({ isPublic: false, expiresAt: '' });
      alert('공유 링크가 삭제되었습니다.');
    } catch (err) {
      console.error('공유 링크 삭제 실패:', err);
      setError(err.response?.data?.message || '공유 링크 삭제에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyLink = () => {
    if (!shareInfo) return;

    const shareUrl = `${window.location.origin}/shared/${shareInfo.shareToken}`;
    navigator.clipboard.writeText(shareUrl).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const shareUrl = shareInfo 
    ? `${window.location.origin}/shared/${shareInfo.shareToken}`
    : '';

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>여행 공유</h2>
          <button className="close-button" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="trip-info">
            <h3>{tripTitle}</h3>
          </div>

          {error && (
            <div className="error-banner">
              <span className="error-icon">⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {shareInfo && (
            <div className="share-link-section">
              <label>공유 링크</label>
              <div className="link-box">
                <input 
                  type="text" 
                  value={shareUrl} 
                  readOnly 
                  className="share-link-input"
                />
                <button 
                  onClick={handleCopyLink}
                  className={`copy-button ${copied ? 'copied' : ''}`}
                >
                  {copied ? '✓ 복사됨' : '복사'}
                </button>
              </div>
              <div className="share-stats">
                <span>조회수: {shareInfo.viewCount}회</span>
                <span>생성일: {new Date(shareInfo.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          )}

          <div className="form-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isPublic"
                checked={shareData.isPublic}
                onChange={handleInputChange}
              />
              <span>공개 여행으로 설정 (다른 사용자들이 볼 수 있습니다)</span>
            </label>
          </div>

          <div className="form-group">
            <label>만료일 (선택사항)</label>
            <input
              type="date"
              name="expiresAt"
              value={shareData.expiresAt}
              onChange={handleInputChange}
              min={new Date().toISOString().split('T')[0]}
              className="date-input"
            />
            <small>만료일을 설정하지 않으면 영구적으로 공유됩니다.</small>
          </div>
        </div>

        <div className="modal-footer">
          {shareInfo && (
            <button 
              onClick={handleDelete}
              className="delete-share-button"
              disabled={loading}
            >
              공유 삭제
            </button>
          )}
          <div className="footer-actions">
            <button 
              onClick={onClose}
              className="cancel-button"
            >
              닫기
            </button>
            <button 
              onClick={handleCreateOrUpdate}
              className="save-button"
              disabled={loading}
            >
              {loading ? '처리 중...' : (shareInfo ? '수정' : '생성')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TripShareModal;
