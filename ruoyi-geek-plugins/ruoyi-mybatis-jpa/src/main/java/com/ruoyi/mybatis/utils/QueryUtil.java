package com.ruoyi.mybatis.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.mybatis.annotation.Query;

public class QueryUtil {

    /**
     * 判断该字段是否可以被列为查询条件
     * @param entity 参与判断的对象
     * @param field 参与判断的字段
     * @param query 参与判断的查询注解
     * @return
     */
    public static boolean fieldQueryIsNotNull(Object entity, Field field, Query query) {
        try {
            if (query == null)return false;
            BaseEntity baseEntity = (BaseEntity) entity;
            Map<String, Object> map = baseEntity.getParams();
            return switch(query.operation()){
                case between ->  map.get(query.sections()[0]) != null && map.get(query.sections()[1]) != null;
                case in -> {
                    Object section = map.get(query.section());
                    if (section == null) yield  false;
                    if (section instanceof String) {
                        List<String> list = new ArrayList<>();
                        for (String split : ((String) section).split(",")) {
                            list.add("\"" + split + "\"");
                        }
                        map.put(query.section() + "_operation", String.join(",", list));
                        yield true;
                    } else if (section instanceof Collection) {
                        List<String> list = new ArrayList<>();
                        for (Object split : ((Collection<?>) section)) {
                            list.add("\"" + split.toString() + "\"");
                        }
                        map.put(query.section() + "_operation", String.join(",", list));
                        yield true;
                    } else {
                        yield false;
                    }
                }
                default ->field.get(entity) != null;
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field for building query conditions.", e);
        }
    }

    public static String getJoinSql(String leftTable, String rightTable, String leftField, String rightField) {
        return rightTable + " on "
                + leftTable + "." + leftField + " = "
                + rightTable + "." + rightField;
    }

    public static String getQuerySql(String column, String iField, Query query) {
        if (query == null)
            return "";
        String begin = "#{params." + query.sections()[0] + "}";
        String end = "#{params." + query.sections()[1] + "}";
        String inParams = "${params." + query.section() + "_operation" + "}";
        return switch (query.operation()) {
            case eq -> column + " = " + iField;
            case ne -> column + " <> " + iField;
            case gt -> column + " > " + iField;
            case ge -> column + " >= " + iField;
            case le -> column + " < " + iField;
            case lt -> column + " <= " + iField;
            case between -> column + " between " + begin + " and " + end;
            case notBetween -> column + " not between " + begin + " and " + end;
            case like -> column + " like concat('%'," + iField + ",'%')";
            case notLike -> column + "not like concat('%'," + iField + ",'%')";
            case likeLeft -> column + "like concat('%'," + iField + ")";
            case likeRight -> column + "like concat(" + iField + ",'%')";
            case notLikeLeft -> column + "not like concat('%'," + iField + ")";
            case notLikeRight -> column + "not like concat(" + iField + ",'%')";
            case isNull -> column + " is null";
            case isNotNull -> column + " is not null";
            case in -> column + " in (" + inParams + ")";
            case notIn -> column + " not in (" + inParams + ")";
            case inSql -> column + " in (" + query.sql() + ")";
            case notInSql -> column + " not in (" + query.sql() + ")";
            default -> throw new IllegalArgumentException(
                    "Unsupported operation: " + query.operation());
        };
    }

    public static String listToInSQL(Collection<Serializable> oList) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Serializable o : oList) {
            sb.append(o).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }
}
