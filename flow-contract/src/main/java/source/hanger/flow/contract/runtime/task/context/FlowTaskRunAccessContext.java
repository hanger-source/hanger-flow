package source.hanger.flow.contract.runtime.task.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccessContext;

/**
 * 任务执行访问上下文
 * 继承自FlowRuntimeAccessContext，专门用于任务执行时的上下文数据存储
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public class FlowTaskRunAccessContext extends FlowRuntimeAccessContext {
    @Serial
    private static final long serialVersionUID = 6526379546423195930L;
}
