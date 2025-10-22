package com.ruoyi.mybatisinterceptor.util;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

/**
 * 安全构建 order by 子句，简单白名单/字符校验，避免注入。
 */
public class OrderByUtil {
    private static final Pattern COL_OK = Pattern.compile("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)?$");
    private static final Set<String> DIR = Set.of("asc", "desc");

    public static String build(String orderByColumn, String isAsc) {
        if (!StringUtils.hasText(orderByColumn)) return null;
    String globalDirTmp = (isAsc == null ? "" : isAsc.trim().toLowerCase());
    if (!DIR.contains(globalDirTmp)) globalDirTmp = null;
    final String globalDir = globalDirTmp;

        // 分解项目：允许 item 形如 "col" 或 "schema.col" 或 "col asc/desc"
        String[] items = Arrays.stream(orderByColumn.split(","))
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);
        if (items.length == 0) return null;

        return Arrays.stream(items).map(item -> {
            String[] parts = item.split("\\s+");
            if (parts.length == 0) return null;
            String col = parts[0];
            if (!COL_OK.matcher(col).matches()) return null;
            String dir = null;
            if (parts.length >= 2) {
                String d = parts[1].toLowerCase();
                if (DIR.contains(d)) dir = d;
            }
            if (dir == null) dir = (globalDir == null ? "asc" : globalDir);
            return col + " " + dir;
        }).filter(s -> s != null).collect(Collectors.joining(", "));
    }
}
