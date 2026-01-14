import React, { useState, useEffect, useRef, useCallback } from 'react';
import chatWebSocketService from '../utils/chatWebSocket';
import chatApi from '../api/chatApi';
import './ChatWindow.css';

/**
 * 채팅 창 컴포넌트
 * @param {number} tripId - 여행 ID
 * @param {Object} currentUser - 현재 사용자 정보 { id, nickname, email }
 */
const ChatWindow = ({ tripId, currentUser }) => {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);
  const [chatRoomId, setChatRoomId] = useState(null);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);

  // 스크롤을 최하단으로
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  // 채팅방 정보 가져오기
  const loadChatRoom = useCallback(async () => {
    try {
      const response = await chatApi.getChatRoom(tripId);
      const roomId = response.data.id;
      setChatRoomId(roomId);
      console.log('채팅방 정보:', response.data);
      return roomId;
    } catch (err) {
      console.error('채팅방 조회 실패:', err);
      setError('채팅방을 불러올 수 없습니다.');
      throw err;
    }
  }, [tripId]);

  // 기존 메시지 불러오기
  const loadMessages = useCallback(async (roomId) => {
    try {
      setLoading(true);
      const response = await chatApi.getMessages(roomId, 0, 50);
      // 오래된 메시지가 위로 오도록 역순 정렬
      const messagesData = response.data.content || [];
      setMessages(messagesData.reverse());
      console.log(`메시지 ${messagesData.length}개 로드 완료`);
    } catch (err) {
      console.error('메시지 불러오기 실패:', err);
      setError('메시지를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  // WebSocket 연결 및 구독
  useEffect(() => {
    let roomId = null;

    const initializeChat = async () => {
      try {
        // 1. 채팅방 정보 가져오기
        roomId = await loadChatRoom();

        // 2. WebSocket 연결
        chatWebSocketService.connect(
          () => {
            console.log('채팅 연결 성공');
            setConnected(true);
            setError(null);

            // 3. 채팅방 구독
            chatWebSocketService.subscribe(roomId, (message) => {
              console.log('새 메시지 수신:', message);
              setMessages((prev) => [...prev, message]);
            });

            // 4. 입장 알림
            chatWebSocketService.addUser(roomId, currentUser.nickname);
          },
          (error) => {
            console.error('채팅 연결 실패:', error);
            setConnected(false);
            setError('채팅 서버에 연결할 수 없습니다.');
          }
        );

        // 5. 기존 메시지 불러오기
        await loadMessages(roomId);
      } catch (err) {
        console.error('채팅 초기화 실패:', err);
      }
    };

    initializeChat();

    // 컴포넌트 언마운트 시 정리
    return () => {
      if (roomId) {
        chatWebSocketService.unsubscribe(roomId);
      }
    };
  }, [tripId, currentUser, loadChatRoom, loadMessages]);

  // 메시지 전송
  const handleSend = (e) => {
    e.preventDefault();
    
    if (!inputMessage.trim()) {
      return;
    }

    if (!connected || !chatRoomId) {
      alert('채팅 서버에 연결되지 않았습니다.');
      return;
    }

    try {
      chatWebSocketService.sendMessage(chatRoomId, 'TEXT', inputMessage);
      setInputMessage('');
    } catch (error) {
      console.error('메시지 전송 실패:', error);
      alert('메시지 전송에 실패했습니다.');
    }
  };

  // Enter 키로 전송
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend(e);
    }
  };

  // 메시지 시간 포맷
  const formatTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // 날짜 구분선 표시 여부 확인
  const shouldShowDateDivider = (currentMsg, previousMsg) => {
    if (!previousMsg) return true;
    
    const currentDate = new Date(currentMsg.createdAt).toLocaleDateString();
    const previousDate = new Date(previousMsg.createdAt).toLocaleDateString();
    
    return currentDate !== previousDate;
  };

  if (loading) {
    return (
      <div className="chat-window">
        <div className="chat-loading">
          <div className="loading-spinner"></div>
          <p>채팅을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-window">
      <div className="chat-header">
        <h3>💬 채팅</h3>
        <div className="chat-status">
          <span className={connected ? 'status-connected' : 'status-disconnected'}>
            {connected ? '● 연결됨' : '○ 연결 끊김'}
          </span>
        </div>
      </div>

      {error && (
        <div className="chat-error">
          <span>⚠️ {error}</span>
        </div>
      )}

      <div className="chat-messages" ref={messagesContainerRef}>
        {messages.length === 0 ? (
          <div className="chat-empty">
            <p>💬 첫 메시지를 보내보세요!</p>
          </div>
        ) : (
          messages.map((msg, index) => (
            <React.Fragment key={msg.id || index}>
              {/* 날짜 구분선 */}
              {shouldShowDateDivider(msg, messages[index - 1]) && (
                <div className="date-divider">
                  {new Date(msg.createdAt).toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </div>
              )}

              {/* 메시지 */}
              <div
                className={`message ${
                  msg.messageType === 'SYSTEM'
                    ? 'system-message-wrapper'
                    : msg.senderId === currentUser.id
                    ? 'my-message'
                    : 'other-message'
                }`}
              >
                {msg.messageType === 'SYSTEM' ? (
                  <div className="system-message">{msg.content}</div>
                ) : (
                  <>
                    {msg.senderId !== currentUser.id && (
                      <div className="message-sender">{msg.senderNickname}</div>
                    )}
                    <div className="message-bubble-wrapper">
                      <div className="message-content">
                        {msg.messageType === 'IMAGE' && msg.imageUrl && (
                          <img
                            src={msg.imageUrl}
                            alt="첨부 이미지"
                            className="message-image"
                          />
                        )}
                        {msg.content}
                      </div>
                      <div className="message-time">{formatTime(msg.createdAt)}</div>
                    </div>
                  </>
                )}
              </div>
            </React.Fragment>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input-form" onSubmit={handleSend}>
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="메시지를 입력하세요..."
          disabled={!connected}
          className="chat-input"
        />
        <button
          type="submit"
          disabled={!connected || !inputMessage.trim()}
          className="chat-send-button"
        >
          전송
        </button>
      </form>
    </div>
  );
};

export default ChatWindow;

