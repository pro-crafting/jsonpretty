package com.pro_crafting.tools.jsonpretty;

import io.undertow.servlet.handlers.ServletRequestContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class RequestEntityLimitProvider implements ContainerRequestFilter {

    @ConfigProperty(name = "max-request-size")
    @Inject
    Long maxRequestSize;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ServletRequestContext.requireCurrent().getExchange().setMaxEntitySize(maxRequestSize);
    }

}