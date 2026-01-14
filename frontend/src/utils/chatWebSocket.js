import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket 기반 실시간 채팅 서비스
 * STOMP 프로토콜 사용
 */
class ChatWebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscriptions = new Map();
    this.isConnecting = false;
  }

  /**
   * WebSocket 연결
   * @param {Function} onConnected - 연결 성공 콜백
   * @param {Function} onError - 에러 콜백
   */
  connect(onConnected, onError) {
    if (this.stompClient && this.stompClient.connected) {
      console.log('이미 WebSocket이 연결되어 있습니다.');
      if (onConnected) onConnected();
      return;
    }

    if (this.isConnecting) {
      console.log('WebSocket 연결 중입니다...');
      return;
    }

    this.isConnecting = true;
    
    const socket = new SockJS(process.env.REACT_APP_API_BASE_URL?.replace('/api', '') + '/ws/chat' || 'http://localhost:8080/ws/chat');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        // 프로덕션에서는 주석 처리
        console.log('STOMP:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      console.log('✅ WebSocket 연결 성공!');
      this.isConnecting = false;
      if (onConnected) onConnected();
    };

    this.stompClient.onStompError = (frame) => {
      console.error('❌ WebSocket 오류:', frame);
      this.isConnecting = false;
      if (onError) onError(frame);
    };

    this.stompClient.onWebSocketClose = () => {
      console.log('WebSocket 연결이 종료되었습니다.');
      this.isConnecting = false;
    };

    this.stompClient.activate();
  }

  /**
   * 채팅방 구독
   * @param {number} chatRoomId - 채팅방 ID
   * @param {Function} onMessageReceived - 메시지 수신 콜백
   * @returns {Object} subscription 객체
   */
  subscribe(chatRoomId, onMessageReceived) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('❌ WebSocket이 연결되지 않았습니다. 먼저 connect()를 호출하세요.');
      return null;
    }

    // 이미 구독 중이면 기존 구독 반환
    if (this.subscriptions.has(chatRoomId)) {
      console.log(`이미 채팅방 ${chatRoomId}를 구독 중입니다.`);
      return this.subscriptions.get(chatRoomId);
    }

    const subscription = this.stompClient.subscribe(
      `/topic/chat/${chatRoomId}`,
      (message) => {
        try {
          const parsedMessage = JSON.parse(message.body);
          console.log('📨 새 메시지 수신:', parsedMessage);
          onMessageReceived(parsedMessage);
        } catch (error) {
          console.error('메시지 파싱 실패:', error);
        }
      }
    );

    this.subscriptions.set(chatRoomId, subscription);
    console.log(`✅ 채팅방 ${chatRoomId} 구독 완료`);
    return subscription;
  }

  /**
   * 메시지 전송
   * @param {number} chatRoomId - 채팅방 ID
   * @param {string} messageType - 메시지 타입 (TEXT, IMAGE, LOCATION, SYSTEM)
   * @param {string} content - 메시지 내용
   * @param {string} imageUrl - 이미지 URL (선택)
   * @param {number} latitude - 위도 (선택)
   * @param {number} longitude - 경도 (선택)
   */
  sendMessage(chatRoomId, messageType, content, imageUrl = null, latitude = null, longitude = null) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('❌ WebSocket이 연결되지 않았습니다.');
      throw new Error('WebSocket이 연결되지 않았습니다.');
    }

    const message = {
      chatRoomId,
      messageType,
      content,
      imageUrl,
      latitude,
      longitude,
    };

    try {
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message),
      });
      console.log('📤 메시지 전송:', message);
    } catch (error) {
      console.error('메시지 전송 실패:', error);
      throw error;
    }
  }

  /**
   * 사용자 입장 알림
   * @param {number} chatRoomId - 채팅방 ID
   * @param {string} userName - 사용자 이름
   */
  addUser(chatRoomId, userName) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('❌ WebSocket이 연결되지 않았습니다.');
      return;
    }

    try {
      this.stompClient.publish({
        destination: '/app/chat.addUser',
        body: JSON.stringify({
          chatRoomId,
          messageType: 'SYSTEM',
          content: `${userName}님이 입장했습니다.`,
        }),
      });
      console.log(`👋 ${userName}님 입장 알림 전송`);
    } catch (error) {
      console.error('입장 알림 전송 실패:', error);
    }
  }

  /**
   * 타이핑 중 알림 (선택적 기능)
   * @param {number} chatRoomId - 채팅방 ID
   * @param {string} userName - 사용자 이름
   */
  sendTyping(chatRoomId, userName) {
    if (!this.stompClient || !this.stompClient.connected) {
      return;
    }

    try {
      this.stompClient.publish({
        destination: `/app/chat.typing/${chatRoomId}`,
        body: JSON.stringify({ userName }),
      });
    } catch (error) {
      console.error('타이핑 알림 전송 실패:', error);
    }
  }

  /**
   * 특정 채팅방 구독 해제
   * @param {number} chatRoomId - 채팅방 ID
   */
  unsubscribe(chatRoomId) {
    const subscription = this.subscriptions.get(chatRoomId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(chatRoomId);
      console.log(`✅ 채팅방 ${chatRoomId} 구독 해제`);
    }
  }

  /**
   * 모든 구독 해제
   */
  unsubscribeAll() {
    this.subscriptions.forEach((subscription, chatRoomId) => {
      subscription.unsubscribe();
      console.log(`✅ 채팅방 ${chatRoomId} 구독 해제`);
    });
    this.subscriptions.clear();
  }

  /**
   * WebSocket 연결 종료
   */
  disconnect() {
    if (this.stompClient) {
      this.unsubscribeAll();
      this.stompClient.deactivate();
      this.stompClient = null;
      this.isConnecting = false;
      console.log('✅ WebSocket 연결 종료');
    }
  }

  /**
   * 연결 상태 확인
   * @returns {boolean} 연결 여부
   */
  isConnected() {
    return this.stompClient && this.stompClient.connected;
  }
}

// 싱글톤 인스턴스 생성
const chatWebSocketService = new ChatWebSocketService();

export default chatWebSocketService;

