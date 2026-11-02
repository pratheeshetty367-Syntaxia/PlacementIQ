package com.placementiq.config;
 
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
 
/**
 * Builds the single RestClient used to talk to the OpenAI API.
 *
 * RestClient is part of spring-web 6.1, which Spring Boot 3.2.5 already brings
 * in through spring-boot-starter-web, so no new dependency is introduced for
 * the HTTP calls in this part.
 *
 * Both timeouts are configured here rather than left at the JDK defaults. Without
 * an explicit read timeout a stalled OpenAI connection would hold a Tomcat worker
 * thread open indefinitely and eventually starve the whole application.
 *
 * The Authorization header is deliberately NOT set as a default header: it is
 * attached per request in OpenAiService, so the key is read at call time and is
 * never baked into a long-lived bean.
 */
@Configuration
@RequiredArgsConstructor
public class OpenAiConfig {
 
    private final OpenAiProperties openAiProperties;
 
    @Bean
    public RestClient openAiRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(openAiProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(openAiProperties.getReadTimeoutMs());
 
        return RestClient.builder()
                .baseUrl(openAiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
