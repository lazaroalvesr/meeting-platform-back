package com.project.meeting_platform.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "prospected_leads")
@Getter
@NoArgsConstructor
public class ProspectedLead {

    private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @Column(name = "provider_place_id", length = 255)
    private String providerPlaceId;

    @Column(name = "deduplication_key", nullable = false, length = 600)
    private String deduplicationKey;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String category;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "public_address", length = 500)
    private String publicAddress;

    @Column(length = 500)
    private String website;

    @Column(length = 50)
    private String phone;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(name = "opening_message", columnDefinition = "TEXT")
    private String openingMessage;

    @Column(name = "prospected_at", nullable = false, updatable = false)
    private Instant prospectedAt;

    @Column(name = "follow_up_count", nullable = false)
    private int followUpCount;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "last_follow_up_at")
    private Instant lastFollowUpAt;

    public ProspectedLead(
            User owner,
            String providerPlaceId,
            String deduplicationKey,
            String name,
            String category,
            String city,
            String state,
            String publicAddress,
            String website,
            String phone,
            String sourceUrl,
            Integer priorityScore,
            String openingMessage
    ) {
        this.owner = owner;
        this.providerPlaceId = providerPlaceId;
        this.deduplicationKey = deduplicationKey;
        this.name = name;
        this.category = category;
        this.city = city;
        this.state = state;
        this.publicAddress = publicAddress;
        this.website = website;
        this.phone = phone;
        this.sourceUrl = sourceUrl;
        this.priorityScore = priorityScore;
        this.openingMessage = openingMessage;
    }

    @PrePersist
    private void onCreate() {
        this.prospectedAt = Instant.now();
        this.nextFollowUpDate = LocalDate.now(BRAZIL_ZONE).plusDays(7);
    }

    public void registerFollowUp() {
        LocalDate today = LocalDate.now(BRAZIL_ZONE);
        this.followUpCount += 1;
        this.lastFollowUpAt = Instant.now();

        if (followUpCount >= 3) {
            this.nextFollowUpDate = today.plusMonths(1);
            return;
        }

        int daysAfterProspecting = followUpCount == 1 ? 15 : 30;
        LocalDate prospectionDate = prospectedAt.atZone(BRAZIL_ZONE).toLocalDate();
        LocalDate plannedDate = prospectionDate.plusDays(daysAfterProspecting);
        this.nextFollowUpDate = plannedDate.isAfter(today) ? plannedDate : today.plusDays(7);
    }
}
