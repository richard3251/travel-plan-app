import { useState, useCallback } from 'react';
import { fileApi } from '../api/api';

/**
 * 파일 업로드를 위한 커스텀 훅
 * Pre-signed URL 방식으로 S3에 직접 업로드
 */
export const useFileUpload = () => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState(null);

  // 파일 유효성 검사
  const validateFile = useCallback((file, maxSize = 10 * 1024 * 1024) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    
    if (!file) {
      throw new Error('파일을 선택해주세요.');
    }

    if (!allowedTypes.includes(file.type)) {
      throw new Error('지원하지 않는 파일 형식입니다. (JPEG, PNG, GIF, WebP만 지원)');
    }

    if (file.size > maxSize) {
      const maxSizeMB = Math.round(maxSize / (1024 * 1024));
      throw new Error(`파일 크기가 ${maxSizeMB}MB를 초과했습니다.`);
    }

    return true;
  }, []);

  // 일반 파일 업로드
  const uploadFile = useCallback(async (file) => {
    try {
      setUploading(true);
      setProgress(0);
      setError(null);

      // 파일 유효성 검사
      validateFile(file);

      console.log('파일 업로드 시작:', file.name);

      // 1. Pre-signed URL 발급 요청
      const presignedResponse = await fileApi.getPresignedUrl(
        file.name,
        file.size,
        file.type
      );

      const { fileId, presignedUrl } = presignedResponse.data;
      console.log('Pre-signed URL 발급 완료:', fileId);

      // 2. S3에 직접 업로드
      const uploadResult = await fileApi.uploadToS3(
        presignedUrl,
        file,
        file.type,
        (progressPercent) => {
          setProgress(progressPercent);
          console.log(`업로드 진행률: ${progressPercent}%`);
        }
      );

      // 3. 업로드 완료 통지
      await fileApi.notifyUploadComplete(
        fileId,
        uploadResult.success,
        uploadResult.error
      );

      if (uploadResult.success) {
        console.log('파일 업로드 완료:', fileId);
        setProgress(100);
        return { fileId, success: true };
      } else {
        throw new Error(uploadResult.error || '파일 업로드에 실패했습니다.');
      }

    } catch (err) {
      console.error('파일 업로드 오류:', err);
      const errorMessage = err.response?.data?.message || err.message || '파일 업로드에 실패했습니다.';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setUploading(false);
    }
  }, [validateFile]);

  // 여행 이미지 업로드
  const uploadTripImage = useCallback(async (file, tripId, options = {}) => {
    try {
      setUploading(true);
      setProgress(0);
      setError(null);

      const { isCoverImage = false, caption = null, displayOrder = null } = options;

      // 파일 유효성 검사
      validateFile(file);

      console.log('여행 이미지 업로드 시작:', file.name, 'tripId:', tripId);

      // 1. Pre-signed URL 발급 요청 (여행 이미지용)
      const presignedResponse = await fileApi.getTripImagePresignedUrl(
        file.name,
        file.size,
        file.type,
        tripId,
        isCoverImage,
        caption,
        displayOrder
      );

      const { fileId, presignedUrl } = presignedResponse.data;
      console.log('여행 이미지 Pre-signed URL 발급 완료:', fileId);

      // 2. S3에 직접 업로드
      const uploadResult = await fileApi.uploadToS3(
        presignedUrl,
        file,
        file.type,
        (progressPercent) => {
          setProgress(progressPercent);
          console.log(`여행 이미지 업로드 진행률: ${progressPercent}%`);
        }
      );

      // 3. 업로드 완료 통지
      await fileApi.notifyUploadComplete(
        fileId,
        uploadResult.success,
        uploadResult.error
      );

      if (uploadResult.success) {
        console.log('여행 이미지 업로드 완료:', fileId);
        setProgress(100);
        return { fileId, success: true };
      } else {
        throw new Error(uploadResult.error || '여행 이미지 업로드에 실패했습니다.');
      }

    } catch (err) {
      console.error('여행 이미지 업로드 오류:', err);
      const errorMessage = err.response?.data?.message || err.message || '여행 이미지 업로드에 실패했습니다.';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setUploading(false);
    }
  }, [validateFile]);

  // 상태 초기화
  const reset = useCallback(() => {
    setUploading(false);
    setProgress(0);
    setError(null);
  }, []);

  return {
    uploading,
    progress,
    error,
    uploadFile,
    uploadTripImage,
    reset
  };
};

export default useFileUpload;
