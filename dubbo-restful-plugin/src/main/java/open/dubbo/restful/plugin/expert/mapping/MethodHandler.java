package open.dubbo.restful.plugin.expert.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * created by huangy on 2019年4月9日
 */
public class MethodHandler {

    private String methodName;


    private Method method;

    private Class<?>[] argTypes;

    private Object target;

    private Type[] argRealTypes;

    public MethodHandler(String methodName, Method method, Class<?>[] argTypes, Type[] argRealTypes, Object target) {
        this.methodName = methodName;
        this.method = method;
        this.argTypes = argTypes;
        this.argRealTypes = argRealTypes;
        this.target = target;
    }

    public Object invoke(Object...args) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Type[] getArgRealTypes() {
        return argRealTypes;
    }

    public boolean support(RequestEntity requestEntity){
        if(this.methodName.equals(requestEntity.getMethod())){
            //参数长度比较
            if(this.argTypes.length>0){
                if(requestEntity.getArgs() == null
                        || this.argTypes.length != requestEntity.getArgs().length){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MethodHandler{" +
                "methodName='" + methodName + '\'' +
                ", method=" + method +
                ", argTypes=" + Arrays.toString(argTypes) +
                ", target=" + target +
                ", argRealTypes=" + Arrays.toString(argRealTypes) +
                '}';
    }
}

