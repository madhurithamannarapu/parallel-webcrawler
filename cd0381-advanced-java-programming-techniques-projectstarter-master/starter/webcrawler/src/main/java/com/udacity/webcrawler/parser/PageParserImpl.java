package com.udacity.webcrawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator.Tag;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An implementation of {@link PageParser} that works for both local and remote files.
 *
 * <p>HTML parsing is done using the JSoup library. This class is a thin adapter around JSoup's API,
 * since JSoup does not know how to correctly resolve relative hyperlinks when parsing HTML from
 * local files.
 */
final class PageParserImpl implements PageParser {

    /**
     * Matches whitespace characters.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * Matches non-word characters.
     */
    private static final Pattern NON_WORD_CHARACTERS = Pattern.compile("\\W");

    private final String uri;
    private final Duration timeout;
    private final List<Pattern> ignoredWords;

    /**
     * Constructs a page parser with the given parameters.
     *
     * @param uri          the URI of the file to parse.
     * @param timeout      the timeout to use when downloading the file, if it is remote.
     * @param ignoredWords patterns of which words should be ignored by the {@link #parse()} method.
     */
    PageParserImpl(String uri, Duration timeout, List<Pattern> ignoredWords) {
        this.uri = Objects.requireNonNull(uri, "uri must not be null");
        this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
        this.ignoredWords = Objects.requireNonNull(ignoredWords, "ignoredWords must not be null");
    }

    @Override
    public Result parse() {
        URI parsedUri;
        try {
            parsedUri = new URI(uri);
        } catch (URISyntaxException e) {
            // Invalid URI; return empty result
            return new Result.Builder().build();
        }

        Document document;
        try {
            document = parseDocument(parsedUri);
        } catch (Exception e) {
            // Handle exceptions like invalid URIs or unsupported mimetypes gracefully
            return new Result.Builder().build();
        }

        Result.Builder builder = new Result.Builder();

        // Traverse the document to gather all links and words
        document.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    processTextNode((TextNode) node, builder);
                    return;
                }
                if (!(node instanceof Element)) {
                    return;
                }
                processElementNode((Element) node, parsedUri, builder);
            }

            @Override
            public void tail(Node node, int depth) {
                // No action needed on tail
            }
        });

        return builder.build();
    }

    private void processTextNode(TextNode textNode, Result.Builder builder) {
        String text = textNode.text().strip();
        if (text.isEmpty()) {
            return;
        }

        Arrays.stream(WHITESPACE.split(text))
                .filter(s -> !s.isBlank())
                .filter(s -> ignoredWords.stream().noneMatch(p -> p.matcher(s).matches()))
                .map(s -> NON_WORD_CHARACTERS.matcher(s).replaceAll(""))
                .map(String::toLowerCase)
                .forEach(builder::addWord);
    }

    private void processElementNode(Element element, URI baseUri, Result.Builder builder) {
        if (!element.is(new Tag("a")) || !element.hasAttr("href")) {
            return;
        }

        if (isLocalFile(baseUri)) {
            // Manually construct absolute URL for local files
            Path basePath = Path.of(baseUri);
            Path linkPath = basePath.getParent().resolve(element.attr("href")).normalize();
            builder.addLink(linkPath.toUri().toString());
        } else {
            // Let Jsoup resolve absolute URL for remote resources
            builder.addLink(element.attr("abs:href"));
        }
    }

    /**
     * Parses a document from the provided {@link URI}, either local or remote.
     */
    private Document parseDocument(URI uri) throws IOException {
        if (!isLocalFile(uri)) {
            return Jsoup.parse(uri.toURL(), (int) timeout.toMillis());
        }

        // Workaround for Jsoup baseUri issues on local "file://" URIs:
        try (InputStream inputStream = Files.newInputStream(Path.of(uri))) {
            return Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");
        }
    }

    /**
     * Returns true if the given {@link URI} represents a local file.
     */
    private static boolean isLocalFile(URI uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }
}
