package com.fraternityos.server.house.infrastructure;

import com.fraternityos.server.house.domain.MembershipPosition;
import com.fraternityos.server.house.domain.MembershipPositionId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipPositionRepository
        extends JpaRepository<MembershipPosition, MembershipPositionId> {

    List<MembershipPosition> findByMembershipId(Long membershipId);

    boolean existsByMembershipIdAndPositionId(Long membershipId, Long positionId);

    void deleteByMembershipId(Long membershipId);

    void deleteByMembershipIdAndPositionId(Long membershipId, Long positionId);

    /** Position names held by a single membership. */
    @Query("select p.name from MembershipPosition mp, Position p "
            + "where p.id = mp.positionId and mp.membershipId = :membershipId")
    List<String> findPositionNames(@Param("membershipId") Long membershipId);

    /** membership-id + position name for a set of memberships (for list read models). */
    @Query("select mp.membershipId as membershipId, p.name as name "
            + "from MembershipPosition mp, Position p "
            + "where p.id = mp.positionId and mp.membershipId in :membershipIds")
    List<MembershipPositionName> findPositionNamesByMembershipIds(
            @Param("membershipIds") Collection<Long> membershipIds);

    interface MembershipPositionName {
        Long getMembershipId();

        String getName();
    }
}
