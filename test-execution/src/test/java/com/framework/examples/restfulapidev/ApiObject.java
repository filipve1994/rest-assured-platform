package com.framework.examples.restfulapidev;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Maps the resource shape used by https://api.restful-api.dev/objects.
 * "data" is a free-form bag of attributes (price, year, color, ...).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiObject {
    private String id;
    private String name;
    private Map<String, Object> data;
    private String createdAt;
}
