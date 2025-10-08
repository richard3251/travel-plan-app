import React, { useState } from 'react';
import FileUpload from '../components/FileUpload';
import { fileApi } from '../api/api';
import './FileUploadDemo.css';

/**
 * 파일 업로드 기능 데모 페이지
 */
const FileUploadDemo = () => {
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [myFiles, setMyFiles] = useState([]);
  const [loading, setLoading] = useState(false);

  // 파일 업로드 성공 핸들러
  const handleUploadSuccess = (result) => {
    console.log('파일 업로드 성공:', result);
    setUploadedFiles(prev => [...prev, result]);
    loadMyFiles(); // 내 파일 목록 새로고침
  };

  // 파일 업로드 에러 핸들러
  const handleUploadError = (error) => {
    console.error('파일 업로드 에러:', error);
    alert(`업로드 실패: ${error}`);
  };

  // 내 파일 목록 로드
  const loadMyFiles = async () => {
    try {
      setLoading(true);
      const response = await fileApi.getMyFiles(0, 20);
      setMyFiles(response.data.content || []);
    } catch (err) {
      console.error('파일 목록 로드 실패:', err);
      alert('파일 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 파일 삭제
  const handleDeleteFile = async (fileId) => {
    if (!window.confirm('이 파일을 삭제하시겠습니까?')) return;

    try {
      await fileApi.deleteFile(fileId);
      setMyFiles(prev => prev.filter(file => file.id !== fileId));
      alert('파일이 삭제되었습니다.');
    } catch (err) {
      console.error('파일 삭제 실패:', err);
      alert('파일 삭제에 실패했습니다.');
    }
  };

  // 컴포넌트 마운트 시 내 파일 목록 로드
  React.useEffect(() => {
    loadMyFiles();
  }, []);

  return (
    <div className="file-upload-demo">
      <div className="demo-header">
        <h1>파일 업로드 데모</h1>
        <p>Pre-signed URL을 사용한 S3 직접 업로드 기능을 테스트해보세요.</p>
      </div>

      {/* 파일 업로드 섹션 */}
      <div className="upload-section">
        <h2>파일 업로드</h2>
        <FileUpload
          onUploadSuccess={handleUploadSuccess}
          onUploadError={handleUploadError}
          accept="image/*"
          maxSize={10 * 1024 * 1024} // 10MB
          multiple={false}
        />
      </div>

      {/* 업로드된 파일 결과 */}
      {uploadedFiles.length > 0 && (
        <div className="upload-results">
          <h3>방금 업로드된 파일</h3>
          <div className="results-list">
            {uploadedFiles.map((result, index) => (
              <div key={index} className="result-item success">
                <span className="result-icon">✅</span>
                <span className="result-text">
                  파일 ID: {result.fileId} - 업로드 성공!
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 내 파일 목록 */}
      <div className="my-files-section">
        <div className="section-header">
          <h2>내 파일 목록</h2>
          <button 
            onClick={loadMyFiles} 
            className="refresh-button"
            disabled={loading}
          >
            {loading ? '로딩 중...' : '새로고침'}
          </button>
        </div>

        {loading ? (
          <div className="loading">파일 목록을 불러오는 중...</div>
        ) : myFiles.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📁</div>
            <div className="empty-text">업로드된 파일이 없습니다.</div>
          </div>
        ) : (
          <div className="files-grid">
            {myFiles.map((file) => (
              <div key={file.id} className="file-card">
                <div className="file-preview">
                  {file.fileType === 'IMAGE' ? (
                    <img 
                      src={file.thumbnailUrl || file.fileUrl} 
                      alt={file.originalName}
                      className="file-thumbnail"
                      onError={(e) => {
                        e.target.src = file.fileUrl; // 썸네일 로드 실패 시 원본 이미지 사용
                      }}
                    />
                  ) : (
                    <div className="file-icon">
                      📄
                    </div>
                  )}
                </div>
                
                <div className="file-info">
                  <div className="file-name" title={file.originalName}>
                    {file.originalName}
                  </div>
                  <div className="file-details">
                    <span className="file-size">
                      {(file.fileSize / 1024 / 1024).toFixed(2)} MB
                    </span>
                    <span className="file-type">{file.fileType}</span>
                  </div>
                  <div className="file-date">
                    {new Date(file.createdAt).toLocaleDateString()}
                  </div>
                </div>
                
                <div className="file-actions">
                  <a 
                    href={file.fileUrl} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="view-button"
                  >
                    보기
                  </a>
                  <button 
                    onClick={() => handleDeleteFile(file.id)}
                    className="delete-button"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default FileUploadDemo;
