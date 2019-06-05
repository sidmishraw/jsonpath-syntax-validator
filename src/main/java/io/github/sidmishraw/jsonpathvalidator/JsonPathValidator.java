package io.github.sidmishraw.jsonpathvalidator;

/**
 * Interface for validating the syntax of JSON path expressions.
 *
 * @author Sidharth Mishra
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JsonPathValidator {

    /**
     * Given the JSON path expression, validates the syntax of the expression.
     *
     * @param jsonPathExpression the JSON path expression
     * @return true if the path expression is a valid JSON path, else false.
     */
    public boolean validate(String jsonPathExpression);

    /**
     * Given the JSON path expression and the JSON object on which the path needs to be evaluated, verifies if the JSON path expression is a valid one.
     *
     * @param jsonPathExpression the JSON path expression
     * @param object             the JSON object –– as a string
     * @return true if the path expression is a valid JSON path expression in the context of the JSON object, else false.
     */
    public boolean validate(String jsonPathExpression, String object);
}
