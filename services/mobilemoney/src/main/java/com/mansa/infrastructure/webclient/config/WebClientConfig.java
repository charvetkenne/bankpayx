package com.mansa.infrastructure.webclient.config;


import com.mansa.infrastructure.operator.cinetpay.config.CinetPayProperties;
import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.notchpay.config.NotchPayProperties;
import com.mansa.infrastructure.operator.orange.config.OrangeProperties;
import com.mansa.infrastructure.operator.wave.config.WaveProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    // @Bean("mtnWebClient")
    // public WebClient mtnWebClient(MtnProperties props, WebClient.Builder builder) {
    //     return builder
    //             .baseUrl(props.baseUrl())
    //             .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    //             .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    //             .clientConnector(reactorConnector(props.connectTimeoutMs(), props.readTimeoutMs()))
    //             .filter(loggingFilter("MTN"))
    //             .build();
    // }

    @Bean
   public WebClient mtnWebClient(WebClient.Builder builder, MtnProperties mtnProperties) {
    return builder
            .baseUrl(mtnProperties.baseUrl())
            .defaultHeader(HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (compatible; BankPayX/1.0)")
            .build();
}

    @Bean("orangeWebClient")
    public WebClient orangeWebClient(OrangeProperties props, WebClient.Builder builder) {
        return builder
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(reactorConnector(props.connectTimeoutMs(), props.readTimeoutMs()))
                .filter(loggingFilter("ORANGE"))
                .build();
    }

    @Bean("waveWebClient")
    public WebClient waveWebClient(WaveProperties props, WebClient.Builder builder) {
        return builder
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(reactorConnector(props.connectTimeoutMs(), props.readTimeoutMs()))
                .filter(loggingFilter("WAVE"))
                .build();
    }

    @Bean("cinetPayWebClient")
    public WebClient cinetPayWebClient(CinetPayProperties props, WebClient.Builder builder) {
        return builder
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(reactorConnector(props.connectTimeoutMs(), props.readTimeoutMs()))
                .filter(loggingFilter("CINETPAY"))
                .build();
    }



        @Bean("notchPayWebClient")
        public WebClient notchPayWebClient(NotchPayProperties props, WebClient.Builder builder) {
            return builder
                    .baseUrl(props.baseUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT,       MediaType.APPLICATION_JSON_VALUE)
                    .clientConnector(reactorConnector(props.connectTimeoutMs(), props.readTimeoutMs()))
                    .filter(loggingFilter("NOTCHPAY"))
                    .build();
        }

    private ReactorClientHttpConnector reactorConnector(int connectTimeoutMs, int readTimeoutMs) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                );
        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeFilterFunction loggingFilter(String operatorName) {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("[{}] HTTP {} {}", operatorName, request.method(), request.url());
            return Mono.just(request);
        });
    }
}
