package com.lesofn.archforge.common.utils.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableSet;
import com.lesofn.archforge.common.sensitive.jackson.SensitiveJacksonModule;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.MapType;

/**
 * Jackson工具类 优势： 数据量高于百万的时候，速度和FastJson相差极小 API和注解支持最完善，可定制性最强 支持的数据源最广泛（字符串，对象，文件、流、URL）
 *
 * @author sofn
 */
@Slf4j
public class JsonUtil {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static ObjectMapper mapper;

    private static final Set<JsonReadFeature> JSON_READ_FEATURES_ENABLED = ImmutableSet.of(
            // 允许在JSON中使用Java注释
            JsonReadFeature.ALLOW_JAVA_COMMENTS,
            // 允许 json 存在没用双引号括起来的 field
            JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES,
            // 允许 json 存在使用单引号括起来的 field
            JsonReadFeature.ALLOW_SINGLE_QUOTES,
            // 允许 json 存在没用引号括起来的 ascii 控制字符
            JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
            // 允许 json number 类型的数存在前导 0 (例: 0001)
            JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS,
            // 允许 json 存在 NaN, INF, -INF 作为 number 类型
            JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS,
            // 允许 只有Key没有Value的情况
            JsonReadFeature.ALLOW_MISSING_VALUES,
            // 允许数组json的结尾多逗号
            JsonReadFeature.ALLOW_TRAILING_COMMA);

    static {
        try {
            // 初始化
            mapper = initMapper();
        } catch (Exception e) {
            log.error("jackson config error", e);
        }
    }

    private JsonUtil() {
        throw new IllegalStateException("Utility class JsonUtil can not be instantiated");
    }

    public static ObjectMapper initMapper() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        SimpleModule javaTimeModule = new SimpleModule();
        javaTimeModule
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                .addDeserializer(
                        LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule
                .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        return JsonMapper.builder()
                .enable(JSON_READ_FEATURES_ENABLED.toArray(new JsonReadFeature[0]))
                .enable(StreamWriteFeature.IGNORE_UNKNOWN, StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .defaultDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT))
                .changeDefaultPropertyInclusion(
                        v -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .addModule(javaTimeModule)
                .addModule(new SensitiveJacksonModule())
                .build();
    }

    public static ObjectMapper getObjectMapper() { return mapper; }

