package com.ruoyi.flowable.service.impl;

import java.util.Map;
import java.util.Objects;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.vo.FlowTaskVo;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.service.IFlowInstanceService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 工作流流程实例管理
 * <p>
 *
 * @author Tony
 * @date 2021-04-03
 */
@Service
@Slf4j
public class FlowInstanceServiceImpl extends FlowServiceFactory implements IFlowInstanceService {

    /**
     * 结束流程实例
     *
     * @param vo
     */
    @Override
    public void stopProcessInstance(FlowTaskVo vo) {
        String taskId = vo.getTaskId();

        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空。");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("未找到ID为 " + taskId + " 的任务，无法停止流程实例。可能任务已完成或不存在。");
        }
        String processInstanceId = task.getProcessInstanceId();
        if (!StringUtils.hasText(processInstanceId)) {
            throw new RuntimeException("任务 " + taskId + " 没有关联的流程实例ID。");
        }
        String deleteReason = vo.getComment(); // 假设 FlowTaskVo 有 getComment() 方法获取原因
        if (!StringUtils.hasText(deleteReason)) {
            deleteReason = "流程实例由用户通过任务ID " + taskId + " 手动停止。"; // 提供一个默认原因
        }
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    /**
     * 激活或挂起流程实例
     *
     * @param state      状态
     * @param instanceId 流程实例ID
     */
    @Override
    public void updateState(Integer state, String instanceId) {

        // 激活
        if (state == 1) {
            runtimeService.activateProcessInstanceById(instanceId);
        }
        // 挂起
        if (state == 2) {
            runtimeService.suspendProcessInstanceById(instanceId);
        }
    }

    /**
     * 删除流程实例ID
     *
     * @param instanceId   流程实例ID
     * @param deleteReason 删除原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String instanceId, String deleteReason) {

        // 查询历史数据
        HistoricProcessInstance historicProcessInstance = getHistoricProcessInstanceById(instanceId);
        if (historicProcessInstance.getEndTime() != null) {
            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
            return;
        }
        // 删除流程实例
        runtimeService.deleteProcessInstance(instanceId, deleteReason);
        // 删除历史流程实例
        historyService.deleteHistoricProcessInstance(instanceId);
    }

    /**
     * 根据实例ID查询历史实例数据
     *
     * @param processInstanceId
     * @return
     */
    @Override
    public HistoricProcessInstance getHistoricProcessInstanceById(String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (Objects.isNull(historicProcessInstance)) {
            throw new FlowableObjectNotFoundException("流程实例不存在: " + processInstanceId);
        }
        return historicProcessInstance;
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程定义Id
     * @param variables 流程变量
     * @return
     */
    @Override
    public AjaxResult startProcessInstanceById(String procDefId, Map<String, Object> variables) {

        try {
            // 设置流程发起人Id到流程中
            Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
            // identityService.setAuthenticatedUserId(userId.toString());
            variables.put("initiator", userId);
            variables.put("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
            runtimeService.startProcessInstanceById(procDefId, variables);
            return AjaxResult.success("流程启动成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("流程启动错误");
        }
    }
}