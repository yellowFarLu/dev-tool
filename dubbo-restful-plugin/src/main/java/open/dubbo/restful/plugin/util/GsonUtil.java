package open.dubbo.restful.plugin.util;

import java.text.DateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author huangy
 */
public class GsonUtil {

    private static Gson gson = null;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(java.util.Date.class, new DateSerializer()).setDateFormat(DateFormat.LONG);
        builder.registerTypeAdapter(java.util.Date.class, new DateDeserializer()).setDateFormat(DateFormat.LONG);
        gson = builder.disableHtmlEscaping().create();
    }

    public static Gson getGson(){
        return gson;
    }

}
