package open.dubbo.restful.plugin.expert.mapping;

import com.alibaba.dubbo.common.Constants;
import lombok.extern.slf4j.Slf4j;
import open.dubbo.restful.plugin.annotation.RestPath;
import open.dubbo.restful.plugin.exception.NotFoundServiceException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * created by huangy on 2019年4月9日
 */
@Slf4j
public class ServiceHandler<T extends Object> {

    private String group;

    private String version;

    private Class<T> serviceType;

    private T impl;

    private List<MethodHandler> methodHandlerList;

    private String path;

    public ServiceHandler(String group, String version, Class<T> serviceType,String path, T impl) {
        if(serviceType==null){
            throw new IllegalArgumentException("[serviceType] must not be null");
        }
        this.group = Constants.ANY_VALUE.equals(group)?null:group;
        this.version = Constants.ANY_VALUE.equals(version)?null:version;
        this.serviceType = serviceType;
        this.impl = impl;
        methodHandlerList =new ArrayList<MethodHandler>();
        this.path=path;
        initHandler();
    }


    public String getPath() {
        return path;
    }

    @SuppressWarnings("rawtypes")
    private void initHandler(){
        Class type = this.serviceType;
        String methodName = null;
        RestPath restPath = null;
        while(type!=null && type!= Object.class){
            Method[] methods = type.getDeclaredMethods();
            for(Method method: methods){
                restPath = method.getAnnotation(RestPath.class);
                methodName = null != restPath ? restPath.path() : method.getName();
                MethodHandler methodHandler = new MethodHandler(methodName, method,
                        method.getParameterTypes(), method.getGenericParameterTypes(), this.impl);
                methodHandlerList.add(methodHandler);
            }
            type = type.getSuperclass();
        }
    }


    public List<MethodHandler> getMethodHandlerList() {
        return methodHandlerList;
    }

    public MethodHandler mappingMethod(RequestEntity requestEntity){
        for(MethodHandler methodHandler:methodHandlerList){
            if(methodHandler.support(requestEntity)){
                return methodHandler;
            }
        }
        log.warn("Not found method in service {}, requestEntity={}[", serviceType.getName(), requestEntity);
        throw new NotFoundServiceException(path, version, group);
    }


    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public Class<T> getServiceType() {
        return serviceType;
    }

    public T getImpl() {
        return impl;
    }

    @Override
    public String toString() {
        return "ServiceHandler{" +
                "serviceType=" + serviceType.getName() +
                ", version='" + version + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}

