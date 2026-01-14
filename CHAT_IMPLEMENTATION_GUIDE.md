# 💬 채팅 기능 구현 가이드

여행 플래너 서비스에 실시간 채팅 기능을 추가하는 완벽한 가이드입니다.

---

## 📋 목차

1. [아키텍처 설계](#아키텍처-설계)
2. [백엔드 구현](#백엔드-구현)
3. [프론트엔드 구현](#프론트엔드-구현)
4. [테스트 방법](#테스트-방법)
5. [배포 고려사항](#배포-고려사항)

---

## 🏗️ 아키텍처 설계

### 전체 흐름

```
클라이언트 (React)
    ↕ WebSocket (STOMP)
Spring Boot Server
    ↕ JPA
MySQL Database

Redis (선택사항 - 온라인 사용자 관리)
```

### 주요 구성요소

1. **ChatRoom (채팅방)**
   - 각 여행(Trip)마다 1개의 채팅방
   - 여행 공유된 사용자만 접근 가능

2. **ChatMessage (메시지)**
   - TEXT, IMAGE, LOCATION, SYSTEM 타입
   - 실시간 전송 + DB 저장

3. **WebSocket (실시간 통신)**
   - STOMP 프로토콜 사용
   - `/ws/chat` 엔드포인트

---

## 💻 백엔드 구현

### Step 1: 의존성 추가 ✅

`build.gradle`에 WebSocket 의존성이 추가되어 있습니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-websocket'
```

### Step 2: 엔티티 생성 ✅

- `ChatRoom.java` ✅
- `ChatMessage.java` ✅
- `MessageType.java` ✅

### Step 3: WebSocket 설정 ✅

- `WebSocketConfig.java` ✅

### Step 4: Repository ✅

- `ChatRoomRepository.java` ✅
- `ChatMessageRepository.java` ✅

### Step 5: Service 구현

#### ChatService.java

```java
package com.travelapp.backend.domain.chat.service;

import com.travelapp.backend.domain.chat.entity.ChatRoom;
import com.travelapp.backend.domain.chat.entity.ChatMessage;
import com.travelapp.backend.domain.chat.entity.MessageType;
import com.travelapp.backend.domain.chat.repository.ChatRoomRepository;
import com.travelapp.backend.domain.chat.repository.ChatMessageRepository;
import com.travelapp.backend.domain.member.entity.Member;
import com.travelapp.backend.domain.member.repository.MemberRepository;
import com.travelapp.backend.domain.trip.entity.Trip;
import com.travelapp.backend.domain.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;

    /**
     * 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(Long tripId) {
        return chatRoomRepository.findByTripId(tripId)
                .orElseGet(() -> {
                    Trip trip = tripRepository.findById(tripId)
                            .orElseThrow(() -> new RuntimeException("여행을 찾을 수 없습니다"));
                    
                    ChatRoom chatRoom = ChatRoom.builder()
                            .trip(trip)
                            .name(trip.getTitle() + " 채팅방")
                            .build();
                    
                    log.info("새 채팅방 생성 - Trip ID: {}", tripId);
                    return chatRoomRepository.save(chatRoom);
                });
    }

    /**
     * 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(Long chatRoomId, Long senderId, MessageType messageType, 
                                     String content, String imageUrl, Double latitude, Double longitude) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
        
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(messageType)
                .content(content)
                .imageUrl(imageUrl)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // 채팅방의 마지막 메시지 시간 업데이트
        chatRoom.updateLastMessageTime();
        chatRoomRepository.save(chatRoom);
        
        log.info("메시지 저장 완료 - Room: {}, Sender: {}", chatRoomId, senderId);
        return savedMessage;
    }

    /**
     * 메시지 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessages(Long chatRoomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatMessageRepository.findByChatRoomId(chatRoomId, pageRequest);
    }
}
```

### Step 6: WebSocket Controller (핵심!)

#### ChatController.java

```java
package com.travelapp.backend.domain.chat.controller;

import com.travelapp.backend.domain.chat.dto.request.ChatMessageRequest;
import com.travelapp.backend.domain.chat.dto.response.ChatMessageResponse;
import com.travelapp.backend.domain.chat.entity.ChatMessage;
import com.travelapp.backend.domain.chat.service.ChatService;
import com.travelapp.backend.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅 WebSocket 컨트롤러
 * 
 * 클라이언트 → 서버: /app/chat.sendMessage
 * 서버 → 클라이언트들: /topic/chat/{chatRoomId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 메시지 전송
     * 
     * 클라이언트가 /app/chat.sendMessage로 메시지 전송
     * → 서버가 처리 후 /topic/chat/{chatRoomId}로 브로드캐스트
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessageResponse sendMessage(ChatMessageRequest request, 
                                            SimpMessageHeaderAccessor headerAccessor) {
        // 현재 사용자 ID 가져오기 (JWT에서)
        Long currentUserId = SecurityUtil.getCurrentMemberId();
        
        log.info("메시지 수신 - Room: {}, User: {}, Content: {}", 
                request.getChatRoomId(), currentUserId, request.getContent());
        
        // 메시지 저장
        ChatMessage savedMessage = chatService.saveMessage(
                request.getChatRoomId(),
                currentUserId,
                request.getMessageType(),
                request.getContent(),
                request.getImageUrl(),
                request.getLatitude(),
                request.getLongitude()
        );
        
        // 응답 생성
        return ChatMessageResponse.of(savedMessage);
    }

    /**
     * 사용자 입장 알림
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessageResponse addUser(ChatMessageRequest request,
                                         SimpMessageHeaderAccessor headerAccessor) {
        Long currentUserId = SecurityUtil.getCurrentMemberId();
        
        log.info("사용자 입장 - Room: {}, User: {}", request.getChatRoomId(), currentUserId);
        
        // 시스템 메시지 생성
        ChatMessage systemMessage = chatService.saveMessage(
                request.getChatRoomId(),
                currentUserId,
                com.travelapp.backend.domain.chat.entity.MessageType.SYSTEM,
                "님이 입장했습니다.",
                null, null, null
        );
        
        return ChatMessageResponse.of(systemMessage);
    }
}
```

### Step 7: REST API Controller (메시지 조회용)

#### ChatRestController.java

```java
package com.travelapp.backend.domain.chat.controller;

import com.travelapp.backend.domain.chat.dto.response.ChatMessageResponse;
import com.travelapp.backend.domain.chat.dto.response.ChatRoomResponse;
import com.travelapp.backend.domain.chat.entity.ChatMessage;
import com.travelapp.backend.domain.chat.entity.ChatRoom;
import com.travelapp.backend.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅", description = "채팅 관련 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @Operation(summary = "채팅방 생성/조회", description = "여행 ID로 채팅방을 생성하거나 조회합니다")
    @GetMapping("/rooms/trip/{tripId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long tripId) {
        ChatRoom chatRoom = chatService.getOrCreateChatRoom(tripId);
        return ResponseEntity.ok(ChatRoomResponse.of(chatRoom));
    }

    @Operation(summary = "메시지 목록 조회", description = "채팅방의 메시지 목록을 조회합니다 (페이징)")
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessage> messages = chatService.getMessages(chatRoomId, page, size);
        Page<ChatMessageResponse> response = messages.map(ChatMessageResponse::of);
        return ResponseEntity.ok(response);
    }
}
```

---

## 🎨 프론트엔드 구현

### Step 1: 라이브러리 설치

```bash
cd frontend
npm install sockjs-client @stomp/stompjs
```

### Step 2: WebSocket 유틸리티 생성

#### `frontend/src/utils/chatWebSocket.js`

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class ChatWebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscriptions = new Map();
  }

  // WebSocket 연결
  connect(onConnected, onError) {
    const socket = new SockJS('http://localhost:8080/ws/chat');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      console.log('WebSocket 연결 성공!');
      if (onConnected) onConnected();
    };

    this.stompClient.onStompError = (frame) => {
      console.error('WebSocket 오류:', frame);
      if (onError) onError(frame);
    };

    this.stompClient.activate();
  }

  // 채팅방 구독
  subscribe(chatRoomId, onMessageReceived) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket이 연결되지 않았습니다');
      return null;
    }

    const subscription = this.stompClient.subscribe(
      `/topic/chat/${chatRoomId}`,
      (message) => {
        const parsedMessage = JSON.parse(message.body);
        onMessageReceived(parsedMessage);
      }
    );

    this.subscriptions.set(chatRoomId, subscription);
    return subscription;
  }

  // 메시지 전송
  sendMessage(chatRoomId, messageType, content, imageUrl = null, latitude = null, longitude = null) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket이 연결되지 않았습니다');
      return;
    }

    const message = {
      chatRoomId,
      messageType,
      content,
      imageUrl,
      latitude,
      longitude,
    };

    this.stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message),
    });
  }

  // 사용자 입장 알림
  addUser(chatRoomId, userName) {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket이 연결되지 않았습니다');
      return;
    }

    this.stompClient.publish({
      destination: '/app/chat.addUser',
      body: JSON.stringify({
        chatRoomId,
        messageType: 'SYSTEM',
        content: `${userName}`,
      }),
    });
  }

  // 구독 해제
  unsubscribe(chatRoomId) {
    const subscription = this.subscriptions.get(chatRoomId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(chatRoomId);
    }
  }

  // 연결 종료
  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.subscriptions.clear();
    }
  }
}

export default new ChatWebSocketService();
```

### Step 3: 채팅 컴포넌트 생성

#### `frontend/src/components/ChatWindow.js`

```javascript
import React, { useState, useEffect, useRef } from 'react';
import chatWebSocketService from '../utils/chatWebSocket';
import axios from 'axios';
import './ChatWindow.css';

const ChatWindow = ({ tripId, chatRoomId, currentUser }) => {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const messagesEndRef = useRef(null);

  // 스크롤을 최하단으로
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // WebSocket 연결 및 구독
  useEffect(() => {
    // WebSocket 연결
    chatWebSocketService.connect(
      () => {
        console.log('채팅 연결 성공');
        setConnected(true);
        
        // 채팅방 구독
        chatWebSocketService.subscribe(chatRoomId, (message) => {
          console.log('새 메시지:', message);
          setMessages((prev) => [...prev, message]);
        });

        // 입장 알림
        chatWebSocketService.addUser(chatRoomId, currentUser.nickname);
      },
      (error) => {
        console.error('채팅 연결 실패:', error);
        setConnected(false);
      }
    );

    // 기존 메시지 불러오기
    loadMessages();

    // 컴포넌트 언마운트 시 정리
    return () => {
      chatWebSocketService.unsubscribe(chatRoomId);
    };
  }, [chatRoomId]);

  // 기존 메시지 불러오기
  const loadMessages = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/chat/rooms/${chatRoomId}/messages?page=0&size=50`,
        { withCredentials: true }
      );
      // 오래된 메시지가 위로 오도록 역순 정렬
      setMessages(response.data.content.reverse());
    } catch (error) {
      console.error('메시지 불러오기 실패:', error);
    }
  };

  // 메시지 전송
  const handleSend = (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || !connected) return;

    chatWebSocketService.sendMessage(chatRoomId, 'TEXT', inputMessage);
    setInputMessage('');
  };

  return (
    <div className="chat-window">
      <div className="chat-header">
        <h3>채팅</h3>
        <span className={connected ? 'status-connected' : 'status-disconnected'}>
          {connected ? '● 연결됨' : '○ 연결 끊김'}
        </span>
      </div>

      <div className="chat-messages">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`message ${msg.senderId === currentUser.id ? 'my-message' : 'other-message'}`}
          >
            {msg.messageType === 'SYSTEM' ? (
              <div className="system-message">{msg.content}</div>
            ) : (
              <>
                <div className="message-sender">{msg.senderNickname}</div>
                <div className="message-content">{msg.content}</div>
                <div className="message-time">
                  {new Date(msg.createdAt).toLocaleTimeString('ko-KR', {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </div>
              </>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input-form" onSubmit={handleSend}>
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          placeholder="메시지를 입력하세요..."
          disabled={!connected}
        />
        <button type="submit" disabled={!connected || !inputMessage.trim()}>
          전송
        </button>
      </form>
    </div>
  );
};

export default ChatWindow;
```

### Step 4: CSS 스타일

#### `frontend/src/components/ChatWindow.css`

```css
.chat-window {
  display: flex;
  flex-direction: column;
  height: 600px;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #4a90e2;
  color: white;
}

.status-connected {
  color: #4caf50;
  font-weight: bold;
}

.status-disconnected {
  color: #f44336;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #f5f5f5;
}

.message {
  margin-bottom: 15px;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.my-message {
  text-align: right;
}

.other-message {
  text-align: left;
}

.message-sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.message-content {
  display: inline-block;
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.my-message .message-content {
  background: #4a90e2;
  color: white;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 5px;
}

.system-message {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 5px;
}

.chat-input-form {
  display: flex;
  padding: 15px;
  border-top: 1px solid #ddd;
  background: white;
}

.chat-input-form input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
}

.chat-input-form button {
  margin-left: 10px;
  padding: 10px 20px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.chat-input-form button:hover {
  background: #357abd;
}

.chat-input-form button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
```

---

## 🧪 테스트 방법

### 1. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

### 2. 프론트엔드 실행

```bash
cd frontend
npm install sockjs-client @stomp/stompjs
npm start
```

### 3. 채팅 테스트

1. 두 개의 브라우저 창을 엽니다
2. 각각 다른 계정으로 로그인
3. 같은 여행 상세 페이지로 이동
4. 채팅창에서 메시지 전송
5. 실시간으로 메시지가 양쪽에 표시되는지 확인

---

## 🚀 다음 단계 (선택사항)

1. ✅ **읽음 표시** - 누가 메시지를 읽었는지 표시
2. ✅ **이미지 전송** - 파일 업로드 기능 연동
3. ✅ **위치 공유** - 카카오 지도와 연동
4. ✅ **알림** - 새 메시지 푸시 알림
5. ✅ **온라인 상태** - 접속 중인 사용자 표시
6. ✅ **이모지** - 이모지 선택기 추가
7. ✅ **답장 기능** - 특정 메시지에 답장

---

## 📌 주의사항

1. **보안**: JWT 토큰을 WebSocket 헤더에 포함
2. **성능**: 메시지 페이징 필수 (무한 스크롤)
3. **확장성**: Redis Pub/Sub으로 다중 서버 지원
4. **에러 처리**: 연결 끊김 시 자동 재연결

---

## 💡 자주 묻는 질문 (FAQ)

**Q: WebSocket 연결이 안 돼요!**
A: CORS 설정과 포트 번호를 확인하세요. SecurityConfig에서 WebSocket 경로를 허용해야 합니다.

**Q: 메시지가 두 번 전송돼요!**
A: React StrictMode 때문일 수 있습니다. 프로덕션에서는 발생하지 않습니다.

**Q: 배포 시 WebSocket이 안 돼요!**
A: Nginx 설정에서 WebSocket 업그레이드를 허용해야 합니다.

---

이 가이드대로 구현하면 카카오톡과 유사한 실시간 채팅 기능을 만들 수 있습니다! 🎉

