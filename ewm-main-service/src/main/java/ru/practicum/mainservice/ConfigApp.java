package ru.practicum.mainservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.statsclient.StatsClient;


@Configuration
public class ConfigApp {
    @Value(value = "http://localhost:9090")
    private String serverUrl;

    @Bean
    public StatsClient getStatsClient() {
        return new StatsClient(serverUrl, new RestTemplateBuilder());
    }
}


