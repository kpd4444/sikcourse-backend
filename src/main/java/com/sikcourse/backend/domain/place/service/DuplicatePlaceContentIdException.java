package com.sikcourse.backend.domain.place.service;

public class DuplicatePlaceContentIdException extends RuntimeException {

    public DuplicatePlaceContentIdException(String contentId) {
        super("Duplicate place contentId: " + contentId);
    }

    public DuplicatePlaceContentIdException(String contentId, Throwable cause) {
        super("Duplicate place contentId: " + contentId, cause);
    }
}
