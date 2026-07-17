package com.fraternityos.server.announcement.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraternityos.server.announcement.application.dto.CreateAnnouncementRequest;
import com.fraternityos.server.auth.application.JwtService;
import com.fraternityos.server.house.domain.House;
import com.fraternityos.server.house.domain.MemberStatus;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.User;
import com.fraternityos.server.house.infrastructure.HouseRepository;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.house.infrastructure.UserRepository;
import com.fraternityos.server.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests for the announcements endpoint, focused on the Task 5 RBAC
 * split: any authenticated member reads, only a President writes. Callers are
 * authenticated with real signed tokens minted for seeded memberships.
 */
class AnnouncementControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Test
    void president_createsAnnouncement_thenSeesItInTheFeed() throws Exception {
        String token = tokenFor("president@x.com", List.of("President"));

        mockMvc.perform(post("/announcements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAnnouncementRequest("Party Friday", "BYOB", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Party Friday"))
                .andExpect(jsonPath("$.pinned").value(true));

        mockMvc.perform(get("/announcements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Party Friday"));
    }

    @Test
    void resident_cannotCreateAnnouncement_forbidden() throws Exception {
        String token = tokenFor("resident@x.com", List.of());

        mockMvc.perform(post("/announcements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAnnouncementRequest("Nope", "not allowed", false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().isUnauthorized());
    }

    /** Seeds a house + user + membership and returns a signed token carrying the given positions. */
    private String tokenFor(String email, List<String> positions) {
        House house = houseRepository.save(new House("Test House"));
        User user = userRepository.save(new User("Test User", email, "irrelevant-hash"));
        Membership membership = membershipRepository.save(
                new Membership(user.getId(), house.getId(), MemberStatus.ACTIVE));
        return jwtService.generateToken(
                user.getId(), membership.getId(), house.getId(), email, positions);
    }
}
