package open.dubbo.restful.plugin.expert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facishare.dubbo.restful.constants.RestfulConstants;
import com.facishare.dubbo.restful.exception.NotFoundServiceException;
import com.facishare.dubbo.restful.export.mapping.MethodHandler;
import com.facishare.dubbo.restful.export.mapping.RequestEntity;
import com.facishare.dubbo.restful.export.mapping.ServiceHandler;
import com.facishare.dubbo.restful.export.mapping.ServiceMappingContainer;
import com.facishare.dubbo.restful.util.ClassUtils;
import com.facishare.dubbo.restful.util.GsonUtil;
import com.github.trace.TraceContext;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * created by huangy on 2019年4月9日
 */
@Slf4j
public class RestfulHandler implements HttpHandler {

    private ServiceMappingContainer serviceMappingContainer;

    private String contextPath = "/";

    public RestfulHandler(ServiceMappingContainer serviceMappingContainer,String contextPath) {
        this.serviceMappingContainer = serviceMappingContainer;
        this.contextPath = contextPath;
    }

    /**
     * 所有restful的入口
     * http://ip:port/contextpath/${path}/${method}/${version}/${group}
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        setRemoteAddress(request);
        fillTraceId(request);
        String requestUri = request.getRequestURI();
        log.info("receive request, requestUri={}", requestUri);
        if(!StringUtils.contains(requestUri, contextPath)){
            response.sendError(404);
            return;
        }

        try {
            requestUri = requestUri.substring(requestUri.indexOf(contextPath) + contextPath.length());
            String[] fragments = StringUtils.split(requestUri, "/");
            if(fragments.length == 0){
                log.error("request uri ["+requestUri+"] is incorrect.");
                response.sendError(404);
                return;
            }
            String path = fragments[0];

            // 创建请求参数对象
            long startTime = System.currentTimeMillis();
            RequestEntity entity = generateRequestEntity(request, fragments);
            try{
                ServiceHandler serviceHandler = serviceMappingContainer.mappingService(path, entity);
                MethodHandler methodHandler = serviceHandler.mappingMethod(entity);
                try {
                    log.info("start to handle request, requestUri={}, param={}", requestUri, entity);
                    Object result = methodHandler.invoke(convertArgs(entity.getArgs(), methodHandler.getArgTypes(), methodHandler.getArgRealTypes()));
                    fillTraceId(request);
                    log.info("invoke service success, requestUri={}, param={}, result={}, cost={}",
                            requestUri, entity, result, System.currentTimeMillis() - startTime);
                    rendingResponse(response,result);
                } catch (Exception e) {
                    log.warn("Fail to invoke method ", methodHandler ,e);
                    response.sendError(500);
                }
            }catch (NotFoundServiceException e){
                log.warn("Not found service for request uri={}", requestUri, e);
                response.sendError(404);
            }
        } catch (Exception e) {
            log.warn("failed to handle request, requestUri={}, request={}", requestUri, request, e);
        }

    }

    private void fillTraceId(HttpServletRequest request) {
        TraceContext traceContext = TraceContext.get();
        String requestTraceId = request.getHeader("x-fs-trace-id");
        traceContext.setTraceId(requestTraceId);
    }

    private void setRemoteAddress(HttpServletRequest request) {
        RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
    }

    private RequestEntity generateRequestEntity(HttpServletRequest request, String[] fragments) {
        RequestEntity entity = null;
        try{
            byte[] requestContent = copyBytesFromRequest(request);
            JSONObject jsonObject  = (JSONObject) JSON.parse(requestContent);
            entity = new RequestEntity(jsonObject);
        }catch (Exception e){
            log.error("Fail to parse request content to json,requestUri={}", request.getRequestURI());
        }
        if(entity == null){
            entity = new RequestEntity();
        }
        if(fragments.length >= 2){
            entity.setMethod(fragments[1]);
        }
        if(fragments.length >=3 && !fragments[2].equals(RestfulConstants.ALL)){
            entity.setVersion(fragments[2]);
        }
        if(fragments.length >=4 && !fragments[3].equals(RestfulConstants.ALL)){
            entity.setGroup(fragments[3]);
        }
        readAttachment(request);
        log.info("requestEntity={}", entity);
        return entity;
    }

    private void readAttachment(HttpServletRequest request){
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            if(name.startsWith(RestfulConstants.RESTFUL_HEADER_KEY_PREFIX)){
                RpcContext.getContext().setAttachment(name.replaceFirst(RestfulConstants.RESTFUL_HEADER_KEY_PREFIX,""),request.getHeader(name));
            }
        }
    }

    private void rendingResponse(HttpServletResponse response,Object ret) throws IOException {
        if(ret!=null){
            try {
                response.setHeader("Content-type", "application/json;charset=UTF-8");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(GsonUtil.getGson().toJson(ret).getBytes());
            } catch (IOException e) {
                log.error("Fail to rending response",e);
                response.sendError(500);
            }
        }
    }

    private Object[] convertArgs(String[] args,Class<?>[] argTypes, Type[] argRealTypes){
        Object[] objects = new Object[argTypes.length];
        for(int i=0;i<argTypes.length;i++){
            if(!StringUtils.isEmpty(args[i])){
                if(ClassUtils.isBasicType(argTypes[i])){
                    objects[i]=ClassUtils.caseBasicType(argTypes[i],args[i]);
                }else{
                    //Gson转换时，用实际type转换，保证泛型转换不出错
                    objects[i]= GsonUtil.getGson().fromJson(args[i], argRealTypes[i]);
                }
            }
        }
        return objects;
    }

    /**
     * 将request body的byte字节数组复制到内存中
     * @param httpServletRequest
     * @return
     * @throws IOException
     */
    private byte[] copyBytesFromRequest(HttpServletRequest httpServletRequest) throws IOException {
        httpServletRequest.setCharacterEncoding("UTF-8");
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader=null;
        try{
            reader = new BufferedReader(new InputStreamReader(httpServletRequest.getInputStream(),"UTF-8"));
            String line=null;
            while((line = reader.readLine()) != null){
                buffer.append(line);
            }
        }catch(Exception e){
            log.warn("Fail to copyBytesFromRequest",e);
        } finally {
            if(null!=reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Fail to copyBytesFromRequest",e);
                }
            }
        }
        return buffer.toString().getBytes();
    }
}

