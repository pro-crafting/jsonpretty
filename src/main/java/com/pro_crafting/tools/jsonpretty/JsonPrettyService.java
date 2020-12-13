package com.pro_crafting.tools.jsonpretty;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.JsonEvent;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

public class JsonPrettyService {
    private static final JsonFactory factory = new JsonFactory();

    public void handle(RoutingContext rc, Handler<Void> endHandler, String jsonPrefix) {
        HttpServerResponseOutputStream output = new HttpServerResponseOutputStream(rc.response());
        JsonGenerator generator;
        try {
            generator = factory.createGenerator(output, JsonEncoding.UTF8);
            generator.useDefaultPrettyPrinter();
        } catch (IOException e) {
            rc.response().end("Invalid JSON, Reason: " + e.getMessage());
            return;
        }
        JsonParser parser = JsonParser.newParser();
        parser.exceptionHandler(e -> {
            rc.response().end("Invalid JSON, Reason: " + e.getMessage());
        });
        rc.request().exceptionHandler(e -> {
            rc.response().end("Invalid JSON, Reason: " + e.getMessage());
        });
        rc.request().handler(new Handler<>() {
            boolean removedPrefix = false;

            @Override
            public void handle(Buffer buffer) {
                if (!removedPrefix && jsonPrefix != null) {
                    // The form is transmitted as text/plain enctype
                    // in the form entryname=jsonstring
                    // We have to filter out anything before the =,
                    // so that jackson can read just the json string

                    // Read the first 5 bytes
                    // containing json=
                    //buffer.getString().getBytes(0, 5, new byte[5]);
                    buffer.setString(0, jsonPrefix);
                    removedPrefix = true;
                }
                parser.handle(buffer);
            }
        });

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
                            } else if (event.value() instanceof Double) {
                                generator.writeNumber(event.doubleValue());
                            } else if (event.value() instanceof Float) {
                                generator.writeNumber(event.floatValue());
                            } else if (event.value() instanceof Integer) {
                                generator.writeNumber(event.integerValue());
                            } else if (event.value() instanceof Long) {
                                generator.writeNumber(event.longValue());
                            } else {
                                wroteAnything = false;
                            }
                            if (event.fieldName() != null && inArray && !wroteAnything) {
                                generator.writeFieldName(event.fieldName());
                            }
                            break;
                    }
                } catch (IOException e) {
                    rc.response().write(Buffer.buffer("Invalid JSON, Reason: " + e.getMessage()));
                }
            }
        });

        rc.request().endHandler(e -> {
            try {
                generator.close();
            } catch (IOException ex) {
                rc.response().write(Buffer.buffer("Invalid JSON, Reason: " + ex.getMessage()));
            }
            parser.end();
            endHandler.handle(e);

            rc.response().end();
        });
    }
}
