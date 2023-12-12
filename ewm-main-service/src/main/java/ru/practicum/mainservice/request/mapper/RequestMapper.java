package ru.practicum.mainservice.request.mapper;

import ru.practicum.mainservice.request.dto.ParticipationRequestDto;
import ru.practicum.mainservice.request.model.ParticipationRequest;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(participationRequest.getId());
        participationRequestDto.setCreated(participationRequest.getCreated());
        participationRequestDto.setEvent(participationRequest.getEvent().getId());
        participationRequestDto.setRequester(participationRequest.getRequester().getId());
        participationRequestDto.setStatus(participationRequest.getStatus());
        return participationRequestDto;
    }


}
