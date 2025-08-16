package com.travelapp.backend.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "에러 응답 DTO")
@Getter
@Builder
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "1001")
    private final int code;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
    private final String message;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "에러 발생 시간", example = "2024-12-25 10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(description = "요청 경로", example = "/api/members/signup")
    private final String path;

    @Schema(description = "필드 유효성 검사 에러 목록")
    private final List<FieldError> fieldErrors;

    @Schema(description = "필드 에러 정보")
    @Getter
    @Builder
    public static class FieldError {
        @Schema(description = "에러가 발생한 필드명", example = "email")
        private final String field;

        @Schema(description = "입력된 값", example = "invalid-email")
        private final String value;

        @Schema(description = "에러 사유", example = "올바른 이메일 형식이 아닙니다")
        private final String reason;
    }

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .status(errorCode.getStatus())
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .status(errorCode.getStatus())
            .timestamp(LocalDateTime.now())
            .path(path)
            .fieldErrors(fieldErrors)
            .build();
    }




}
