package com.travelapp.backend.domain.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadStatus {

    PENDING("업로드 대기중"),
    COMPLETED("업로드 완료"),
    FAILED("업로드 실패");

    private final String description;
}
