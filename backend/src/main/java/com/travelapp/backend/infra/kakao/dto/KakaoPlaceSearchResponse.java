package com.travelapp.backend.infra.kakao.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class KakaoPlaceSearchResponse {

    private List<Document> documents;


    @Getter
    public static class Document {
       private String id;
       private String place_name;
       private String category_name;
       private String address_name;
       private String road_address_name;
       private String phone;
       private String x;
       private String y;
       private String place_url;
    }

    @Getter
    public static class Meta {
        private int total_count;
        private int pageable_count;
        private boolean is_end;
    }

}
