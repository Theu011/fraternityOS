package com.fraternityos.server.house.infrastructure;

import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByUserId(Long userId);

    List<Membership> findAllByHouseId(Long houseId);

    List<Membership> findAllByHouseIdAndStatus(Long houseId, MemberStatus status);

    /**
     * How many <em>other</em> memberships in the house hold the named position
     * with the given status. Used to guard the "at least one President" invariant.
     */
    @Query("select count(m) from Membership m "
            + "where m.houseId = :houseId and m.status = :status and m.id <> :excludeId "
            + "and exists (select mp from MembershipPosition mp, Position p "
            + "  where mp.membershipId = m.id and p.id = mp.positionId and p.name = :positionName)")
    long countHoldersOfPositionExcluding(@Param("houseId") Long houseId,
                                         @Param("status") MemberStatus status,
                                         @Param("positionName") String positionName,
                                         @Param("excludeId") Long excludeId);

    /** Tenancy-scoped lookup: returns the membership only if it belongs to the given house. */
    Optional<Membership> findByIdAndHouseId(Long id, Long houseId);

    /** Membership-id → user display name for every membership in a house. */
    @Query("select m.id as membershipId, u.name as name "
            + "from Membership m, User u where u.id = m.userId and m.houseId = :houseId")
    List<MemberName> findMemberNamesByHouseId(@Param("houseId") Long houseId);

    /** Display name for a single membership, scoped to a house. */
    @Query("select u.name from Membership m, User u "
            + "where u.id = m.userId and m.id = :membershipId and m.houseId = :houseId")
    Optional<String> findMemberName(@Param("membershipId") Long membershipId,
                                    @Param("houseId") Long houseId);
}
