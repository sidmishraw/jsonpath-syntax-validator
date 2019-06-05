package io.github.sidmishraw.jsonpathvalidator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests of JSON path expression syntax validator.
 *
 * @author Sidharth Mishra
 * @version 1.0.0
 * @since 1.0.0
 */
public class JsonPathValidatorTest {

    @Test
    public void testValidJsonPathExpressions() {
        JsonPathValidator validator = new BasicJsonPathValidator();
        Assert.assertTrue(validator.validate("$.as[?(@.name == 'samba')]"));
        Assert.assertTrue(validator.validate("$.as[?(@.name == 'samba')].value"));
        Assert.assertTrue(validator.validate("as[?(@.name == 'samba')].value"));
        Assert.assertTrue(validator.validate("a"));
        Assert.assertTrue(validator.validate("a[1:99:3]"));
        Assert.assertTrue(validator.validate("a[1:99:3]"));
        Assert.assertTrue(validator.validate("a.b[?(@.name === 'f')]['valid']"));
        Assert.assertTrue(validator.validate("a['kangaroo is going to punch you \"okay\"']"));
        Assert.assertTrue(validator.validate("as*"));
        Assert.assertTrue(validator.validate("as['*']"));
        Assert.assertTrue(validator.validate("as[*]"));
        Assert.assertTrue(validator.validate("$['as*']"));
        Assert.assertTrue(validator.validate("as..a"));
        Assert.assertTrue(validator.validate("as[1+2+3]"));
        Assert.assertTrue(validator.validate("as['1+2+3']"));
    }

    @Test
    public void testInvalidJsonPathExpressions() {
        JsonPathValidator validator = new BasicJsonPathValidator();
        Assert.assertFalse(validator.validate("as[??(@.name == 'a')].value"));
        Assert.assertFalse(validator.validate("a['??kangaroo   \"A\"'][??(@.name == 'a')].value"));
        Assert.assertFalse(validator.validate("as.*.[??($.name == @)].value"));
    }
}
