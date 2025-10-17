# 🌍 Travel Plan App - 여행 계획 애플리케이션

사용자가 여행 일정을 계획하고 장소를 관리하며, 여행 경로를 지도에서 시각화할 수 있는 웹 애플리케이션입니다.

---

## ✨ 주요 기능

### 🗺️ **여행 계획 관리**
- 여행 생성, 수정, 삭제
- 날짜별 일정 관리
- 장소 추가 및 순서 변경 (Drag & Drop)
- 카카오 지도 API 연동으로 장소 검색 및 표시
- 실시간 경로 시각화 (Polyline)

### 📸 **이미지 관리**
- AWS S3 직접 업로드 (Pre-signed URL)
- 자동 썸네일 생성
- 여행 커버 이미지 설정
- 이미지 갤러리 및 순서 관리

### 🔗 **여행 공유**
- 공유 링크 생성 및 관리
- 공개/비공개 설정
- 만료일 설정
- 조회수 추적

### 🔐 **인증 및 보안**
- JWT 기반 인증 (HTTP-only Cookie)
- 회원가입/로그인
- 리프레시 토큰 자동 갱신
- Redis 기반 세션 관리

---

## 🏗️ 기술 스택

### **백엔드**
- **Framework**: Spring Boot 3.4.4
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **ORM**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **API Docs**: Swagger UI (OpenAPI 3.0)
- **File Storage**: AWS S3
- **Build Tool**: Gradle

### **프론트엔드**
- **Framework**: React 18
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Map**: Kakao Maps SDK
- **Drag & Drop**: @hello-pangea/dnd
- **Build Tool**: Create React App

### **DevOps**
- **Containerization**: Docker, Docker Compose
- **Web Server**: Nginx (프로덕션)

---

## 📋 시스템 요구사항

### **개발 환경**
- **JDK**: 21 이상
- **Node.js**: 18 이상
- **Docker**: 20.10 이상 (선택사항)
- **MySQL**: 8.0 이상
- **Redis**: 7.0 이상

### **필수 API 키**
- Kakao REST API Key
- Kakao JavaScript Key (지도용)
- AWS Access Key (S3 업로드 사용시)

---

## 🚀 빠른 시작

### **1. 프로젝트 클론**

```bash
git clone https://github.com/yourusername/travel-plan-app.git
cd travel-plan-app
```

### **2. 환경 변수 설정**

#### 백엔드 환경 변수
```bash
# ENV_EXAMPLE.txt를 backend/.env로 복사
cp ENV_EXAMPLE.txt backend/.env

# backend/.env 파일을 열어 값 설정
nano backend/.env
```

필수 설정:
- `SPRING_DATASOURCE_PASSWORD`: DB 비밀번호
- `JWT_SECRET`: JWT 시크릿 키 (강력한 랜덤 문자열)
- `KAKAO_REST_API_KEY`: 카카오 REST API 키
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`: AWS S3 자격증명

#### 프론트엔드 환경 변수
```bash
# frontend/ENV_EXAMPLE_FRONTEND.txt를 frontend/.env로 복사
cp frontend/ENV_EXAMPLE_FRONTEND.txt frontend/.env

# frontend/.env 파일 수정
nano frontend/.env
```

### **3-A. Docker로 실행 (권장)**

```bash
# Docker Compose로 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 접속
# - 프론트엔드: http://localhost:3000
# - 백엔드 API: http://localhost:8080/api
# - Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### **3-B. 로컬 개발 환경으로 실행**

#### MySQL 및 Redis 설치 및 실행
```bash
# MySQL 실행
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE travel_app;

# Redis 실행
redis-server
```

#### 백엔드 실행
```bash
cd backend

# Gradle 빌드 및 실행
./gradlew bootRun

# 또는 IntelliJ IDEA에서 BackendApplication.java 실행
```

#### 프론트엔드 실행
```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm start
```

접속: http://localhost:3000

---

## 📁 프로젝트 구조

```
travel-plan-app/
├── backend/                      # Spring Boot 백엔드
│   ├── src/main/java/com/travelapp/backend/
│   │   ├── domain/               # 도메인별 패키지
│   │   │   ├── member/           # 회원 관리
│   │   │   ├── trip/             # 여행 관리
│   │   │   ├── tripday/          # 여행 일자 관리
│   │   │   ├── tripplace/        # 여행 장소 관리
│   │   │   ├── tripshare/        # 여행 공유
│   │   │   ├── file/             # 파일 업로드
│   │   │   └── place/            # 장소 검색
│   │   ├── global/               # 전역 설정
│   │   │   ├── config/           # Spring 설정
│   │   │   ├── filter/           # JWT 필터
│   │   │   ├── exception/        # 예외 처리
│   │   │   └── util/             # 유틸리티
│   │   └── infra/                # 외부 API 연동
│   │       └── kakao/            # 카카오 API
│   ├── src/main/resources/
│   │   ├── application.yml       # 기본 설정
│   │   └── application-prod.yml  # 프로덕션 설정
│   ├── Dockerfile
│   └── build.gradle
│
├── frontend/                     # React 프론트엔드
│   ├── src/
│   │   ├── api/                  # API 클라이언트
│   │   ├── components/           # 재사용 컴포넌트
│   │   ├── contexts/             # React Context
│   │   ├── hooks/                # 커스텀 훅
│   │   └── pages/                # 페이지 컴포넌트
│   ├── public/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml            # 개발용 Docker Compose
├── docker-compose.prod.yml       # 프로덕션 Docker Compose
├── ENV_EXAMPLE.txt               # 백엔드 환경 변수 예시
├── DEPLOYMENT_GUIDE.md           # 배포 가이드
└── README.md                     # 이 파일
```

