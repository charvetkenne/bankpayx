package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private Instant             timestamp;
    private int                 status;
    private String              error;
    private String              message;
    private String              path;
    private Map<String, String> fieldErrors;
}
