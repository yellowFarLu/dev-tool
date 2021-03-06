package open.dubbo.restful.plugin.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

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
        // 设置traceId
    }
}