---

## 📖 API 문서

### **Swagger UI**
- URL: `http://localhost:8080/swagger-ui/index.html`
- 모든 API 엔드포인트 확인 및 테스트 가능

### **주요 API 엔드포인트**

| 카테고리 | 메서드 | 경로 | 설명 | 인증 |
|---------|--------|------|------|------|
| **회원** | POST | `/api/members/signup` | 회원가입 | ❌ |
| | POST | `/api/members/login` | 로그인 | ❌ |
| | GET | `/api/members/me` | 내 정보 조회 | ✅ |
| **여행** | POST | `/api/trips` | 여행 생성 | ✅ |
| | GET | `/api/trips` | 여행 목록 조회 | ✅ |
| | GET | `/api/trips/{id}` | 여행 상세 조회 | ✅ |
| | PUT | `/api/trips/{id}` | 여행 수정 | ✅ |
| | DELETE | `/api/trips/{id}` | 여행 삭제 | ✅ |
| **여행 일자** | POST | `/api/trip-days` | 일정 추가 | ✅ |
| | PUT | `/api/trip-days/{id}` | 일정 수정 | ✅ |
| | DELETE | `/api/trip-days/{id}` | 일정 삭제 | ✅ |
| **여행 장소** | POST | `/api/trip-places` | 장소 추가 | ✅ |
| | PUT | `/api/trip-places/{id}` | 장소 수정 | ✅ |
| | DELETE | `/api/trip-places/{id}` | 장소 삭제 | ✅ |
| | PUT | `/api/trip-places/{id}/order` | 장소 순서 변경 | ✅ |
| **파일 업로드** | POST | `/api/files/trips/presigned-url` | Pre-signed URL 발급 | ✅ |
| | POST | `/api/files/upload-complete` | 업로드 완료 알림 | ✅ |
| | GET | `/api/files/trips/{tripId}` | 여행 이미지 목록 | ✅ |
| **여행 공유** | POST | `/api/trip-shares/trips/{tripId}` | 공유 링크 생성 | ✅ |
| | GET | `/api/trip-shares/shared/{token}` | 공유된 여행 조회 | ❌ |
| | GET | `/api/trip-shares/my-shares` | 내 공유 목록 | ✅ |
| | GET | `/api/trip-shares/public` | 공개 여행 목록 | ❌ |

---

## 🔧 개발 가이드

### **백엔드 빌드**

```bash
cd backend

# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# JAR 파일 실행
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### **프론트엔드 빌드**

```bash
cd frontend

# 프로덕션 빌드
npm run build

# 빌드 결과는 build/ 폴더에 생성됨
```

### **코드 스타일**

- **백엔드**: Google Java Style Guide
- **프론트엔드**: ESLint (Airbnb 스타일 기반)

---

## 🚀 배포

상세한 배포 가이드는 **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** 참고

### **빠른 배포 (Docker)**

```bash
# 1. 환경 변수 설정
cp ENV_EXAMPLE.txt .env
# .env 파일 수정

# 2. 프로덕션 모드 실행
docker-compose -f docker-compose.prod.yml up -d

# 3. 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 🛠️ 문제 해결

### **자주 발생하는 문제**

#### 1. **CORS 오류**
- `backend/.env`의 `ALLOWED_ORIGINS`에 프론트엔드 도메인 추가
- 예: `ALLOWED_ORIGINS=http://localhost:3000

#### 2. **쿠키 전송 안됨**
- `frontend/src/api/api.js`에서 `withCredentials: true` 확인
- HTTPS 사용시 쿠키의 `Secure` 속성 활성화

#### 3. **S3 업로드 실패**
- AWS 자격증명 확인
- S3 버킷 CORS 설정 확인
- IAM 사용자 권한 확인 (S3 PutObject 권한 필요)

#### 4. **MySQL 연결 실패**
- 데이터베이스가 실행 중인지 확인
- 환경 변수의 DB 연결 정보 확인
- 방화벽 규칙 확인

---

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

## 👥 기여

버그 리포트, 기능 제안, Pull Request를 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 문의

- **이메일**: paladog31@naver.com

---

## 🙏 감사의 말

- [Kakao Maps API](https://apis.map.kakao.com/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [AWS](https://aws.amazon.com/)

---

