package io.github.sidmishraw.jsonpathvalidator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when the JSON path expression syntax is bad. Obtained when parsing.
 *
 * @author Sidharth Mishra
 * @version 1.0.0
 * @since 1.0.0
 */
class BadJsonPathSyntaxException extends Exception {

    /**
     * Creates the exception.
     *
     * @param traversedPath     the path that has already been traversed
     * @param location          the current path segment
     * @param remainingSegments the remaining path segments
     */
    public BadJsonPathSyntaxException(String traversedPath, String location, List<String> remainingSegments) {
        super(String.format("Already traversed Path = %s, current segment being evaluated = %s, and remaining segments = %s", traversedPath, location,
            remainingSegments.stream().collect(Collectors.joining(";"))));
    }
}
