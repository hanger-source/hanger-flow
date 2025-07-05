package source.hanger.flow.contract.runtime.common;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class FlowRuntimeAccessContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 6473185372366539085L;

    protected final Map<String, Object> inner = new HashMap<>();
}
