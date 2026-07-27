package com.project.meeting_platform.auth.controller.Participant;

import com.project.meeting_platform.auth.Service.Participant.ParticipantService;
import com.project.meeting_platform.realtime.ParticipantPresenceEvent;
import com.project.meeting_platform.realtime.PresenceJoinRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RoomPresenceWebSocketController {

    private final ParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomPresenceWebSocketController(
            ParticipantService participantService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.participantService = participantService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rooms/{slug}/presence/join")
    public void join(
            @DestinationVariable String slug,
            @Payload PresenceJoinRequest request
    ) {
        ParticipantPresenceEvent event =
                participantService.createJoinedEvent(
                        slug,
                        request.participantId()
                );

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + slug + "/presence",
                event
        );
    }

}
