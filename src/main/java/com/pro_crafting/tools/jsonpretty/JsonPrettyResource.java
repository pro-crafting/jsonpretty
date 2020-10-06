package com.pro_crafting.tools.jsonpretty;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.parsetools.JsonEvent;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class JsonPrettyResource {
    private static final String FORM_JSON_PREFIX_REPLACEMENT = "     ";

    private static final JsonFactory factory = new JsonFactory();

    public void init(@Observes Router router) {
        router.post("/jsonpretty").handler(new JsonPrettyHandler());
    }

    public class JsonPrettyHandler implements Handler<RoutingContext> {

        @Override
        public void handle(RoutingContext rc) {
            rc.response().setChunked(true);
            HttpServerResponseOutputStream output = new HttpServerResponseOutputStream(rc.response());
            JsonGenerator generator;
            try {
                generator = factory.createGenerator(output, JsonEncoding.UTF8);
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
            } catch (IOException e) {
                rc.response().setStatusCode(400).end("Invalid JSON, Reason: " + e.getMessage());
                return;
            }
            JsonParser parser = JsonParser.newParser();
            rc.request().handler(new Handler<>() {
                boolean removedPrefix = false;

                @Override
                public void handle(Buffer buffer) {
                    if (!removedPrefix) {
                        // The form is transmitted as text/plain enctype
                        // in the form entryname=jsonstring
                        // We have to filter out anything before the =,
                        // so that jackson can read just the json string

                        // Read the first 5 bytes
                        // containing json=
                        //buffer.getString().getBytes(0, 5, new byte[5]);
                        buffer.setString(0, FORM_JSON_PREFIX_REPLACEMENT);
                        removedPrefix = true;
                    }
                    parser.handle(buffer);
                }
            });

            output.write(asBytes("<pre>"));
            parser.handler(new Handler<>() {
                boolean inArray = false;

                @Override
                public void handle(JsonEvent event) {
                    try {
                        switch (event.type()) {
                            case START_OBJECT:
                                if (event.fieldName() != null && !inArray) {
                                    generator.writeFieldName(event.fieldName());
                                }
                                inArray = false;
                                generator.writeStartObject();
                                break;
                            case END_OBJECT:
                                inArray = false;
                                generator.writeEndObject();
                                break;
                            case START_ARRAY:
                                inArray = true;
                                if (event.fieldName() != null) {
                                    generator.writeFieldName(event.fieldName());
                                }
                                generator.writeStartArray();
                                break;
                            case END_ARRAY:
                                inArray = false;
                                generator.writeEndArray();
                                break;
                            case VALUE:
                                if (event.fieldName() != null && !inArray) {
                                    generator.writeFieldName(event.fieldName());
                                }
                                boolean wroteAnything = true;
                                if (event.isString()) {
                                    generator.writeString(event.stringValue());
                                } else if (event.isBoolean()) {
                                    generator.writeBoolean(event.booleanValue());
                                } else if (event.isNull()) {
                                    generator.writeNull();
                                } else if (event.isNumber()) {
                                    generator.writeNumber(event.doubleValue());
                                } else {
                                    wroteAnything = false;
                                }
                                if (event.fieldName() != null && inArray && !wroteAnything) {
                                    generator.writeFieldName(event.fieldName());
                                }
                                break;
                        }
                    } catch (Exception e) {
                        output.write(asBytes("Invalid JSON, Reason: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }
            });

            rc.request().endHandler(e -> {
                try {
                    generator.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                parser.end();
                output.write(asBytes("</pre>"));

                rc.response().setStatusCode(200).end();
            });
        }
    }

    private byte[] asBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static class HttpServerResponseOutputStream extends OutputStream {

        private HttpServerResponse response;

        public HttpServerResponseOutputStream(HttpServerResponse response) {
            this.response = response;
        }

        @Override
        public void write(int b) {
            response.write(Buffer.buffer(new byte[]{(byte) b}));
        }

        @Override
        public void write(byte[] b)  {
            response.write(Buffer.buffer(b));
        }

        @Override
        public void write(byte[] b, int off, int len)  {
            response.write(Buffer.buffer(b));
        }
    }
}
