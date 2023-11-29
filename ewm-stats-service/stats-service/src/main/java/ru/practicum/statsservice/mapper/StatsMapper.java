package ru.practicum.statsservice.mapper;

import ru.practicum.statsdto.EndpointHitDto;
import ru.practicum.statsservice.model.EndpointHit;

public class StatsMapper {
    public static EndpointHit toHit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(endpointHitDto.getApp());
        hit.setUri(endpointHitDto.getUri());
        hit.setIp(endpointHitDto.getIp());
        hit.setTimestamp(endpointHitDto.getTimestamp());
        return hit;
    }

    public static EndpointHitDto toHitDto(EndpointHit endpointHit) {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setId(endpointHit.getId());
        hitDto.setApp(endpointHit.getApp());
        hitDto.setUri(endpointHit.getUri());
        hitDto.setIp(endpointHit.getIp());
        hitDto.setTimestamp(endpointHit.getTimestamp());
        return hitDto;
    }


}
