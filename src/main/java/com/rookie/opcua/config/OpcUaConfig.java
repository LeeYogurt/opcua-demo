package com.rookie.opcua.config;

import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * @author yugo
 */
@Component
@ConfigurationProperties(prefix = "opcua")
@Data
public class OpcUaConfig {

    private String endpointUrl;

    public Predicate<EndpointDescription> endpointFilter() {
        return e -> true;
    }

}
