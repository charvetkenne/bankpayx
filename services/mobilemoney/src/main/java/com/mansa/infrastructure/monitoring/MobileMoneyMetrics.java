package com.mansa.infrastructure.monitoring;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MobileMoneyMetrics {

    private static final String METRIC_PAYMENT_INITIATED = "mobilemoney.payment.initiated";
    private static final String METRIC_PAYMENT_SUCCEEDED = "mobilemoney.payment.succeeded";
    private static final String METRIC_PAYMENT_FAILED = "mobilemoney.payment.failed";
    private static final String METRIC_PAYMENT_CANCELLED = "mobilemoney.payment.cancelled";
    private static final String METRIC_CALLBACK_RECEIVED = "mobilemoney.callback.received";
    private static final String TAG_OPERATOR = "operator";
    private static final String TAG_FAILURE_CODE = "failureCode";

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counterCache = new ConcurrentHashMap<>();

    public MobileMoneyMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPaymentInitiated(String operatorCode) {
        getOrCreateCounter(METRIC_PAYMENT_INITIATED, TAG_OPERATOR, operatorCode).increment();
    }

    public void recordPaymentSucceeded(String operatorCode) {
        getOrCreateCounter(METRIC_PAYMENT_SUCCEEDED, TAG_OPERATOR, operatorCode).increment();
    }

    public void recordPaymentFailed(String operatorCode, String failureCode) {
        String key = METRIC_PAYMENT_FAILED + ":" + operatorCode + ":" + failureCode;
        counterCache.computeIfAbsent(key, k ->
                Counter.builder(METRIC_PAYMENT_FAILED)
                        .tag(TAG_OPERATOR, operatorCode)
                        .tag(TAG_FAILURE_CODE, failureCode != null ? failureCode : "UNKNOWN")
                        .description("Total mobile money payment failures")
                        .register(meterRegistry)
        ).increment();
    }

    public void recordPaymentCancelled(String operatorCode) {
        getOrCreateCounter(METRIC_PAYMENT_CANCELLED, TAG_OPERATOR, operatorCode).increment();
    }

    public void recordCallbackReceived(String operatorCode) {
        getOrCreateCounter(METRIC_CALLBACK_RECEIVED, TAG_OPERATOR, operatorCode).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operationName, String operatorCode) {
        sample.stop(Timer.builder("mobilemoney.operation.duration")
                .tag("operation", operationName)
                .tag(TAG_OPERATOR, operatorCode)
                .description("Mobile money operation duration")
                .register(meterRegistry));
    }

    private Counter getOrCreateCounter(String metricName, String tagKey, String tagValue) {
        String cacheKey = metricName + ":" + tagValue;
        return counterCache.computeIfAbsent(cacheKey, k ->
                Counter.builder(metricName)
                        .tag(tagKey, tagValue)
                        .description("Mobile money metric: " + metricName)
                        .register(meterRegistry)
        );
    }
}
