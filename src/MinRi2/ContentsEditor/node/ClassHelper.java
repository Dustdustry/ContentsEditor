package MinRi2.ContentsEditor.node;

import arc.struct.*;

public class ClassHelper{
    public static Class<?> unoymousClass(Class<?> clazz){
        if(clazz == null) return null;
        while(clazz.isAnonymousClass()) clazz = clazz.getSuperclass();
        return clazz;
    }

    public static boolean isArray(Class<?> type){
        return type != null && (type.isArray() || Seq.class.isAssignableFrom(type) || ObjectSet.class.isAssignableFrom(type));
    }

    public static boolean isMap(Class<?> type){
        return type != null && ObjectMap.class.isAssignableFrom(type);
    }
}
