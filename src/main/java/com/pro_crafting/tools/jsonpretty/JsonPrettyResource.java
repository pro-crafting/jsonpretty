package com.pro_crafting.tools.jsonpretty;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class JsonPrettyResource {
    private static final String FORM_JSON_PREFIX_REPLACEMENT = "     ";

    JsonPrettyService jsonPrettyService = new JsonPrettyService();

    public void init(@Observes Router router) {
        router.post("/jsonpretty").handler(rc -> {
            rc.response().setChunked(true).setStatusCode(200).putHeader("Content-Type", "text/html;charset=UTF-8");
            rc.response().write(Buffer.buffer("<pre>"));
            jsonPrettyService.handle(rc, (e) -> rc.response().write(Buffer.buffer("</pre>")), FORM_JSON_PREFIX_REPLACEMENT);
        });
    }
}
