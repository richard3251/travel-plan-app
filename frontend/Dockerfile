# 1단계: 빌드 단계
FROM node:18-alpine as build

# 작업 디렉토리 설정
WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY . .

# 빌드 인자로 API URL 전달 가능
ARG REACT_APP_API_BASE_URL
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

# 프로덕션 빌드
RUN npm run build

# 2단계: Nginx로 서빙
FROM nginx:alpine

# 빌드된 파일을 Nginx 기본 디렉토리로 복사
COPY --from=build /app/build /usr/share/nginx/html

# Nginx 설정 파일 복사 (React Router 지원)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]