    /** JSON反序列化 */
    public static <V> V from(URL url, Class<V> type) {
        try {
            return mapper.readValue(url.openStream(), type);
        } catch (Exception e) {
            throw new JacksonException(String.format(
                    "jackson from error, url: %s, type: %s", url.getPath(), type.getName()), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(URL url, TypeReference<V> type) {
        try {
            return mapper.readValue(url.openStream(), type);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, url: %s, type: %s", url.getPath(), type), e);
        }
    }

    /** JSON反序列化（List） */
    public static <V> List<V> fromList(URL url, Class<V> type) {
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(url.openStream(), collectionType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, url: %s, type: %s", url.getPath(), type), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(InputStream inputStream, Class<V> type) {
        try {
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, type: %s", type.getName()), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(InputStream inputStream, TypeReference<V> type) {
        try {
            return mapper.readValue(inputStream, type);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, type: %s", type.getType().getTypeName()), e);
        }
    }

    /** JSON反序列化（List） */
    public static <V> List<V> fromList(InputStream inputStream, Class<V> type) {
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(inputStream, collectionType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, type: %s", type.getName()), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(File file, Class<V> type) {
        try {
            return mapper.readValue(file, type);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, url: %s, type: %s", file.getPath(), type), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(File file, TypeReference<V> type) {
        try {
            return mapper.readValue(file, type);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, url: %s, type: %s", file.getPath(), type), e);
        }
    }

    /** JSON反序列化（List） */
    public static <V> List<V> fromList(File file, Class<V> type) {
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(file, collectionType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, url: %s, type: %s", file.getPath(), type), e);
        }
    }

    /** JSON反序列化 */
    public static <V> V from(String json, Type type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, json: %s, type: %s", json, type), e);
        }
    }

    /** JSON反序列化（List） */
    public static <V> List<V> fromList(String json, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(json, collectionType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, json: %s, type: %s", json, type), e);
        }
    }

    /** JSON反序列化（Map） */
    public static Map<String, Object> fromMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyMap();
        }
        try {
            MapType mapType = mapper.getTypeFactory()
                    .constructMapType(HashMap.class, String.class, Object.class);
            return mapper.readValue(json, mapType);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson from error, json: %s", json), e);
        }
    }

    /** 序列化为JSON */
    public static <V> String to(List<V> list) {
        try {
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson to error, data: %s", list), e);
        }
    }

    /** 序列化为JSON */
    public static <V> String to(V v) {
        try {
            return mapper.writeValueAsString(v);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson to error, data: %s", v), e);
        }
    }

    /** 序列化为JSON */
    public static <V> void toFile(String path, List<V> list) {
        try (Writer writer = new FileWriter(path, true)) {
            mapper.writer().writeValues(writer).writeAll(list);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson to file error, path: %s, list: %s", path, list), e);
        }
    }

    /** 序列化为JSON */
    public static <V> void toFile(String path, V v) {
        try (Writer writer = new FileWriter(path, true)) {
            mapper.writer().writeValues(writer).write(v);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson to file error, path: %s, data: %s", path, v), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return String，默认为 null
     */
    public static String getAsString(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return null;
            }
            return getAsString(jsonNode);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get string error, json: %s, key: %s", json, key), e);
        }
    }

    private static String getAsString(JsonNode jsonNode) {
        return jsonNode.asString();
    }

    /**
     * 从json串中获取某个字段
     *
     * @return int，默认为 0
     */
    public static int getAsInt(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return 0;
            }
            return jsonNode.isInt() ? jsonNode.intValue() : Integer.parseInt(getAsString(jsonNode));
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get int error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return long，默认为 0
     */
    public static long getAsLong(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0L;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return 0L;
            }
            return jsonNode.isLong() ? jsonNode.longValue() : Long.parseLong(getAsString(jsonNode));
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get long error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return double，默认为 0.0
     */
    public static double getAsDouble(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0.0;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return 0.0;
            }
            return jsonNode.isDouble()
                    ? jsonNode.doubleValue()
                    : Double.parseDouble(getAsString(jsonNode));
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get double error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return BigInteger，默认为 0.0
     */
    public static BigInteger getAsBigInteger(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new BigInteger(String.valueOf(0.00));
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return new BigInteger(String.valueOf(0.00));
            }
            return jsonNode.isBigInteger()
                    ? jsonNode.bigIntegerValue()
                    : new BigInteger(getAsString(jsonNode));
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get big integer error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return BigDecimal，默认为 0.00
     */
    public static BigDecimal getAsBigDecimal(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new BigDecimal("0.00");
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return new BigDecimal("0.00");
            }
            return jsonNode.isBigDecimal()
                    ? jsonNode.decimalValue()
                    : new BigDecimal(getAsString(jsonNode));
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get big decimal error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return boolean, 默认为false
     */
    public static boolean getAsBoolean(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return false;
            }
            if (jsonNode.isBoolean()) {
                return jsonNode.booleanValue();
            } else {
                if (jsonNode.getNodeType() == JsonNodeType.STRING) {
                    return BooleanUtils.toBoolean(jsonNode.asString());
                } else { // number
                    return BooleanUtils.toBoolean(jsonNode.intValue());
                }
            }
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get boolean error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return byte[], 默认为 null
     */
    public static byte[] getAsBytes(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new byte[0];
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return new byte[0];
            }
            return jsonNode.isBinary() ? jsonNode.binaryValue() : getAsString(jsonNode).getBytes();
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson get byte error, json: %s, key: %s", json, key), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return object, 默认为 null
     */
    public static <V> V getAsObject(String json, String key, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return null;
            }
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return from(getAsString(jsonNode), javaType);
        } catch (Exception e) {
            throw new JacksonException(String.format(
                    "jackson get list error, json: %s, key: %s, type: %s", json, key, type), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return list, 默认为 null
     */
    public static <V> List<V> getAsList(String json, String key, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            JsonNode jsonNode = getAsJsonObject(json, key);
            if (null == jsonNode) {
                return Collections.emptyList();
            }
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return from(getAsString(jsonNode), collectionType);
        } catch (Exception e) {
            throw new JacksonException(String.format(
                    "jackson get list error, json: %s, key: %s, type: %s", json, key, type), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return JsonNode, 默认为 null
     */
    public static JsonNode getAsJsonObject(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            if (null == node) {
                return null;
            }
            return node.get(key);
        } catch (Exception e) {
            throw new JacksonException(String.format(
                    "jackson get object from json error, json: %s, key: %s", json, key), e);
        }
    }

    /** 向json中添加属性 */
    private static <V> void add(JsonNode jsonNode, String key, V value) {
        ObjectNode node = (ObjectNode) jsonNode;
        switch (value) {
            case String s -> node.put(key, s);
            case Short s -> node.put(key, s);
            case Integer i -> node.put(key, i);
            case Long l -> node.put(key, l);
            case Float f -> node.put(key, f);
            case Double d -> node.put(key, d);
            case BigDecimal bd -> node.put(key, bd);
            case BigInteger bi -> node.put(key, bi);
            case Boolean b -> node.put(key, b);
            case byte[] ba -> node.put(key, ba);
            case null -> node.putNull(key);
            default -> node.put(key, to(value));
        }
    }

    /**
     * 除去json中的某个属性
     *
     * @return json
     */
    public static String remove(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            ((ObjectNode) node).remove(key);
            return node.toString();
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson remove error, json: %s, key: %s", json, key), e);
        }
    }

    /** 修改json中的属性 */
    public static <V> String update(String json, String key, V value) {
        try {
            JsonNode node = mapper.readTree(json);
            ((ObjectNode) node).remove(key);
            add(node, key, value);
            return node.toString();
        } catch (Exception e) {
            throw new JacksonException(String.format(
                    "jackson update error, json: %s, key: %s, value: %s", json, key, value), e);
        }
    }

    /**
     * 格式化Json(美化)
     *
     * @return json
     */
    public static String format(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            throw new JacksonException(String.format("jackson format json error, json: %s", json), e);
        }
    }

    /**
     * 判断字符串是否是json
     *
     * @return json
     */
    public static boolean isJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
