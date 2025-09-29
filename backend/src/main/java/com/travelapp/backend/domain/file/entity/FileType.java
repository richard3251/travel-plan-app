package com.travelapp.backend.domain.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    IMAGE("이미지"),
    DOCUMENT("문서"),
    VIDEO("동영상"),
    AUDIO("오디오"),
    OTHER("기타");

    private final String description;

    public static FileType fromContentType(String contentType) {
        if (contentType == null) {
            return OTHER;
        }

        if (contentType.startsWith("image/")) {
            return IMAGE;
        } else if (contentType.startsWith("video/")) {
            return VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return AUDIO;
        } else if (contentType.contains("pdf") ||
            contentType.contains("document") ||
            contentType.contains("text")) {
            return DOCUMENT;
        }

        return OTHER;
    }
}
