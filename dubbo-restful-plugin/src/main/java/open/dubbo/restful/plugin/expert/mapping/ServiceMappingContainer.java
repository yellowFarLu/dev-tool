package open.dubbo.restful.plugin.expert.mapping;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;

import lombok.extern.slf4j.Slf4j;
import open.dubbo.restful.plugin.constant.RestfulConstants;
import open.dubbo.restful.plugin.exception.NotFoundServiceException;
import open.dubbo.restful.plugin.util.ServicesUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * created by huangy on 2019年4月9日
 */
@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
public class ServiceMappingContainer {


    private static ConcurrentHashMap<String, ServiceHandler> SERVICE_MAPPING = new ConcurrentHashMap<String, ServiceHandler>();

    public void registerService(URL url, Class serviceType, Object impl) {
        String contextPath = url.getParameter(RestfulConstants.CONTEXT_PATH,"/");
        String path  = StringUtils.replaceOnce(url.getPath(),contextPath,"");
        if(StringUtils.startsWith(path, "/")){
            path = StringUtils.replaceOnce(path, "/", "");
        }
        SERVICE_MAPPING.putIfAbsent(path,
                new ServiceHandler(url.getParameter(Constants.GROUP_KEY),
                        url.getParameter(Constants.VERSION_KEY),
                        serviceType,path,impl));
    }

    public ServiceHandler mappingService(String path, RequestEntity entity) {
        //路径直接匹配，dubbo的默认path是接口全名(数字累加值)，如果配置了path属性，则是该属性值
        if (SERVICE_MAPPING.containsKey(path)) {
            ServiceHandler serviceHandler = SERVICE_MAPPING.get(path);
            if (!serviceHandler.getServiceType().getName().equals(path)) {
                return SERVICE_MAPPING.get(path);
            }
        }
        //路径并没有匹配，那么说明这个服务存在多个版本实现，前端请求的路径是直接接口请求，所以需要判断接口信息以及版本和分组
        Collection<ServiceHandler> serviceHandlerCollection = SERVICE_MAPPING.values();
        for (ServiceHandler serviceHandler : serviceHandlerCollection) {
            if (serviceHandler.getServiceType().getName().equals(path)
                    || serviceHandler.getServiceType().getSimpleName().equals(path)) {//请求的path是接口全名
                if (!StringUtils.isEmpty(serviceHandler.getVersion())
                        && !StringUtils.isEmpty(serviceHandler.getGroup())) {
                    if (serviceHandler.getVersion().equals(entity.getVersion())
                            && serviceHandler.getGroup().equals(entity.getGroup())) {
                        return serviceHandler;
                    }
                } else {
                    if (!StringUtils.isEmpty(serviceHandler.getVersion())) {
                        if (serviceHandler.getVersion().equals(entity.getVersion())) {
                            return serviceHandler;
                        }
                    } else if (!StringUtils.isEmpty(serviceHandler.getGroup())) {
                        if (serviceHandler.getGroup().equals(entity.getGroup())) {
                            return serviceHandler;
                        }
                    }else if(StringUtils.isEmpty(serviceHandler.getVersion())
                            && StringUtils.isEmpty(serviceHandler.getGroup())
                            && StringUtils.isEmpty(entity.getVersion())
                            && StringUtils.isEmpty(entity.getGroup())){
                        return serviceHandler;
                    }
                }
            }
        }
        log.warn("service is not found, requestEntity={}", entity);
        throw new NotFoundServiceException(path, entity.getVersion(), entity.getGroup());
    }

    public void unregisterService(URL url) {
        SERVICE_MAPPING.remove(url.getAbsolutePath());
    }


    public void writeServiceHtml(OutputStream outputStream) throws IOException {
        ServicesUtil.writeServicesHtml(outputStream,SERVICE_MAPPING);
    }

}
