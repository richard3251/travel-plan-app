package com.travelapp.backend.domain.file.controller;

import com.travelapp.backend.domain.file.dto.request.PresignedUrlRequest;
import com.travelapp.backend.domain.file.dto.response.PresignedUrlResponse;
import com.travelapp.backend.domain.file.service.FileService;
import com.travelapp.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 관리", description = "Pre-signed URL 기반 파일 업로드, 다운로드, 삭제 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    /**
     * 일반 파일 업로드용 Pre-signed URL 발급
     */
    @Operation(
        summary = "일반 파일 Pre-signed URL 발급",
        description = "일반 파일 업로드를 위한 Pre-Signed URL을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pre-signed URL 발급 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 정보 또는 크기 초과"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
        @Parameter(description = "Pre-signed URL 발급 요청 정보")
        @Valid @RequestBody PresignedUrlRequest request
    ) {
        log.info("일반 파일 Pre-signed URL 발급 요청 - 파일명: {}", request.getFileName());

        Long userId = SecurityUtil.getCurrentMemberId();
        PresignedUrlResponse response = fileService.generatePresignedUrl(request, userId);

        log.info("일반 파일 Pre-signed URL 발급 성공 - 파일 ID: {}", response.getFileId());
        return ResponseEntity.ok(response);
    }
























}
