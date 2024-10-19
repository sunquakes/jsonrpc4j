package com.sunquakes.jsonrpc4j.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONUtilsTest {

    @Data
    @AllArgsConstructor
    class TestObject {
        private String name;
    }

    @Test
    void testToString() {
        assertEquals("{\"name\":\"sunquakes\"}", JSONUtils.toString(new TestObject("sunquakes")));
    }

    @Test
    void testToJavaObject() {
        TestObject testObject = JSONUtils.toJavaObject(TestObject.class, "{\"name\":\"sunquakes\"}");
        assertEquals("sunquakes", testObject.name);
    }

    @Test
    void testToBytes() {
        byte[] bytes = JSONUtils.toBytes(new TestObject("sunquakes"));
        assertEquals(20, bytes.length);
    }

    @Test
    void testParseJavaObject() {
        TestObject testObject = JSONUtils.parseJavaObject("{\"name\":\"sunquakes\"}", TestObject.class);
        assertEquals("sunquakes", testObject.name);
    }

    @Test
    void testParseJSONObject() {
        Object object = JSONUtils.parseJSONObject("{\"name\":\"sunquakes\"}");
        assertEquals("sunquakes", JSONUtils.get(object, "name"));
    }

    @Test
    void testParseList() {
        List<TestObject> testObjectList = JSONUtils.parseList("[{\"name\":\"sunquakes\"}]", TestObject.class);
        assertEquals("sunquakes", testObjectList.get(0).name);
    }

    @Test
    void testParse() {
        Object object = JSONUtils.parse("{\"name\":\"sunquakes\"}");
        assertEquals("sunquakes", JSONUtils.get(object, "name"));
    }

    @Test
    void testIsArray() {
        assertEquals(true, JSONUtils.isArray(JSONUtils.parse("[]")));
    }

    @Test
    void testIsObject() {
        assertEquals(true, JSONUtils.isObject(JSONUtils.parse("{}")));
    }

    @Test
    void testToList() {
        List<Object> objects = JSONUtils.toList(JSONUtils.parse("[{\"name\":\"sunquakes\"}]"), (index, object) -> object);
        assertEquals("{\"name\":\"sunquakes\"}", objects.get(0).toString());
    }

    @Test
    void testContainsKey() {
        assertEquals(true, JSONUtils.containsKey(JSONUtils.parse("{\"name\":\"sunquakes\"}"), "name"));
    }

    @Test
    void testToArray() {
        Object[] objects = JSONUtils.toArray(JSONUtils.parse("[\"sunquakes\"]"), 1, (index, object) -> object);
        assertEquals("sunquakes", objects[0]);
    }

    @Test
    void testGet() {
        assertEquals("sunquakes", JSONUtils.get(JSONUtils.parse("{\"name\":\"sunquakes\"}"), "name"));
    }

    @Test
    void testToArray2() {
        Object[] objects = JSONUtils.toArray(JSONUtils.parse("[\"sunquakes\"]"));
        assertEquals("sunquakes", objects[0]);
    }
}
