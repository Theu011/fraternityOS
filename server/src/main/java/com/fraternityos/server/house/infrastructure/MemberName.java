package com.fraternityos.server.house.infrastructure;

/**
 * Read projection pairing a membership id with its user's display name, so
 * house-scoped read models can label rows without loading full entities.
 */
public interface MemberName {

    Long getMembershipId();

    String getName();
}
