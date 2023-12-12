package ru.practicum.statsclient;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsdto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("http://localhost:9090") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris,
                                           @Nullable Boolean unique) {
        StringBuilder path = new StringBuilder("/stats?start={start}&end={end}");
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("start", start.format(formatter));
        params.put("end", end.format(formatter));
        if (uris != null && !uris.isEmpty()) {
            String pathUris = String.join("&uris=", uris);
            path.append("&uris={uris}");
            params.put("uris", pathUris);
        }
        if (unique != null) {
            path.append("&unique={unique}");
            params.put("unique", unique);
        }
        return get(path.toString(), params);
    }

}
