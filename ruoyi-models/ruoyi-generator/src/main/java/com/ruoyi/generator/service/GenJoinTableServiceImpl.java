package com.ruoyi.generator.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.constant.GenConstants;
import com.ruoyi.generator.domain.GenJoinTable;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.vo.GenTableVo;
import com.ruoyi.generator.mapper.GenJoinTableMapper;
import com.ruoyi.generator.mapper.GenTableMapper;

/**
 * 代码生成关联字段Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-02-19
 */
@Service
public class GenJoinTableServiceImpl implements IGenJoinTableService {
    @Autowired
    private GenJoinTableMapper genJoinTableMapper;

    @Autowired
    private GenTableMapper genTableMapper;

    /**
     * 查询代码生成关联字段列表
     * 
     * @param genJoinTable 代码生成关联字段
     * @return 代码生成关联字段
     */
    @Override
    public List<GenJoinTable> selectGenJoinTableList(GenJoinTable genJoinTable) {
        return genJoinTableMapper.selectGenJoinTableList(genJoinTable);
    }

    public GenTable selectGenTableById(Long id) {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        setTableFromOptions(genTable);
        return genTable;
    }

    /**
     * 设置代码生成其他选项值
     * 
     * @param genTable 设置后的生成对象
     */
    public void setTableFromOptions(GenTable genTable) {
        JSONObject paramsObj = JSON.parseObject(genTable.getOptions());
        if (StringUtils.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getString(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getString(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getString(GenConstants.TREE_NAME);
            String parentMenuId = paramsObj.getString(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getString(GenConstants.PARENT_MENU_NAME);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }

    @Override
    public GenTableVo selectGenJoinTableVoListByGenTable(GenTable table) {
        GenTableVo genTableVo = new GenTableVo();
        genTableVo.setTable(table);
        genTableVo.setColumns(table.getColumns());
        GenJoinTable genJoinTable = new GenJoinTable();
        genJoinTable.setTableId(table.getTableId());
        List<GenJoinTable> selectGenJoinTableList = this.selectGenJoinTableList(genJoinTable);
        genTableVo.setJoinTablesMate(selectGenJoinTableList);
        Map<Long, GenTable> joinTableMap = new HashMap<Long, GenTable>();
        joinTableMap.put(table.getTableId(), table);
        selectGenJoinTableList.forEach(i -> {
            if (Objects.isNull(joinTableMap.get(i.getLeftTableId()))) {
                joinTableMap.put(i.getLeftTableId(), this.selectGenTableById(i.getLeftTableId()));
            }
            if (Objects.isNull(joinTableMap.get(i.getRightTableId()))) {
                joinTableMap.put(i.getRightTableId(), this.selectGenTableById(i.getRightTableId()));
            }
        });
        genTableVo.setJoinTables(joinTableMap.values());
        return genTableVo;
    }

    /**
     * 新增代码生成关联字段
     * 
     * @param genJoinTable 代码生成关联字段
     * @return 结果
     */
    @Override
    public int insertGenJoinTable(GenJoinTable genJoinTable) {
        genJoinTable.setCreateTime(DateUtils.getNowDate());
        return genJoinTableMapper.insertGenJoinTable(genJoinTable);
    }

    /**
     * 修改代码生成关联字段
     * 
     * @param genJoinTable 代码生成关联字段
     * @return 结果
     */
    @Override
    public int updateGenJoinTable(GenJoinTable genJoinTable) {
        genJoinTable.setUpdateTime(DateUtils.getNowDate());
        return genJoinTableMapper.updateGenJoinTable(genJoinTable);
    }

    /**
     * 根据tableId删除字段关联
     */
    public int deleteGenJoinTableByTableId(Long tableId) {
        return genJoinTableMapper.deleteGenJoinTableByTableId(tableId);
    }

}
