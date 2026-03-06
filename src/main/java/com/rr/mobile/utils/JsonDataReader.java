package com.rr.mobile.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rr.mobile.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Static utility for reading JSON test-data files from
 * {@code src/test/resources/testdata/}.
 *
 * Usage example:
 * <pre>
 *   Map<String, String> creds = JsonDataReader.getDataSet("testdata.json", "validUser");
 *   String username = creds.get("username");
 * </pre>
 */
public final class JsonDataReader {

    private static final Logger log = LogManager.getLogger(JsonDataReader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonDataReader() {}

    // ---------------------------------------------------------------
    // Core file reader
    // ---------------------------------------------------------------

    /**
     * Parses a JSON file and returns the root {@link JsonNode}.
     *
     * @param fileName relative name inside the testdata directory, e.g. {@code "testdata.json"}
     */
    public static JsonNode readFile(String fileName) {
        String path = FrameworkConfig.TEST_DATA_DIR + fileName;
        try {
            log.debug("Reading test data: {}", path);
            return MAPPER.readTree(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read test data file: " + path, e);
        }
    }

    // ---------------------------------------------------------------
    // Convenience accessors
    // ---------------------------------------------------------------

    /**
     * Returns a flat {@code Map<String, String>} for a named object node.
     *
     * @param fileName JSON file name
     * @param dataKey  top-level key whose value is a JSON object
     */
    public static Map<String, String> getDataSet(String fileName, String dataKey) {
        JsonNode node = requireNode(readFile(fileName), dataKey);
        Map<String, String> map = new HashMap<>();
        node.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().asText()));
        return map;
    }

    /**
     * Returns a single string value from a named object node.
     *
     * @param fileName JSON file name
     * @param dataKey  top-level object key
     * @param field    field within that object
     */
    public static String getValue(String fileName, String dataKey, String field) {
        return getDataSet(fileName, dataKey).get(field);
    }

    /**
     * Returns a list of flat maps from a named JSON array node.
     *
     * @param fileName JSON file name
     * @param dataKey  top-level key whose value is a JSON array of objects
     */
    public static List<Map<String, String>> getDataList(String fileName, String dataKey) {
        JsonNode arrayNode = requireNode(readFile(fileName), dataKey);
        if (!arrayNode.isArray()) {
            throw new RuntimeException(
                "Expected a JSON array for key '" + dataKey + "' in " + fileName);
        }
        List<Map<String, String>> list = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            Map<String, String> map = new HashMap<>();
            item.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().asText()));
            list.add(map);
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private static JsonNode requireNode(JsonNode root, String key) {
        JsonNode node = root.get(key);
        if (node == null) {
            throw new RuntimeException("Key '" + key + "' not found in JSON.");
        }
        return node;
    }
}
