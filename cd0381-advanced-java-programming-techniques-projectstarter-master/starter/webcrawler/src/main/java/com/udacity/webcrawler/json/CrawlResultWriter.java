package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes CrawlResult to JSON.
 */
public final class CrawlResultWriter {

    private final CrawlResult result;

    public CrawlResultWriter(CrawlResult result) {
        this.result = result;
    }

    public void write(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            write(writer);
        }
    }

    public void write(Writer writer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, result);
    }
}
