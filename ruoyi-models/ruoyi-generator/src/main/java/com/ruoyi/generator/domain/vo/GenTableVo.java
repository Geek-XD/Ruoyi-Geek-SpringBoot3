package com.ruoyi.generator.domain.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.generator.domain.GenJoinTable;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * 业务表 gen_table
 * 
 * @author ruoyi
 */
@Data
@Setter
@EqualsAndHashCode(callSuper = true)
public class GenTableVo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 业务表 */
    @Valid
    private GenTable table;

    /** 业务表的列 */
    @Valid
    private List<GenTableColumn> columns;

    /** 关联信息 */
    @Valid
    private List<GenJoinTable> joinTablesMate;

    /** 参与关联的表 */
    private Collection<GenTable> joinTables;

    /** 参与关联的列 */
    private List<GenTableColumn> joinColumns;

    public List<GenTable> getAllGenTables() {
        List<GenTable> allGenTables = new ArrayList<>();
        allGenTables.add(table);
        allGenTables.addAll(joinTables);
        return allGenTables;
    }

    public List<GenTableColumn> getAllGenTableColumns() {
        List<GenTableColumn> allGenTableColumns = new ArrayList<>();
        if (columns != null) {
            allGenTableColumns.addAll(columns);
        }
        if (joinColumns != null) {
            allGenTableColumns.addAll(joinColumns);
        }
        return allGenTableColumns;
    }

    public Map<Long, GenTable> getTableMap() {
        Map<Long, GenTable> tableMap = new HashMap<>();
        if (table != null) {
            tableMap.put(table.getTableId(), table);
        }
        if (joinTables != null) {
            for (GenTable genTable : joinTables) {
                if (genTable != null) {
                    tableMap.put(genTable.getTableId(), genTable);
                }
            }
        }
        return tableMap;
    }

    public Map<Long, String> getTableAliasMap() {
        Map<Long, String> tableMap = new HashMap<>();
        if (table != null) {
            tableMap.put(table.getTableId(), table.getTableAlias());
        }
        if (joinTablesMate != null) {
            for (GenJoinTable genTable : joinTablesMate) {
                if (genTable != null) {
                    tableMap.put(genTable.getLeftTableId(), genTable.getLeftTableAlias());
                    tableMap.put(genTable.getRightTableId(), genTable.getRightTableAlias());
                }
            }
        }
        return tableMap;
    }

    public Map<Long, GenTableColumn> getColumnMap() {
        Map<Long, GenTableColumn> columnMap = new HashMap<>();
        List<GenTable> genTables = getAllGenTables();
        for (GenTable genTable : genTables) {
            for (GenTableColumn genTableColumn : genTable.getColumns()) {
                columnMap.put(genTableColumn.getColumnId(), genTableColumn);
            }
        }
        return columnMap;
    }

}