package open.dubbo.restful.plugin.util;

/**
 * created by huangy on 2019年4月9日
 */
public class ClassUtils {

    public static boolean isBasicType(Class<?> type){
        if(type.isPrimitive()){
            return true;
        }else if(type==String.class){
            return true;
        }else if(Byte.class==type){
            return true;
        }else if(Number.class.isAssignableFrom(type)){
            return true;
        }else if(Character.class==type){
            return true;
        }else if(Boolean.class==type){
            return true;
        }
        return false;
    }

    public static Object caseBasicType(Class<?> type,String value){
        if(type==String.class){
            return value;
        }else if(Byte.class==type||byte.class==type){
            return caseByte(value);
        }else if(Integer.class==type||int.class==type){
            return caseInteger(value);
        }else if(Short.class==type||short.class==type){
            return caseShort(value);
        }else if(Long.class==type||long.class==type){
            return caseLong(value);
        }else if(Float.class==type||float.class==type){
            return caseFloat(value);
        }else if(Double.class==type||double.class==type){
            return caseDouble(value);
        }else if(Integer.class==type||int.class==type){
            return caseDouble(value);
        }else if(Character.class==type||char.class==type){
            return caseChar(value);
        }else if(Boolean.class==type||boolean.class==type){
            return caseBoolean(value);
        }
        return value;
    }

    public static boolean caseBoolean(String value){
        return Boolean.parseBoolean(value);
    }

    public static Integer caseInteger(String value){
        return Integer.parseInt(value);
    }

    public static Short caseShort(String value){
        return Short.parseShort(value);
    }

    public static Long caseLong(String value){
        return Long.parseLong(value);
    }

    public static Float caseFloat(String value){
        return Float.parseFloat(value);
    }

    public static Double caseDouble(String value){
        return Double.parseDouble(value);
    }

    public static Byte caseByte(String value){
        return Byte.parseByte(value);
    }

    public static Character caseChar(String value){
        return value.charAt(0);
    }


}

