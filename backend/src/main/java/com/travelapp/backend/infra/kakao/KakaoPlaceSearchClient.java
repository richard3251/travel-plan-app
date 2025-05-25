package com.travelapp.backend.infra.kakao;

import com.travelapp.backend.global.kakao.dto.KakaoPlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

//    @PostConstruct
//    public void init() {
//        log.info("사용 중인 카카오 API 키: {}", kakaoRestApiKey);
//    }

    /**
     * 카카오 장소 검색 요청 (페이지네이션 지원)
     * @param keyword 검색어(예: "카페")
     * @param longitude 경도 (x)
     * @param latitude 위도 (y)
     * @param page 페이지 번호 (1~45)
     * @param size 한 페이지에 보여질 문서의 개수 (1~15)
     * @return Kakao API로 부터 받은 JSON 문자열 응답
     */
    public KakaoPlaceSearchResponse searchPlaces(String keyword, double latitude, double longitude, int page, int size) {

        // 파라미터 유효성 검사 및 final 변수로 재할당
        final int validPage = (page < 1 || page > 45) ? 1 : page;
        final int validSize = (size < 1 || size > 15) ? 15 : size;

        log.info("카카오 장소 검색 API 호출: keyword={}, lat={}, lng={}, page={}, size={}", keyword, latitude, longitude, validPage, validSize);
        
        return webClient.get() // GET 방식 요청 시작
            .uri(uriBuilder -> uriBuilder
                .queryParam("query", keyword)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", 20000) // 20km 반경으로 늘려서 더 많은 검색 결과 제공
                .queryParam("page", validPage)
                .queryParam("size", validSize)
                .build())
            .header("Authorization", "KakaoAK " + kakaoRestApiKey) // 인증용 헤더. KakaoAK 띄어쓰기 필수!
            .retrieve() // 요청을 보내고 응답 수신 대기
            .bodyToMono(KakaoPlaceSearchResponse.class) // 응답 JSON을 KakaoPlaceSearchResponse 객체로 변환하여 비동기(Mono) 스트림으로 처리
            .onErrorResume(e -> {
                log.error("Kakao API 호출 실패", e);
                return Mono.empty(); // 예외 발생시 빈 응답
            })
            .block(); // block()을 통해 응답을 동기적으로 받음 (간단한 요청 처리 시 유용)
    }

}
