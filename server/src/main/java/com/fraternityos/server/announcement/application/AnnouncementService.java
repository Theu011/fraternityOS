package com.fraternityos.server.announcement.application;

import com.fraternityos.server.announcement.application.dto.AnnouncementResponse;
import com.fraternityos.server.announcement.application.dto.CreateAnnouncementRequest;
import com.fraternityos.server.announcement.application.dto.UpdateAnnouncementRequest;
import com.fraternityos.server.announcement.domain.Announcement;
import com.fraternityos.server.announcement.infrastructure.AnnouncementRepository;
import com.fraternityos.server.house.infrastructure.MemberName;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Announcement CRUD, always scoped to the caller's house. Author display names
 * are resolved from the author's membership for the read model.
 */
@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final MembershipRepository membershipRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               MembershipRepository membershipRepository) {
        this.announcementRepository = announcementRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> list(Long houseId) {
        Map<Long, String> names = membershipRepository.findMemberNamesByHouseId(houseId).stream()
                .collect(Collectors.toMap(MemberName::getMembershipId, MemberName::getName));
        return announcementRepository.findAllByHouseIdOrderByPinnedDescCreatedAtDesc(houseId).stream()
                .map(a -> AnnouncementResponse.from(a, names.get(a.getAuthorMembershipId())))
                .toList();
    }

    @Transactional
    public AnnouncementResponse create(Long houseId, Long authorMembershipId,
                                       CreateAnnouncementRequest request) {
        Announcement saved = announcementRepository.save(new Announcement(
                houseId, authorMembershipId, request.title(), request.content(), request.pinned()));
        return AnnouncementResponse.from(saved, authorName(houseId, authorMembershipId));
    }

    @Transactional
    public AnnouncementResponse update(Long houseId, Long id, UpdateAnnouncementRequest request) {
        Announcement announcement = require(houseId, id);
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setPinned(request.pinned());
        return AnnouncementResponse.from(announcementRepository.save(announcement),
                authorName(houseId, announcement.getAuthorMembershipId()));
    }

    @Transactional
    public void delete(Long houseId, Long id) {
        announcementRepository.delete(require(houseId, id));
    }

    private Announcement require(Long houseId, Long id) {
        return announcementRepository.findByIdAndHouseId(id, houseId)
                .orElseThrow(() -> new AnnouncementNotFoundException(id));
    }

    private String authorName(Long houseId, Long authorMembershipId) {
        return membershipRepository.findMemberName(authorMembershipId, houseId).orElse(null);
    }
}
