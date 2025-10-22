package com.ruoyi.mybatis.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.mybatis.annotation.Query;
import com.ruoyi.mybatis.domain.TableInfo;

public class QueryWrapperUtil {
    public static List<String> getArrayFromParam(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj instanceof String) {
            for (String split : ((String) obj).split(",")) {
                list.add(split);
            }
        } else if (obj instanceof Collection) {
            for (Object split : ((Collection<?>) obj)) {
                list.add(split.toString());
            }
        }
        return list;
    }

    public static <T extends BaseEntity> QueryWrapper<T> initQueryWrapper(T entity) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        TableInfo tableInfo = TableContainer.getTableInfo(entity);
        Map<String, Object> params = entity.getParams();

        tableInfo.getNotNullColumnsForQuery(entity).stream()
                .forEach(field -> {
                    Object fieldValue = null;
                    try {
                        fieldValue = field.getField().get(entity);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    Query queryAnnotation = field.getQuery();
                    switch (queryAnnotation.operation()) {
                        case eq -> queryWrapper.eq(field.getUnqualifiedColumnName(), fieldValue);
                        case ne -> queryWrapper.ne(field.getUnqualifiedColumnName(), fieldValue);
                        case gt -> queryWrapper.gt(field.getUnqualifiedColumnName(), fieldValue);
                        case ge -> queryWrapper.ge(field.getUnqualifiedColumnName(), fieldValue);
                        case le -> queryWrapper.le(field.getUnqualifiedColumnName(), fieldValue);
                        case lt -> queryWrapper.lt(field.getUnqualifiedColumnName(), fieldValue);
                        case between -> {
                            String begin = queryAnnotation.sections()[0];
                            String end = queryAnnotation.sections()[1];
                            queryWrapper.between(field.getUnqualifiedColumnName(), params.get(begin), params.get(end));
                        }
                        case notBetween -> {
                            String begin = queryAnnotation.sections()[0];
                            String end = queryAnnotation.sections()[1];
                            queryWrapper.notBetween(field.getUnqualifiedColumnName(), params.get(begin),
                                    params.get(end));
                        }
                        case like -> queryWrapper.like(field.getUnqualifiedColumnName(), fieldValue);
                        case notLike -> queryWrapper.notLike(field.getUnqualifiedColumnName(), fieldValue);
                        case likeLeft -> queryWrapper.likeLeft(field.getUnqualifiedColumnName(), fieldValue);
                        case likeRight -> queryWrapper.likeRight(field.getUnqualifiedColumnName(), fieldValue);
                        case notLikeLeft -> queryWrapper.notLikeLeft(field.getUnqualifiedColumnName(), fieldValue);
                        case notLikeRight -> queryWrapper.notLikeRight(field.getUnqualifiedColumnName(), fieldValue);
                        case isNull -> queryWrapper.isNull(field.getUnqualifiedColumnName());
                        case isNotNull -> queryWrapper.isNotNull(field.getUnqualifiedColumnName());
                        case in -> queryWrapper.in(field.getUnqualifiedColumnName(),
                                getArrayFromParam(params.get(queryAnnotation.section())));
                        case notIn -> queryWrapper.notIn(field.getUnqualifiedColumnName(),
                                getArrayFromParam(params.get(queryAnnotation.section())));
                        case inSql -> queryWrapper.inSql(field.getUnqualifiedColumnName(), queryAnnotation.sql());
                        case notInSql -> queryWrapper.notInSql(field.getUnqualifiedColumnName(), queryAnnotation.sql());
                        default ->
                            throw new IllegalArgumentException(
                                    "Unsupported operation: " + queryAnnotation.operation());
                    }
                });

        return queryWrapper;
    }
}
