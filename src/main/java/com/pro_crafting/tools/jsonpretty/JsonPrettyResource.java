package com.pro_crafting.tools.jsonpretty;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * No Rest Api. This is just supposed as output medium for the prettifyed json.
 */
@ApplicationScoped
@Path("jsonpretty")
public class JsonPrettyResource {

    @Inject
    ObjectMapper mapper;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    @Counted(name = "prettifyCount", description = "How often a json was prettified")
    @Timed(name = "ttfbTimer", description = "A measure of how long it takes to return the first byte.", unit = MetricUnits.MILLISECONDS)
    public StreamingOutput prettify(InputStream json) {
        // The form is transmitted as text/plain enctype
        // in the form entryname=jsonstring
        // We have to filter out anything before the =,
        // so that jackson can read just the json string

        // Read the first 5 bytes
        // containing json=
        try {
            json.read(new byte[5], 0, 5);
        } catch (IOException ex) {
            return output -> output.write(asBytes("Unable to read json"));
        }

        JsonParser parser;
        try {
            parser = mapper.getFactory().createParser(json);
        } catch (IOException e) {
            return output -> output.write(asBytes("Invalid JSON, Reason: " + e.getMessage()));
        }

        return output -> {
            output.write(asBytes("<pre>"));

            try (JsonGenerator generator = mapper.getFactory().createGenerator(output, JsonEncoding.UTF8))
            {
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                while (parser.nextToken() != null) {
                    generator.copyCurrentEvent(parser);
                }
            } catch (Exception e) {
                output.write(asBytes("Invalid JSON, Reason: " + e.getMessage()));
            }

            parser.close();

            output.write(asBytes("</pre>"));
        };
    }

    private byte[] asBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
