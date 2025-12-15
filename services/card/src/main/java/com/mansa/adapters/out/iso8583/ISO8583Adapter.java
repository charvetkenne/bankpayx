package com.mansa.adapters.out.iso8583;
import org.springframework.stereotype.Component;

/**
 * Skeleton adapter. In production use j8583 library and real TCP comms.
 * Here we simulate network roundtrip and response mapping.
 */
@Component
public class ISO8583Adapter {
    public String sendAuthorization(String isoRequest) {
        // simulate latency & response
        try { Thread.sleep(20); } catch (InterruptedException ignored){}
        // return a simple simulated response
        return "ISO_RESPONSE_OK";
    }
}