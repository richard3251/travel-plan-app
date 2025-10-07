package com.travelapp.backend.domain.file.controller;

import com.travelapp.backend.domain.file.dto.request.PresignedUrlRequest;
import com.travelapp.backend.domain.file.dto.request.UploadCompleteRequest;
import com.travelapp.backend.domain.file.dto.response.FileUploadResponse;
import com.travelapp.backend.domain.file.dto.response.PresignedUrlResponse;
import com.travelapp.backend.domain.file.dto.response.TripImageResponse;
import com.travelapp.backend.domain.file.service.FileService;
import com.travelapp.backend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 여행 이미지 업로드용 Pre-signed URL 발급
     */
    @Operation(
        summary = "여행 이미지 Pre-signed URL 발급",
        description = "여행 이미지 업로드를 위한 Pre-signed URL을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pre-signed URL 발급 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 정보 또는 크기 초과"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 여행")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/trips/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generateTripImagePresignedUrl(
        @Parameter(description = "여행 이미지 Pre-signed URL 발급 요청 정보")
        @Valid @RequestBody PresignedUrlRequest request
    ) {
        log.info("여행 이미지 Pre-signed URL 발급 요청 - 여행: {}, 파일명: {}"
        , request.getTripId(), request.getFileName());

        Long userId = SecurityUtil.getCurrentMemberId();
        PresignedUrlResponse response = fileService.generateTripImagePresignedUrl(request, userId);

        log.info("여행 이미지 Pre-signed URL 발급 성공 - 파일 ID: {}", response.getFileId());
        return ResponseEntity.ok(response);
    }

    /**
     * 업로드 완료 통지
     */
    @Operation(
        summary = "업로드 완료 통지",
        description = "클라이언트가 S3에 파일을 업로드한 후 서버에 완료를 통지합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "업로드 완료 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 파일")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/upload-complete")
    public ResponseEntity<FileUploadResponse> completeUpload(
        @Parameter(description = "업로드 완료 통지 정보")
        @Valid @RequestBody UploadCompleteRequest request
    ) {
        log.info("업로드 완료 통지 - 파일 ID: {}, 성공: {}", request.getFileId(), request.getSuccess());

        Long userId = SecurityUtil.getCurrentMemberId();
        FileUploadResponse response = fileService.completeUpload(request, userId);

        log.info("업로드 완료 처리 성공 - 파일 ID: {}, 상태: {}", response.getId(), response.getUploadStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * 내 파일 목록 조회
     */
    @Operation(
        summary = "내 파일 목록 조회",
        description = "업로드 완료된 파일 목록을 페이징하여 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my")
    public ResponseEntity<Page<FileUploadResponse>> getMyFiles(
        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        Long userId = SecurityUtil.getCurrentMemberId();
        Page<FileUploadResponse> files = fileService.getUserFiles(userId, pageable);

        return ResponseEntity.ok(files);
    }

    /**
     * 여행 이미지 목록 조회
     */
    @Operation(
        summary = "여행 이미지 목록 조회",
        description = "특정 여행의 업로드 완료된 모든 이미지를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이미지 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 여행")
    })
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<List<TripImageResponse>> getTripImages(
        @Parameter(description = "여행 ID", example = "1")
        @PathVariable Long tripId
    ) {
        List<TripImageResponse> images = fileService.getTripImages(tripId);
        return ResponseEntity.ok(images);
    }

    /**
     * 여행 커버 이미지 조회
     */
    @Operation(
        summary = "여행 커버 이미지 조회",
        description = "특정 여행의 커버 이미지를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "커버 이미지 조회 성공"),
        @ApiResponse(responseCode = "404", description = "커버 이미지를 찾을 수 없음")
    })
    @GetMapping("/trips/{tripId}/cover")
    public ResponseEntity<TripImageResponse> getTripCoverImage(
        @Parameter(description = "여행 ID", example = "1")
        @PathVariable Long tripId
    ) {
        TripImageResponse coverImage = fileService.getTripCoverImage(tripId);

        return ResponseEntity.ok(coverImage);
    }

    /**
     * 파일 삭제
     */
    @Operation(
        summary = "파일 삭제",
        description = "업로드한 파일을 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(
        @Parameter(description = "파일 ID", example = "1")
        @PathVariable Long fileId
    ) {
        Long userId = SecurityUtil.getCurrentMemberId();
        fileService.deleteFile(fileId, userId);

        return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
    }

    /**
     * 여행 이미지 삭제
     */
    @Operation(
        summary = "여행 이미지 삭제",
        description = "여행에서 이미지를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 필요"),
        @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/trips/images/{imageId}")
    public ResponseEntity<String> deleteTripImage(
        @Parameter(description = "이미지 ID", example = "1")
        @PathVariable Long imageId
    ) {
        Long userId = SecurityUtil.getCurrentMemberId();
        fileService.deleteTripImage(imageId, userId);

        return ResponseEntity.ok("여행 이미지가 성공적으로 삭제되었습니다.");
    }

    /**
     * 여행 이미지 순서 변경
     */
    @Operation(
        summary = "여행 이미지 순서 변경",
        description = "여행 이미지의 표시 순서를 변경합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "순서 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PutMapping("/trips/images/{imageId}/order")
    public ResponseEntity<String> updateTripImageOrder(
        @Parameter(description = "이미지 ID", example = "1")
        @PathVariable Long imageId,
        @Parameter(description = "새로운 표시 순서", example = "2")
        @RequestParam Integer newOrder
    ) {
        Long userId = SecurityUtil.getCurrentMemberId();
        fileService.updateTripImageOrder(imageId, newOrder, userId);

        return ResponseEntity.ok("이미지 순서가 성공적으로 변경되었습니다.");
    }



























}
