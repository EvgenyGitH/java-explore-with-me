package ru.practicum.statsservice.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.practicum.statsdto.EndpointHitDto;
import ru.practicum.statsdto.ViewStats;
import ru.practicum.statsservice.exceprion.ValidationException;
import ru.practicum.statsservice.mapper.StatsMapper;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = StatsMapper.toHit(endpointHitDto);
        statsRepository.save(hit);
        return StatsMapper.toHitDto(hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Not correct dates");
        }
        if (unique) {
            if (uris == null) {
                return statsRepository.getStatsByUniqueIp(start, end);
            } else {
                return statsRepository.getStatsByUniqueIpInUris(start, end, uris);
            }
        } else {
            if (uris == null) {
                return statsRepository.getStatsByNonUniqueIp(start, end);
            } else {
                return statsRepository.getStatsByNonUniqueIpInUris(start, end, uris);
            }
        }
    }

}
