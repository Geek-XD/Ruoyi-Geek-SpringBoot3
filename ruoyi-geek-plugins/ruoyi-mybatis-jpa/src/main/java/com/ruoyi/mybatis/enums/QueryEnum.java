package com.ruoyi.mybatis.enums;

/**
 * 数据库查询枚举
 *
 * @author Dftre
 */
public enum QueryEnum {
    eq, // 等于 =
    ne, // 不等于 <>
    gt, // 大于 >
    ge, // 大于等于 >=
    lt, // 小于 <
    le, // 小于等于 <=
    like, // 模糊查询
    notLike,
    likeLeft, // 左模糊查询
    likeRight, // 右模糊查询
    notLikeLeft,
    notLikeRight,
    in, // in
    notIn, // not in
    between, // between and
    notBetween, // not between and
    isNull, //is null
    isNotNull,
    inSql,
    notInSql;
}   
