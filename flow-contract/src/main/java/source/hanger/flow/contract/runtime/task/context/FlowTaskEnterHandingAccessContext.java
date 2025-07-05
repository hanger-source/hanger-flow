package source.hanger.flow.contract.runtime.task.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccessContext;

/**
 * 任务进入处理访问上下文
 * 继承自FlowRuntimeAccessContext，专门用于任务进入处理时的上下文数据存储
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public class FlowTaskEnterHandingAccessContext extends FlowRuntimeAccessContext {
    @Serial
    private static final long serialVersionUID = -6099466616844523546L;
}
