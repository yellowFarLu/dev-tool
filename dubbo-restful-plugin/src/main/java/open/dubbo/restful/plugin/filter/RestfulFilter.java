package open.dubbo.restful.plugin.filter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.github.trace.TraceContext;

/**
 * created by huangy on 2019年4月9日
 */
@Activate(group = Constants.PROVIDER, order = -100001)
public class RestfulFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        fillTraceId(invocation);
        return invoker.invoke(invocation);
    }

    private void fillTraceId(Invocation invocation) {
        invocation.getAttachments().putAll(RpcContext.getContext().getAttachments());
        Map<String, String> m = invocation.getAttachments();
        if(StringUtils.isBlank(m.get("traceId"))) {
            m.put("traceId", TraceContext.get().getTraceId());
        }
    }
}
