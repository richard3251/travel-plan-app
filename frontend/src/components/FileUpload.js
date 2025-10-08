import React, { useRef, useState } from 'react';
import useFileUpload from '../hooks/useFileUpload';
import './FileUpload.css';

/**
 * 범용 파일 업로드 컴포넌트
 */
const FileUpload = ({ 
  onUploadSuccess, 
  onUploadError,
  accept = "image/*",
  maxSize = 10 * 1024 * 1024, // 10MB
  multiple = false,
  disabled = false,
  className = "",
  children
}) => {
  const fileInputRef = useRef(null);
  const [dragOver, setDragOver] = useState(false);
  const { uploading, progress, error, uploadFile, reset } = useFileUpload();

  // 파일 선택 핸들러
  const handleFileSelect = async (files) => {
    if (!files || files.length === 0) return;

    const fileArray = Array.from(files);
    
    if (!multiple && fileArray.length > 1) {
      onUploadError?.('한 번에 하나의 파일만 업로드할 수 있습니다.');
      return;
    }

    // 단일 파일 업로드 (multiple이 false인 경우)
    if (!multiple) {
      const result = await uploadFile(fileArray[0]);
      if (result.success) {
        onUploadSuccess?.(result);
      } else {
        onUploadError?.(result.error);
      }
      return;
    }

    // 다중 파일 업로드 (multiple이 true인 경우)
    const results = [];
    for (const file of fileArray) {
      const result = await uploadFile(file);
      results.push(result);
      
      if (result.success) {
        onUploadSuccess?.(result);
      } else {
        onUploadError?.(result.error);
      }
    }
  };

  // 파일 입력 변경 핸들러
  const handleInputChange = (event) => {
    handleFileSelect(event.target.files);
  };

  // 드래그 앤 드롭 핸들러
  const handleDragOver = (event) => {
    event.preventDefault();
    if (!disabled && !uploading) {
      setDragOver(true);
    }
  };

  const handleDragLeave = (event) => {
    event.preventDefault();
    setDragOver(false);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setDragOver(false);
    
    if (disabled || uploading) return;
    
    const files = event.dataTransfer.files;
    handleFileSelect(files);
  };

  // 파일 선택 버튼 클릭
  const handleButtonClick = () => {
    if (!disabled && !uploading) {
      fileInputRef.current?.click();
    }
  };

  // 업로드 취소
  const handleCancel = () => {
    reset();
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className={`file-upload ${className}`}>
      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        onChange={handleInputChange}
        style={{ display: 'none' }}
        disabled={disabled || uploading}
      />

      <div
        className={`file-upload-area ${dragOver ? 'drag-over' : ''} ${uploading ? 'uploading' : ''} ${disabled ? 'disabled' : ''}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={handleButtonClick}
      >
        {uploading ? (
          <div className="upload-progress">
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{ width: `${progress}%` }}
              ></div>
            </div>
            <div className="progress-text">{progress}% 업로드 중...</div>
            <button 
              className="cancel-button"
              onClick={(e) => {
                e.stopPropagation();
                handleCancel();
              }}
            >
              취소
            </button>
          </div>
        ) : (
          <div className="upload-content">
            {children || (
              <>
                <div className="upload-icon">📁</div>
                <div className="upload-text">
                  <div className="primary-text">
                    파일을 여기에 드래그하거나 클릭하여 선택하세요
                  </div>
                  <div className="secondary-text">
                    최대 {Math.round(maxSize / (1024 * 1024))}MB까지 업로드 가능
                  </div>
                </div>
              </>
            )}
          </div>
        )}
      </div>

      {error && (
        <div className="upload-error">
          <span className="error-icon">⚠️</span>
          <span className="error-message">{error}</span>
          <button 
            className="error-close"
            onClick={() => reset()}
          >
            ✕
          </button>
        </div>
      )}
    </div>
  );
};

export default FileUpload;
