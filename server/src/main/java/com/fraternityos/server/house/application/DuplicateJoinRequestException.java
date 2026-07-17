package com.fraternityos.server.house.application;

/** Raised when a user submits a join request they already have outstanding. */
public class DuplicateJoinRequestException extends RuntimeException {

    public DuplicateJoinRequestException(Long houseId) {
        super("A pending join request already exists for house " + houseId);
    }
}
