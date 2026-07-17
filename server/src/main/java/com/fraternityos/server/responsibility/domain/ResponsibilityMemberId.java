package com.fraternityos.server.responsibility.domain;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key for {@link ResponsibilityMember}. */
public class ResponsibilityMemberId implements Serializable {

    private Long responsibilityId;
    private Long membershipId;

    public ResponsibilityMemberId() {
    }

    public ResponsibilityMemberId(Long responsibilityId, Long membershipId) {
        this.responsibilityId = responsibilityId;
        this.membershipId = membershipId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResponsibilityMemberId that)) {
            return false;
        }
        return Objects.equals(responsibilityId, that.responsibilityId)
                && Objects.equals(membershipId, that.membershipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responsibilityId, membershipId);
    }
}
