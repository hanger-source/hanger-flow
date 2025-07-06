package source.hanger.flow.contract.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 并行步骤定义（ParallelStepDefinition）
 * <p>
 * 作用：
 *   - 表示流程中的并行处理节点，支持多个分支并发执行
 *   - 支持分支条件、分支目标、汇合点等并行控制语义
 * <p>
 * 典型用法：
 *   - 由DSL parallel { ... } 语法块生成
 *   - 每个分支由Branch对象描述，汇合点由joinBranchNames指定
 * <p>
 * 设计说明：
 *   - branches存储所有分支定义，key为分支目标节点名
 *   - joinBranchNames指定哪些分支完成后进行汇合
 *   - 支持灵活的并行与汇合控制
 */
public class ParallelStepDefinition extends AbstractStepDefinition {
    /** 并行分支映射，key为分支目标节点名，value为分支定义 */
    private final Map<String, Branch> branches = new HashMap<>();
    /** 汇合点分支名称列表，指定哪些分支完成后汇合 */
    private List<String> joinBranchNames;

    /**
     * 添加并行分支
     * @param branch 分支定义
     */
    public void addBranch(Branch branch) {
        branches.put(branch.nextStepName(), branch);
    }

    /**
     * 获取所有并行分支
     * @return 分支映射
     */
    public Map<String, Branch> getBranches() {
        return branches;
    }

    /**
     * 获取汇合点分支名称列表
     * @return 汇合点分支名称列表
     */
    public List<String> getJoinBranchNames() {
        return joinBranchNames;
    }

    /**
     * 设置汇合点分支名称列表
     * @param joinBranchNames 汇合点分支名称列表
     */
    public void setJoinBranchNames(List<String> joinBranchNames) {
        this.joinBranchNames = joinBranchNames;
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
        return StepType.PARALLEL;
    }
}