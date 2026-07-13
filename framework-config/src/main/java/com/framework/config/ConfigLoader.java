package com.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads application.yml (defaults) and application-{env}.yml (overrides),
 * deep-merges them, and resolves ${placeholder} tokens against:
 *   1. System properties (-Dfoo.bar=baz)
 *   2. Environment variables (FOO_BAR)
 *   3. Other values already present in the merged config tree
 *
 * Pure static utility - stateless, so it's not DI-managed itself. It's the
 * thing {@link EnvironmentConfig} (which IS DI-managed, see framework-di)
 * delegates to at construction time.
 */
@Slf4j
public final class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private ConfigLoader() {
    }

    public static String activeEnvironment() {
        String fromProp = System.getProperty("env");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp.trim();
        }
        String fromEnvVar = System.getenv("ENV_NAME");
        if (fromEnvVar != null && !fromEnvVar.isBlank()) {
            return fromEnvVar.trim();
        }
        return "dev";
    }

    public static JsonNode loadMergedConfig() {
        String env = activeEnvironment();
        log.info("Loading configuration for environment '{}'", env);

        JsonNode base = readYaml("application.yml");
        JsonNode overlay = readYaml("application-" + env + ".yml");

        JsonNode merged = deepMerge(base, overlay);
        return resolvePlaceholders(merged, merged);
    }

    private static JsonNode readYaml(String classpathResource) {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                log.warn("Config resource '{}' not found on classpath - skipping.", classpathResource);
                return YAML_MAPPER.createObjectNode();
            }
            return YAML_MAPPER.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config file: " + classpathResource, e);
        }
    }

    private static JsonNode deepMerge(JsonNode base, JsonNode overlay) {
        if (base == null || base.isMissingNode()) {
            return overlay;
        }
        if (overlay == null || overlay.isMissingNode()) {
            return base;
        }
        if (!(base instanceof ObjectNode baseObj) || !(overlay instanceof ObjectNode overlayObj)) {
            return overlay;
        }
        Iterator<String> fieldNames = overlayObj.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            JsonNode overlayValue = overlayObj.get(field);
            JsonNode baseValue = baseObj.get(field);
            if (baseValue != null && baseValue.isObject() && overlayValue.isObject()) {
                baseObj.set(field, deepMerge(baseValue, overlayValue));
            } else {
                baseObj.set(field, overlayValue);
            }
        }
        return baseObj;
    }

    private static JsonNode resolvePlaceholders(JsonNode node, JsonNode root) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode child = obj.get(field);
                if (child.isTextual()) {
                    obj.put(field, resolveString(child.asText(), root));
                } else {
                    resolvePlaceholders(child, root);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                resolvePlaceholders(item, root);
            }
        }
        return node;
    }

    private static String resolveString(String raw, JsonNode root) {
        Matcher matcher = PLACEHOLDER.matcher(raw);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = lookup(key, root);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement != null ? replacement : ""));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String lookup(String key, JsonNode root) {
        String sysProp = System.getProperty(key);
        if (sysProp != null) {
            return sysProp;
        }
        String envVarName = key.toUpperCase().replace('.', '_');
        String envVar = System.getenv(envVarName);
        if (envVar != null) {
            return envVar;
        }
        JsonNode current = root;
        for (String part : key.split("\\.")) {
            if (current == null) {
                break;
            }
            current = current.get(part);
        }
        if (current != null && current.isTextual()) {
            return current.asText();
        }
        log.warn("Unresolved placeholder '${{}}' - leaving as empty string", key);
        return null;
    }
}
