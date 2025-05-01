package com.travelapp.backend.infra.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchClient {

    // 카카오 로컬 API에서 키워드 검색을 요청할 때 사용하는 기본 URL
    private static final String KAKAO_LOCAL_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    // WebClient 객체를 직접 builder로 생성함
    // baseUrl을 KAKAO_LOCAL_SEARCH_URL로 지정하여 이후 GET 요청 시 자동으로 해당 URL 사용
    private final WebClient webClient = WebClient.builder().baseUrl(KAKAO_LOCAL_SEARCH_URL).build();

    // application.yml 또는 .env에서 "kakao.rest-api-key" 값을 주입받음
    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    /**
     * 카카오 장소 검색 요청
     * @param keyword 검색어(예: "카페")
     * @param longitude 경도 (x)
     * @param latitude 위도 (y)
     * @return Kakao API로 부터 받은 JSON 문자열 응답
     */
    public String searchPlaces(String keyword, double latitude, double longitude) {
        return webClient.get() // GET 방식 요청 시작
            .uri(uriBuilder -> uriBuilder
                .queryParam("query", keyword)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", 3000) // 3km 반경
                .build())
            .header("Authorization", "KakaoAK " + kakaoRestApiKey) // 인증용 헤더. KakaoAK 띄어쓰기 필수!
            .retrieve() // 요청을 보내고 응답 수신 대기
            .bodyToMono(String.class) // 응답을 String(JSON)으로 비동기 처리
            .block(); // block()을 통해 응답을 동기적으로 받음 (간단한 요청 처리 시 유용)
    }

}
