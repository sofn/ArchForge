package com.lesofn.archsmith.common.utils.collection;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.stream.Gatherers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/** Authors: sofn Version: 1.0 Created at 15-6-7 00:43. */
public class CollectionUtils {
    public static final Joiner.MapJoiner MAP_JOINER = Joiner.on(',').withKeyValueSeparator("=");
    public static final Joiner ARGS_JOINER = Joiner.on('_');
    public static final Joiner ARGS_JOINER_2 = Joiner.on(',');
    public static final Splitter LIST_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    public static final Splitter LINE_SPLITTER =
            Splitter.onPattern("\r?\n").trimResults().omitEmptyStrings();

    public static List<String> strListSplitter(String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(LIST_SPLITTER.split(str));
    }

    public static List<Long> longListSplitter(String str) {
        return Lists.newArrayList(
                Collections2.transform(strListSplitter(str), NumberUtils::toLong));
    }

    public static List<Integer> intListSplitter(String str) {
        return Lists.newArrayList(Collections2.transform(strListSplitter(str), NumberUtils::toInt));
    }

    public static List<String> strLineSplitter(String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(LINE_SPLITTER.split(str));
    }

    /**
     * 将列表按指定大小分批（使用 JDK 24+ Gatherers API）
     *
     * @param list 源列表
     * @param size 每批大小
     * @return 分批后的列表
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        Preconditions.checkNotNull(list, "list must not be null");
        Preconditions.checkArgument(size > 0, "size must be positive, got: %s", size);
        return list.stream().gather(Gatherers.windowFixed(size)).toList();
    }
}
