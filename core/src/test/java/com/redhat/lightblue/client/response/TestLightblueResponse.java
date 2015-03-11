package com.redhat.lightblue.client.response;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestLightblueResponse {

    LightblueResponse testResponse = new LightblueResponse();

    private static final String initialResponseText = "{\"name\":\"value\"}";
    private static final String updatedResponseText = "{\"name\":\"value\"}";

    @Before
    public void setUp() throws Exception {
        testResponse = new LightblueResponse(initialResponseText);
    }

    @Test
    public void testGetText() {
        Assert.assertEquals(initialResponseText, testResponse.getText());
    }

    @Test
    public void testSetText() {
        testResponse.setText(updatedResponseText);
        Assert.assertEquals(updatedResponseText, testResponse.getText());
    }

    @Test
    public void testSetJson() throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(updatedResponseText);

        testResponse.setJson(node);
        Assert.assertEquals(node, testResponse.getJson());
    }

    @Test
    public void testHasError_NoErrorElement_False() {
        assertFalse(testResponse.hasError());
    }

    @Test
    public void testHasError_False() {
        LightblueResponse response = new LightblueResponse("{\"status\":\"successful\"}");
        assertFalse(response.hasError());
    }

    @Test
    public void testHasError_True() {
        LightblueResponse response = new LightblueResponse("{\"status\":\"error\"}");
        assertTrue(response.hasError());
    }

    @Test
    public void testParseProcessed_EmptyProcessed_ForArrayGeneric() throws LightblueResponseParseException {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 0, \"processed\": [], \"status\": \"COMPLETE\"}");

        Object[] results = response.parseProcessed(Object[].class);

        Assert.assertEquals(0, results.length);
    }

    @Test
    public void testParseProcessed_EmptyProcessed_ForSimpleGeneric() throws LightblueResponseParseException {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 0, \"processed\": [], \"status\": \"COMPLETE\"}");

        Object results = response.parseProcessed(Object.class);

        Assert.assertNull(results);
    }

    @Test
    public void testParseProcessed() throws LightblueResponseParseException {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 1, \"modifiedCount\": 0, \"processed\": [{\"_id\": \"idhash\", \"field\":\"value\"}], \"status\": \"COMPLETE\"}");

        SimpleModelObject[] results = response.parseProcessed(SimpleModelObject[].class);

        Assert.assertEquals(1, results.length);

        Assert.assertTrue(new SimpleModelObject("idhash", "value").equals(results[0]));
    }

    @Test
    public void testParseProcessed_NullProcessedNode() throws LightblueResponseParseException {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 0, \"processed\": null, \"status\": \"COMPLETE\"}");

        Object results = response.parseProcessed(Object.class);

        Assert.assertNull(results);
    }

    @Test(expected = LightblueResponseParseException.class)
    public void testParseProcessedWithParsingError() throws LightblueResponseParseException {
        LightblueResponse response = new LightblueResponse("{\"context\": \"rest/FindCommand/esbMessage\", \"errorCode\": \"rest-crud:RestFindError\", \"msg\": \"java.lang.IllegalArgumentException: Cannot call method public static com.redhat.lightblue.crud.FindRequest com.redhat.lightblue.crud.FindRequest.fromJson(com.fasterxml.jackson.databind.node.ObjectNode)\", \"objectType\": \"error\"}");

        response.parseProcessed(Object[].class);
    }

    @Test
    public void testParseModifiedCount() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 5, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(5, response.parseModifiedCount());
    }

    @Test
    public void testParseModifiedCount_zeroValue() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseModifiedCount());
    }

    @Test
    public void testParseModifiedCount_notExist() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseModifiedCount());
    }

    @Test
    public void testParseModifiedCount_nullValue() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": null, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseModifiedCount());
    }

    @Test
    public void testParseMatchCount() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 5, \"modifiedCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(5, response.parseMatchCount());
    }

    @Test
    public void testParseMatchCount_zeroValue() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": 0, \"modifiedCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseMatchCount());
    }

    @Test
    public void testParseMatchCount_notExist() {
        LightblueResponse response = new LightblueResponse("{\"modifiedCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseMatchCount());
    }

    @Test
    public void testParseMatchCount_nullValue() {
        LightblueResponse response = new LightblueResponse("{\"matchCount\": null, \"modifiedCount\": 0, \"status\": \"COMPLETE\"}");
        Assert.assertEquals(0, response.parseMatchCount());
    }

    private static class SimpleModelObject {

        String _id = "", field = "";

        public SimpleModelObject() {}

        public SimpleModelObject(String _id, String field) {
            super();
            this._id = _id;
            this.field = field;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object obj) {
            SimpleModelObject o = (SimpleModelObject) obj;

            return _id.equals(o._id) && field.equals(o.field);
        }

    }

}
