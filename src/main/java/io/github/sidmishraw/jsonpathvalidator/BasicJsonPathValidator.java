package io.github.sidmishraw.jsonpathvalidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import lombok.extern.slf4j.Slf4j;

/**
 * A basic JSON path validator implementation.
 *
 * @author Sidharth Mishra
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class BasicJsonPathValidator implements JsonPathValidator {

    /** Holds the filter operations once they are substituted with a [#digit]. */
    private List<String> substitutes = new ArrayList<>();

    /** The valid path segments. If this list is empty then the JSON path expression is not valid. */
    private List<String> paths = new ArrayList<>();

    /**
     * Substitutes the filter expressions `[?(expression)]` with [#N] where N=0,1,2... so save them when normalizing the
     * JSON path expression while parsing the expression.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the JSON path expression with the filter operations substituted.
     */
    private String substituteFilterOperations(String jsonPathExpression) {
        final var filterOperatorPattern = Pattern.compile("[\\['](\\??\\(.*?\\))[\\]']");
        var matcher = filterOperatorPattern.matcher(jsonPathExpression);
        var i = 0;
        while (matcher.find(1)) {
            var match = matcher.group(1);
            substitutes.add(match);
            jsonPathExpression = matcher.replaceFirst("[#" + i + "]");
            matcher = filterOperatorPattern.matcher(jsonPathExpression);
            i++;
        }
        return jsonPathExpression;
    }

    /**
     * Replaces the dots(for member access) with semicolons when normalizing the JSON path expression. It also replaces the starting of `[` when using
     * index based member access with semicolons.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the JSON path expression with dots replaced with semicolons.
     */
    private String replaceMemberAccessorsWithSemicolons(String jsonPathExpression) {
        final var dotsPattern = Pattern.compile("'?\\.'?|\\['?");
        return jsonPathExpression.replaceAll(dotsPattern.pattern(), ";");
    }

    /**
     * Replaces the multiple semicolons with the spread operator (..) while normalizing JSON path expression.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the JSON path expression with consecutive semicolons with spread operator.
     */
    private String replaceMultipleSemicolonsWithSpreadOperator(String jsonPathExpression) {
        final var repeatedSemicolonPattern = Pattern.compile(";;;|;;");
        return jsonPathExpression.replaceAll(repeatedSemicolonPattern.pattern(), ";..;");
    }

    /**
     * Replaces the end semicolon, end of index based object access`']`, and end single quote with empty string, clearing them while normalizing the
     * JSON path expression.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the JSON path expression with ends replaced with empty string.
     */
    private String clearEnds(String jsonPathExpression) {
        final var endPattern = Pattern.compile(";$|'?\\]|'$");
        return jsonPathExpression.replaceAll(endPattern.pattern(), "");
    }

    /**
     * Restores the substituted filter expression from the {@link #substitutes} list.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the JSON path expression with filter operations restored.
     */
    private String restoreFilterOperations(String jsonPathExpression) {
        final var filterOperationSubstitutedPattern = Pattern.compile("#([0-9]+)");
        var matcher = filterOperationSubstitutedPattern.matcher(jsonPathExpression);
        while (matcher.find(1)) {
            var filterOperationIndex = Integer.parseInt(matcher.group(1));
            var filterOperationExpression = substitutes.get(filterOperationIndex);
            jsonPathExpression = matcher.replaceFirst(filterOperationExpression);
            matcher = filterOperationSubstitutedPattern.matcher(jsonPathExpression);
        }
        return jsonPathExpression;
    }

    /**
     * Normalizes the JSON path expression.
     *
     * @param jsonPathExpression the JSON path expression
     * @return the normalized JSON path expression.
     */
    private String normalize(String jsonPathExpression) {
        log.info("Before normalization = {}", jsonPathExpression);
        // @formatter:off
        jsonPathExpression =
            Stream.<Function<String, String>>of(
            this::substituteFilterOperations,
            this::replaceMemberAccessorsWithSemicolons,
            this::replaceMultipleSemicolonsWithSpreadOperator,
            this::clearEnds,
            this::restoreFilterOperations
            )
            .collect(Collectors.toList())
            .stream()
            .reduce(Function.identity(), (f, a) -> f.andThen(a))
            .apply(jsonPathExpression);
        // @formatter:on
        log.info("After normalization = {}", jsonPathExpression);
        return jsonPathExpression;
    }

    /**
     * Stores the valid path segments in {@link #paths}.
     *
     * @param path the path segment
     * @return true if path is not null or empty and was added to store, else false.
     */
    private boolean store(String path) {
        if (null != path && !path.isEmpty()) {
            paths.add(path);
            return true;
        }
        return false;
    }

    /**
     * Digits are replaced by index access.
     *
     * @param path the normalized JSON path expression
     * @return the JSON path replaced with digits of index access.
     */
    private String asPath(String path) {
        var x = path.split(";");
        var p = "$";
        for (int i = 1, n = x.length; i < n; i++)
            // @formatter:off
            p += x[i].matches("^[0-9*]+$")
                ? "[" + x[i] + "]"
                : "['" + x[i] +"']";
            // @formatter:on
        return p;
    }

    /**
     * Parses the JSON path expression. Depending upon the various path segment patterns, stores the path segments into {@link #paths}.
     *
     * @param jsonPathExpression the JSON path expression
     * @param path               the current path segment
     */
    private void trace(String jsonPathExpression, String path) {
        if (null != jsonPathExpression && !jsonPathExpression.isEmpty()) {
            var x = Stream.of(jsonPathExpression.split(";")).collect(Collectors.toList());
            var loc = x.remove(0);
            var exp = x.stream().collect(Collectors.joining(";"));
            // @formatter:off
            if (
                loc.equals("*") // all indices in array
                || loc.equals("..") // spread operator
                || loc.matches(",") // array
                || loc.matches("^\\(.*?\\)$") // group
                || loc.matches("^\\?\\(.*?\\)$") // filter operation
                || loc.matches("^(-?[0-9]*):(-?[0-9]*):?([0-9]*)$") // pythonic range splits
                || !loc.matches("^\".*\"$")
            )
            // @formatter:on
                trace(exp, path + ";" + loc);
        } else
            store(path);
    }

    @Override
    public boolean validate(String jsonPathExpression) {
        var normalizedJsonPathExpression = normalize(jsonPathExpression);
        trace(normalizedJsonPathExpression.replaceFirst("^\\$;", ""), "$");
        return !paths.isEmpty();
    }

    @Override
    public boolean validate(String jsonPathExpression, String object) {
        throw new RuntimeException(new NotImplementedException("Not supported yet!"));
    }
}
