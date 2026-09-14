package com.lesofn.archforge.server.admin.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Merges the live OpenAPI exports of server-admin and server-web into the single committed
 * snapshot {@code spec/openapi.yaml}.
 *
 * <p>
 * Invoked by the {@code generateOpenApi} Gradle task:
 *
 * <pre>
 * {@code java OpenApiSpecWriter <admin.json> <web.json> <out.yaml>}
 * </pre>
 *
 * <p>
 * Merge rules:
 *
 * <ul>
 * <li>{@code paths} — union; a path present in both exports aborts the merge (ownership
 * boundary violated: admin owns /admin,/auth — web owns /web).
 * <li>{@code components.schemas} — union; same-named entries that differ get the web entry
 * renamed to {@code Web<Name>} with every {@code $ref} in the web document rewritten.
 * Other component subsections are unioned (admin wins on conflict).
 * <li>{@code tags} — union by name.
 * <li>{@code info} — taken from the admin export; {@code servers} is replaced by a single
 * relative root (the live exports carry random Testcontainers ports).
 * </ul>
 *
 * <p>
 * Output is sorted by key at every level so regeneration is byte-stable.
 */
public final class OpenApiSpecWriter {

    private static final String REF_PREFIX = "#/components/schemas/";

    private OpenApiSpecWriter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("usage: OpenApiSpecWriter <admin.json> <web.json> <out.yaml>");
        }
        ObjectMapper json = new ObjectMapper();
        ObjectNode admin = (ObjectNode) json.readTree(Path.of(args[0]).toFile());
        ObjectNode web = (ObjectNode) json.readTree(Path.of(args[1]).toFile());
        writeYaml(merge(admin, web), Path.of(args[2]));
    }

    static ObjectNode merge(ObjectNode admin, ObjectNode web) {
        Map<String, String> renames = schemaRenames(admin, web);
        if (!renames.isEmpty()) {
            rewriteRefs(web, renames);
        }
        ObjectNode out = admin.deepCopy();
        ArrayNode servers = JsonNodeFactory.instance.arrayNode();
        servers.addObject().put("url", "/");
        out.set("servers", servers);
        out.set("paths", mergePaths(admin, web));
        out.set("components", mergeComponents(admin, web, renames));
        out.set("tags", mergeTags(admin, web));
        dedupeOperationIds(out);
        return out;
    }

    /**
     * operationId must be unique across the merged document (both servers declare e.g.
     * {@code login}). On collision the later-seen op is renamed to
     * {@code <firstPathSegment><OperationId>} (e.g. {@code webLogin}); if still taken, a
     * numeric suffix is appended.
     */
    private static void dedupeOperationIds(ObjectNode doc) {
        Map<String, Integer> seen = new HashMap<>();
        doc.required("paths")
                .properties()
                .forEach(pathEntry -> {
                    if (!(pathEntry.getValue()instanceof ObjectNode pathItem)) {
                        return;
                    }
                    String stripped = pathEntry.getKey();
                    while (stripped.startsWith("/")) {
                        stripped = stripped.substring(1);
                    }
                    int slash = stripped.indexOf('/');
                    String segment = slash < 0 ? stripped : stripped.substring(0, slash);
                    pathItem.properties().forEach(opEntry -> {
                        if (!(opEntry.getValue()instanceof ObjectNode op)) {
                            return;
                        }
                        JsonNode idNode = op.get("operationId");
                        if (idNode == null || !idNode.isTextual()) {
                            return;
                        }
                        String id = idNode.asText();
                        if (!seen.containsKey(id)) {
                            seen.put(id, 1);
                            return;
                        }
                        String renamed = segment + Character.toUpperCase(id.charAt(0)) + id.substring(1);
                        while (seen.containsKey(renamed)) {
                            renamed = renamed + "2";
                        }
                        seen.put(renamed, 1);
                        op.put("operationId", renamed);
                    });
                });
    }

    /** Web schemas colliding with a different admin schema of the same name get renamed. */
    private static Map<String, String> schemaRenames(ObjectNode admin, ObjectNode web) {
        Map<String, JsonNode> adminSchemas = new HashMap<>();
        fieldsOf(admin, "components", "schemas")
                .forEach(e -> adminSchemas.put(e.getKey(), e.getValue()));

        Map<String, String> renames = new HashMap<>();
        Map<String, JsonNode> taken = new TreeMap<>(adminSchemas);
        fieldsOf(web, "components", "schemas")
                .forEach(e -> {
                    JsonNode existing = adminSchemas.get(e.getKey());
                    if (existing != null && !existing.equals(e.getValue())) {
                        String renamed = uniqueName("Web" + e.getKey(), taken);
                        renames.put(e.getKey(), renamed);
                        taken.put(renamed, e.getValue());
                    } else {
                        taken.putIfAbsent(e.getKey(), e.getValue());
                    }
                });
        return renames;
    }

    private static ObjectNode mergePaths(ObjectNode admin, ObjectNode web) {
        Map<String, JsonNode> paths = new TreeMap<>();
        admin.required("paths").properties().forEach(e -> paths.put(e.getKey(), e.getValue()));
        web.required("paths")
                .properties()
                .forEach(e -> {
                    JsonNode existing = paths.get(e.getKey());
                    if (existing == null) {
                        paths.put(e.getKey(), e.getValue());
                    } else if (!existing.equals(e.getValue())) {
                        // Shared infrastructure endpoints (e.g. /idempotent/token) exist on
                        // both servers with minor differences (media-type inference etc.).
                        // The admin definition wins; the endpoint is identical for clients.
                        System.err.println(
                                "warn: path defined by both servers, keeping server-admin's: " + e.getKey());
                    }
                });
        return toObjectNode(paths);
    }

    private static ObjectNode mergeComponents(
            ObjectNode admin, ObjectNode web, Map<String, String> renames) {
        ObjectNode out = admin.has("components")
                ? admin.required("components").deepCopy()
                : JsonNodeFactory.instance.objectNode();
        JsonNode webComponents = web.get("components");

        Map<String, JsonNode> schemas = new TreeMap<>();
        fieldsOf(out, "schemas").forEach(e -> schemas.put(e.getKey(), e.getValue()));
        if (webComponents instanceof ObjectNode wc) {
            fieldsOf(wc, "schemas")
                    .forEach(e -> schemas.put(renames.getOrDefault(e.getKey(), e.getKey()), e.getValue()));
            wc.properties().forEach(e -> {
                if ("schemas".equals(e.getKey())) {
                    return;
                }
                if (e.getValue()instanceof ObjectNode sub) {
                    ObjectNode target = (ObjectNode) out.withObjectProperty(e.getKey());
                    sub.properties().forEach(s -> target.putIfAbsent(s.getKey(), s.getValue()));
                } else {
                    out.putIfAbsent(e.getKey(), e.getValue());
                }
            });
        }
        out.set("schemas", toObjectNode(schemas));
        return out;
    }

    private static ArrayNode mergeTags(ObjectNode admin, ObjectNode web) {
        Map<String, JsonNode> tags = new TreeMap<>();
        collectTags(admin, tags);
        collectTags(web, tags);
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        tags.values().forEach(out::add);
        return out;
    }

    private static void collectTags(ObjectNode doc, Map<String, JsonNode> into) {
        JsonNode tags = doc.get("tags");
        if (tags == null || !tags.isArray()) {
            return;
        }
        tags.forEach(t -> {
            JsonNode name = t.get("name");
            if (name != null) {
                into.putIfAbsent(name.asText(), t);
            }
        });
    }

    private static List<Map.Entry<String, JsonNode>> fieldsOf(ObjectNode node, String field) {
        List<Map.Entry<String, JsonNode>> out = new ArrayList<>();
        JsonNode sub = node.get(field);
        if (sub != null && sub.isObject()) {
            sub.properties().forEach(out::add);
        }
        return out;
    }

    private static List<Map.Entry<String, JsonNode>> fieldsOf(
            ObjectNode node, String parent, String field) {
        JsonNode parentNode = node.get(parent);
        if (parentNode instanceof ObjectNode obj) {
            return fieldsOf(obj, field);
        }
        return List.of();
    }

    private static String uniqueName(String base, Map<String, JsonNode> existing) {
        String candidate = base;
        int i = 2;
        while (existing.containsKey(candidate)) {
            candidate = base + i;
            i++;
        }
        return candidate;
    }

    /** Rewrites {@code $ref: #/components/schemas/<old>} throughout the document. */
    private static void rewriteRefs(JsonNode node, Map<String, String> renames) {
        if (node instanceof ObjectNode obj) {
            obj.properties().forEach(e -> {
                if ("$ref".equals(e.getKey()) && e.getValue().isTextual()) {
                    String ref = e.getValue().asText();
                    if (ref.startsWith(REF_PREFIX)) {
                        String renamed = renames.get(ref.substring(REF_PREFIX.length()));
                        if (renamed != null) {
                            obj.put("$ref", REF_PREFIX + renamed);
                        }
                    }
                } else {
                    rewriteRefs(e.getValue(), renames);
                }
            });
        } else if (node instanceof ArrayNode arr) {
            arr.forEach(child -> rewriteRefs(child, renames));
        }
    }

    private static ObjectNode toObjectNode(Map<String, JsonNode> sorted) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        sorted.forEach(node::set);
        return node;
    }

    private static void writeYaml(ObjectNode merged, Path out) throws IOException {
        YAMLFactory factory = YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();
        YAMLMapper yaml = YAMLMapper.builder(factory)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        Files.createDirectories(
                java.util.Objects.requireNonNull(out.getParent(), "output dir"));
        yaml.writeValue(out.toFile(), merged);
    }
}
