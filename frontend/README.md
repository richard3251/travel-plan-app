# 여행 계획 앱 프론트엔드

이 프로젝트는 여행 계획 관리 애플리케이션의 프론트엔드 부분입니다. React를 사용하여 개발되었으며, 백엔드 API와 연동하여 여행 계획을 생성, 관리, 확인할 수 있는 기능을 제공합니다.

## 사용 기술

- React 18
- React Router v6
- Axios
- CSS (순수 CSS)

## 시작하기

### 설치

```bash
cd frontend
npm install
```

### 개발 서버 실행

```bash
npm start
```

기본적으로 http://localhost:3000 에서 개발 서버가 실행됩니다.

## 기능

- 여행 목록 보기
- 새로운 여행 계획 생성
- 여행 상세 정보 확인
- 여행 계획 수정 및 삭제

## 백엔드 연동

이 프론트엔드는 http://localhost:8080 에서 실행되는 Spring Boot 백엔드 API와 통신합니다. 백엔드 서버가 실행 중이어야 정상적으로 작동합니다.