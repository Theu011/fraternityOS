package com.fraternityos.server.house.domain;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key for {@link MembershipPosition}. */
public class MembershipPositionId implements Serializable {

    private Long membershipId;
    private Long positionId;

    public MembershipPositionId() {
    }

    public MembershipPositionId(Long membershipId, Long positionId) {
        this.membershipId = membershipId;
        this.positionId = positionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MembershipPositionId that)) {
            return false;
        }
        return Objects.equals(membershipId, that.membershipId)
                && Objects.equals(positionId, that.positionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membershipId, positionId);
    }
}
