package com.fraternityos.server.house.infrastructure;

import com.fraternityos.server.house.domain.JoinRequest;
import com.fraternityos.server.house.domain.JoinRequestStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    boolean existsByUserIdAndHouseIdAndStatus(Long userId, Long houseId, JoinRequestStatus status);

    List<JoinRequest> findByUserIdAndStatus(Long userId, JoinRequestStatus status);

    /** The caller's own requests, most recent first, with the target house name. */
    @Query("select jr.id as id, jr.houseId as houseId, h.name as houseName, "
            + "jr.status as status, jr.createdAt as createdAt "
            + "from JoinRequest jr, House h "
            + "where h.id = jr.houseId and jr.userId = :userId "
            + "order by jr.createdAt desc")
    List<MyJoinRequest> findMine(@Param("userId") Long userId);

    /** Pending requests to a house, with requester identity (for President review). */
    @Query("select jr.id as id, jr.userId as userId, u.name as name, u.email as email, "
            + "jr.createdAt as createdAt "
            + "from JoinRequest jr, User u "
            + "where u.id = jr.userId and jr.houseId = :houseId and jr.status = :status "
            + "order by jr.createdAt asc")
    List<PendingJoinRequest> findPending(@Param("houseId") Long houseId,
                                         @Param("status") JoinRequestStatus status);

    interface MyJoinRequest {
        Long getId();

        Long getHouseId();

        String getHouseName();

        JoinRequestStatus getStatus();

        Instant getCreatedAt();
    }

    interface PendingJoinRequest {
        Long getId();

        Long getUserId();

        String getName();

        String getEmail();

        Instant getCreatedAt();
    }
}
