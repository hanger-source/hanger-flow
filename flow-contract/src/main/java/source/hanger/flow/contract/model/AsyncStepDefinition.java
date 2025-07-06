package source.hanger.flow.contract.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步分支步骤定义（AsyncStepDefinition）
 * <p>
 * 作用：
 *   - 表示流程中的异步分支节点，支持多个分支异步触发
 *   - 适用于通知、事件等无需阻塞主流程的场景
 * <p>
 * 典型用法：
 *   - 由DSL async { ... } 语法块生成
 *   - 每个分支只需指定目标节点名称
 * <p>
 * 设计说明：
 *   - branchNames存储所有异步分支目标节点名
 *   - 支持灵活的异步分支扩展
 */
public class AsyncStepDefinition extends AbstractStepDefinition {
    /** 异步分支目标节点名称列表 */
    private final List<String> branchNames = new ArrayList<>();

    /**
     * 添加异步分支目标节点名称
     * @param s 分支目标节点名称
     */
    public void addBranchName(String s) {
        branchNames.add(s);
    }

    /**
     * 获取所有异步分支目标节点名称
     * @return 分支目标节点名称列表
     */
    public List<String> getBranchNames() {
        return branchNames;
    }

    @Override
    public boolean isStreamingSupported() {
        return false;
    }

    @Override
    public Class<?> getOutputType() {
        return Object.class;
    }

    @Override
    public StepType getStepType() {
        return StepType.ASYNC;
    }
}