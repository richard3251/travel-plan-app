import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

/**
 * 채팅 관련 REST API
 */
const chatApi = {
  /**
   * 채팅방 생성 또는 조회
   * @param {number} tripId - 여행 ID
   * @returns {Promise} 채팅방 정보
   */
  getChatRoom: (tripId) => {
    return axios.get(`${API_BASE_URL}/chat/rooms/trip/${tripId}`, {
      withCredentials: true,
    });
  },

  /**
   * 메시지 목록 조회 (페이징)
   * @param {number} chatRoomId - 채팅방 ID
   * @param {number} page - 페이지 번호 (0부터 시작)
   * @param {number} size - 페이지 크기
   * @returns {Promise} 메시지 목록
   */
  getMessages: (chatRoomId, page = 0, size = 50) => {
    return axios.get(`${API_BASE_URL}/chat/rooms/${chatRoomId}/messages`, {
      params: { page, size },
      withCredentials: true,
    });
  },

  /**
   * 특정 시간 이후 메시지 조회
   * @param {number} chatRoomId - 채팅방 ID
   * @param {string} since - 기준 시간 (ISO 8601 형식)
   * @returns {Promise} 메시지 목록
   */
  getMessagesSince: (chatRoomId, since) => {
    return axios.get(`${API_BASE_URL}/chat/rooms/${chatRoomId}/messages/since`, {
      params: { since },
      withCredentials: true,
    });
  },

  /**
   * 메시지 삭제
   * @param {number} messageId - 메시지 ID
   * @returns {Promise}
   */
  deleteMessage: (messageId) => {
    return axios.delete(`${API_BASE_URL}/chat/messages/${messageId}`, {
      withCredentials: true,
    });
  },

  /**
   * 읽음 표시 업데이트
   * @param {number} chatRoomId - 채팅방 ID
   * @param {number} messageId - 마지막으로 읽은 메시지 ID
   * @returns {Promise}
   */
  markAsRead: (chatRoomId, messageId) => {
    return axios.post(
      `${API_BASE_URL}/chat/rooms/${chatRoomId}/read`,
      { messageId },
      { withCredentials: true }
    );
  },
};

export default chatApi;

