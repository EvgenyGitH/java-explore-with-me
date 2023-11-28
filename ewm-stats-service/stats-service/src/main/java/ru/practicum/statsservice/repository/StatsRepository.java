package ru.practicum.statsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statsdto.ViewStats;
import ru.practicum.statsservice.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.statsdto.ViewStats (eph.app, eph.uri, count(distinct(eph.ip))) " +
            "from EndpointHit as eph " +
            "where eph.timestamp >= ?1 and eph.timestamp <= ?2 " +
            "group by eph.app, eph.uri " +
            "order by count(distinct(eph.ip)) desc ")
    List<ViewStats> getStatsByUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.statsdto.ViewStats (eph.app, eph.uri, count(distinct(eph.ip))) " +
            "from EndpointHit as eph " +
            "where eph.timestamp >= ?1 and eph.timestamp <= ?2 " +
            "and eph.uri in ?3 " +
            "group by eph.app, eph.uri " +
            "order by count(distinct(eph.ip)) desc ")
    List<ViewStats> getStatsByUniqueIpInUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.statsdto.ViewStats (eph.app, eph.uri, count(eph.ip)) " +
            "from EndpointHit as eph " +
            "where eph.timestamp >= ?1 and eph.timestamp <= ?2 " +
            "group by eph.app, eph.uri " +
            "order by count(eph.ip) desc ")
    List<ViewStats> getStatsByNonUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.statsdto.ViewStats (eph.app, eph.uri, count(eph.ip)) " +
            "from EndpointHit as eph " +
            "where eph.timestamp >= ?1 and eph.timestamp <= ?2 " +
            "and eph.uri in ?3 " +
            "group by eph.app, eph.uri " +
            "order by count(eph.ip) desc ")
    List<ViewStats> getStatsByNonUniqueIpInUris(LocalDateTime start, LocalDateTime end, List<String> uris);

}


