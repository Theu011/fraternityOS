package com.fraternityos.server.house.infrastructure;

import java.time.Instant;

/** Read-model projection for house listing/search: basic info + active member count. */
public interface HouseSummary {

    Long getId();

    String getName();

    Instant getCreatedAt();

    long getActiveMemberCount();
}
