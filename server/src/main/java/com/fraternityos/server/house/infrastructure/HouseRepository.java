package com.fraternityos.server.house.infrastructure;

import com.fraternityos.server.house.domain.House;
import com.fraternityos.server.house.domain.MemberStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseRepository extends JpaRepository<House, Long> {

    /** All houses with their ACTIVE member count, ordered by name. */
    @Query("select h.id as id, h.name as name, h.createdAt as createdAt, "
            + "(select count(m.id) from Membership m "
            + " where m.houseId = h.id and m.status = :active) as activeMemberCount "
            + "from House h "
            + "order by h.name asc")
    List<HouseSummary> findSummaries(@Param("active") MemberStatus active);

    /** Houses whose name contains {@code name} (case-insensitive), with active member count. */
    @Query("select h.id as id, h.name as name, h.createdAt as createdAt, "
            + "(select count(m.id) from Membership m "
            + " where m.houseId = h.id and m.status = :active) as activeMemberCount "
            + "from House h "
            + "where lower(h.name) like lower(concat('%', :name, '%')) "
            + "order by h.name asc")
    List<HouseSummary> searchSummaries(@Param("name") String name, @Param("active") MemberStatus active);
}
