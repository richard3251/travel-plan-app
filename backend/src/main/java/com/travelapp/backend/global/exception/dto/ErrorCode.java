package com.travelapp.backend.global.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (1000번대)
    INVALID_INPUT_VALUE(1000, "잘못된 입력값입니다.", 400),
    METHOD_NOT_ALLOWED(1001, "지원하지 않는 HTTP 메서드입니다.", 405),
    ENTITY_NOT_FOUND(1002, "요청한 리소스를 찾을 수 없습니다.", 404),
    INTERNAL_SERVER_ERROR(1003, "서버 내부 오류가 발생했습니다.", 500),
    INVALID_TYPE_VALUE(1004, "잘못된 타입의 값입니다.", 400),
    HANDLE_ACCESS_DENIED(1005, "접근이 거부되었습니다.", 403),

    // Member Errors (2000번대)
    MEMBER_NOT_FOUND(2000, "존재하지 않는 회원입니다.", 404),
    DUPLICATE_EMAIL(2001, "이미 사용 중인 이메일입니다.", 409),
    INVALID_PASSWORD(2002, "비밀번호가 일치하지 않습니다.", 400),
    MEMBER_ALREADY_EXISTS(2003, "이미 존재하는 회원입니다.", 409),

    // Trip Errors (3000번대)
    TRIP_NOT_FOUND(3000, "존재하지 않는 여행 계획입니다.", 404),
    TRIP_ACCESS_DENIED(3001, "해당 여행 계획에 접근할 권한이 없습니다.", 403),
    INVALID_TRIP_DATE(3002, "여행 날짜가 올바르지 않습니다.", 400),
    TRIP_ALREADY_EXISTS(3003, "이미 존재하는 여행 계획입니다.", 409),

    // TripDay Errors (4000번대)
    TRIP_DAY_NOT_FOUND(4000, "존재하지 않는 여행일입니다.", 404),
    INVALID_TRIP_DAY(4001, "올바르지 않은 여행일입니다.", 400),

    // TripPlace Errors (5000번대)
    TRIP_PLACE_NOT_FOUND(5000, "존재하지 않는 여행 장소입니다.", 404),
    INVALID_VISIT_ORDER(5001, "올바르지 않은 방문 순서입니다.", 400),
    DUPLICATE_VISIT_ORDER(5002, "중복된 방문 순서입니다.", 409),

    // External API Errors (6000번대)
    KAKAO_API_ERROR(6000, "카카오 API 호출 중 오류가 발생했습니다.", 500),
    EXTERNAL_API_TIMEOUT(6001, "외부 API 호출 시간이 초과되었습니다.", 408),

    // TripShare Errors (6500번대)
    TRIP_SHARE_NOT_FOUND(6500, "공유된 여행을 찾을 수 없습니다.", 404),
    TRIP_SHARE_ACCESS_DENIED(6501, "공유된 여행에 접근할 권한이 없습니다.", 403),
    TRIP_SHARE_EXPIRED(6502, "공유 링크가 만료되었습니다.", 410),
    TRIP_SHARE_ALREADY_EXISTS(6503, "이미 공유된 여행입니다.", 409),

    // Authentication Errors (7000번대)
    UNAUTHORIZED(7000, "인증이 필요합니다.", 401),
    INVALID_TOKEN(7001, "유효하지 않은 토큰입니다.", 401),
    TOKEN_EXPIRED(7002, "토큰이 만료되었습니다.", 401),

    // Validation Errors (8000번대)
    MISSING_REQUEST_PARAMETER(8000, "필수 요청 파라미터가 누락되었습니다.", 400),
    INVALID_REQUEST_BODY(8001, "요청 본문이 올바르지 않습니다.", 400),
    CONSTRAINT_VIOLATION(8002, "데이터 제약 조건을 위반했습니다.", 400),

    // File Upload Errors (9000번대)
    FILE_NOT_FOUND(9000, "파일을 찾을 수 없습니다.", 404),
    INVALID_FILE_TYPE(9001, "지원하지 않는 파일 형식입니다.", 400),
    FILE_SIZE_EXCEEDED(9002, "파일 크기가 허용된 범위를 초과했습니다.", 400),
    FILE_UPLOAD_FAILED(9003, "파일 업로드에 실패했습니다.", 500),
    THUMBNAIL_CREATION_FAILED(9004, "썸네일 생성에 실패했습니다.", 500),
    PRESIGNED_URL_GENERATION_FAILED(9005, "Pre-signed URL 생성에 실패했습니다.", 500),
    S3_UPLOAD_FAILED(9006, "S3 업로드에 실패했습니다.", 500);

    private final int code;
    private final String message;
    private final int status;
}
