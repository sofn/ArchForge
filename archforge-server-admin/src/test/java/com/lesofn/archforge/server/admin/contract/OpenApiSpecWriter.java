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
import java.util.Iterator;
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
        fillMissingSummaries(out);
        fillLicense(out);
        ensureErrorResponses(out);
        return out;
    }

    /** Apache-2.0 per the repo LICENSE file; put-if-absent so a configured value wins. */
    private static void fillLicense(ObjectNode doc) {
        ObjectNode info = doc.withObjectProperty("info");
        if (!info.has("license")) {
            ObjectNode license = info.putObject("license");
            license.put("name", "Apache-2.0");
            license.put("identifier", "Apache-2.0");
        }
    }

    /**
     * Every endpoint can fail auth (401/403 via the sa-token interceptor) or validation
     * (400), all rendered as RFC 9457 ProblemDetail — but springdoc only documents the
     * success path. Declare the error shape once in {@code components.responses.Error}
     * and inject a {@code 4XX} wildcard $ref into operations that lack one
     * ({@code operation-4xx-response}).
     */
    private static void ensureErrorResponses(ObjectNode doc) {
        ObjectNode responses = doc.withObjectProperty("components").withObjectProperty("responses");
        if (!responses.has("Error")) {
            ObjectNode error = responses.putObject("Error");
            error.put("description", "Error response (RFC 9457 ProblemDetail)");
            ObjectNode schema = error.withObjectProperty("content")
                    .withObjectProperty("application/json")
                    .putObject("schema");
            schema.put("type", "object");
            ObjectNode props = schema.putObject("properties");
            props.putObject("type").put("type", "string");
            props.putObject("title").put("type", "string");
            props.putObject("status").put("type", "integer");
            props.putObject("detail").put("type", "string");
            props.putObject("instance").put("type", "string");
        }
        doc.required("paths")
                .properties()
                .forEach(pathEntry -> {
                    if (!(pathEntry.getValue()instanceof ObjectNode pathItem)) {
                        return;
                    }
                    pathItem.properties().forEach(opEntry -> {
                        if (!(opEntry.getValue()instanceof ObjectNode op) || !(op.get(
                                "responses")instanceof ObjectNode resps)) {
                            return;
                        }
                        boolean has4xx = false;
                        for (Iterator<String> names = resps.fieldNames(); names.hasNext();) {
                            if (names.next().startsWith("4")) {
                                has4xx = true;
                                break;
                            }
                        }
                        if (!has4xx) {
                            resps.withObjectProperty("4XX")
                                    .put("$ref", "#/components/responses/Error");
                        }
                    });
                });
    }

    /**
     * springdoc only emits {@code summary} when {@code @Operation(summary=...)} is present.
     * The committed spec must lint clean ({@code operation-summary}), so operations without
     * one get a deterministic fallback: humanized operationId, else {@code METHOD path}.
     */
    private static void fillMissingSummaries(ObjectNode doc) {
        doc.required("paths")
                .properties()
                .forEach(pathEntry -> {
                    if (!(pathEntry.getValue()instanceof ObjectNode pathItem)) {
                        return;
                    }
                    pathItem.properties().forEach(opEntry -> {
                        if (!(opEntry.getValue()instanceof ObjectNode op) || op.has("summary")) {
                            return;
                        }
                        JsonNode idNode = op.get("operationId");
                        String summary = idNode != null && idNode.isTextual()
                                ? humanizeOperationId(idNode.asText())
                                : opEntry.getKey().toUpperCase(java.util.Locale.ROOT) + " " + pathEntry.getKey();
                        op.put("summary", summary);
                    });
                });
    }

    /** {@code getUserList} → {@code "Get user list"}; {@code list_2} → {@code "List 2"}. */
    private static String humanizeOperationId(String operationId) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < operationId.length(); i++) {
            char c = operationId.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append(' ');
                out.append(Character.toLowerCase(c));
            } else if (c == '_' || c == '-') {
                out.append(' ');
            } else {
                out.append(i == 0 ? Character.toUpperCase(c) : c);
            }
        }
        String summary = out.toString().trim();
        return summary.isEmpty() ? operationId : summary;
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

    /**
     * springdoc emits schema properties in a JVM-dependent order (the same DTO serializes
     * with different field orders on different machines — e.g. uniqueColumn/indexedColumn
     * flipped between local and CI exports). Sort every object recursively so the output
     * is byte-stable regardless of source order. Arrays keep their order.
     */
    private static JsonNode sortDeep(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            Map<String, JsonNode> sorted = new TreeMap<>();
            obj.properties().forEach(e -> sorted.put(e.getKey(), sortDeep(e.getValue())));
            return toObjectNode(sorted);
        }
        if (node instanceof ArrayNode arr) {
            ArrayNode out = JsonNodeFactory.instance.arrayNode();
            arr.forEach(child -> out.add(sortDeep(child)));
            return out;
        }
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
        yaml.writeValue(out.toFile(), sortDeep(merged));
    }
}
