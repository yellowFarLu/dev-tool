package open.dubbo.restful.plugin.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * created by huangy on 2019年4月9日
 */
public class NotFoundServiceException extends RuntimeException {

    private static final long serialVersionUID = 7189192949980502421L;

    private String path;

    private String version;

    private String group;

    public NotFoundServiceException(String path, String version, String group) {
        this.path = path;
        this.version = version;
        this.group = group;
    }

    @Override
    public String getMessage() {
        return "Not found service for path:["+path+"] " +
                "version:["+ (StringUtils.isEmpty(version)?"*":version)+"] " +
                "group ["+(StringUtils.isEmpty(group)?"*":group)+"].";
    }
}
