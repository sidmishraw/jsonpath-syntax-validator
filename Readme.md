# JSON Path expression syntax validation

This project uses the JSON Path expression parsing logic mentioned in [Stefan Goessner's article](https://goessner.net/articles/JsonPath/).
I make use of parts of the code from original JSON path implementation in JS found [here](https://code.google.com/archive/p/jsonpath/).

## Usage

```java
class Example {
    public static void main(String[] args) {
        JsonPathValidator validator = new BasicJsonPathValidator();
        Assert.assertTrue(validator.validate("$.as[?(@.name == 'samba')]"));
        Assert.assertFalse(validator.validate("a['??kangaroo   \"A\"'][??(@.name == 'a')].value"));        
    } 
}
```

`JsonPathValidator` is an interface that has a basic implementation `BasicJsonPathValidator`. If required other sophisticated validators can be added to this library.

> Note: This library doesn't really evaluate the path against a JSON object, this is strictly pre-evaluation phase syntax check.

## Dependencies
- Project Lombok
- Maven
- Slf4j
- Logback

## References
[1] https://goessner.net/articles/JsonPath/

[2] https://code.google.com/archive/p/jsonpath/