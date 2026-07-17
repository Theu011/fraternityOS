package com.fraternityos.server.house.domain;

/**
 * Lifecycle status of a house membership. A member is ACTIVE while living in the
 * house and becomes ALUMNI after leaving; their membership (and its position
 * history) is retained for the record.
 */
public enum MemberStatus {
    ACTIVE,
    ALUMNI
}
