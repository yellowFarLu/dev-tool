package open.dubbo.restful.plugin.protocol;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.facishare.dubbo.restful.client.RestfulInvoker;
import com.facishare.dubbo.restful.constants.RestfulConstants;
import com.facishare.dubbo.restful.export.RestfulHandler;
import com.facishare.dubbo.restful.export.mapping.ServiceMappingContainer;

import lombok.extern.slf4j.Slf4j;

/**
 * created by huangy on 2019年4月9日
 */
@Slf4j
public class RestfulProtocol extends AbstractProxyProtocol{

    private static final Map<Class<?>,Object> REFER_MAPPER = new HashMap<Class<?>, Object>();

    private static final Map<String,HttpServer> SERVER_MAPPER = new HashMap<String, HttpServer>();

    private static final ServiceMappingContainer SERVICE_MAPPING_CONTAINER = new ServiceMappingContainer();

    private HttpBinder httpBinder;

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, final URL url) throws RpcException {
        log.info("export restful service, url={}", url);
        String contextPath = ConfigUtils.getProperty(RestfulConstants.DUBBO_PROTOCOL_RESTFUL_CONTEXTPATH,"/");
        String addr = url.getIp() + ":" + url.getPort();
        HttpServer server = SERVER_MAPPER.get(addr);
        if (server == null) {
            server = httpBinder.bind(url, new RestfulHandler(SERVICE_MAPPING_CONTAINER,contextPath));
            SERVER_MAPPER.put(addr, server);
        }
        SERVICE_MAPPING_CONTAINER.registerService(url,type,impl);
        return new Runnable() {
            @Override
            public void run() {
                SERVICE_MAPPING_CONTAINER.unregisterService(url);
            }
        };
    }

    @Override
    protected synchronized  <T> T doRefer(Class<T> type, URL url) throws RpcException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if(!REFER_MAPPER.containsKey(type)){
            REFER_MAPPER.put(type,new RestfulInvoker(url.setProtocol("http"),type));
        }
        return (Invoker<T>) REFER_MAPPER.get(type);
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    public void setHttpBinder(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }
}
