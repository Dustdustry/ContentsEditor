package MinRi2.ContentsEditor.node;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.*;

public class PatchJsonIO{
    public static final int simplifySingleCount = 3;

    private static ContentParser parser;
    private static ObjectMap<String, ContentType> nameToType;

    public static ObjectMap<Class<?>, ContentType> contentClassTypeMap = ObjectMap.of(
        Block.class, ContentType.block,
        Item.class, ContentType.item,
        Liquid.class, ContentType.liquid,
        StatusEffect.class, ContentType.status,
        UnitType.class, ContentType.unit
    );

    public static boolean isArray(NodeData data){
        return isArray(getType(data));
    }

    public static boolean isMap(NodeData data){
        return isMap(getType(data));
    }

    public static boolean isArray(Class<?> type){
        return type != null && (type.isArray() || Seq.class.isAssignableFrom(type) || ObjectSet.class.isAssignableFrom(type));
    }

    public static boolean isMap(Class<?> type){
        return type != null && ObjectMap.class.isAssignableFrom(type);
    }

    public static Object readData(NodeData data){
        if(data.jsonData == null) return null;
        Class<?> type = getType(data);
        if(type == null) return null;
        return getParser().getJson().readValue(type, data.jsonData);
    }

    public static String getKeyName(Object object){
        if(object instanceof MappableContent mc) return mc.name;
        if(object instanceof Enum<?> e) return e.name();
        if(object instanceof Class<?> clazz) return clazz.getName();
        return String.valueOf(object);
    }

    public static ContentParser getParser(){
        if(parser == null) parser = Reflect.get(ContentPatcher.class, "parser");
        return parser;
    }

    public static OrderedMap<String, FieldMetadata> getFields(Class<?> type){
        return getParser().getJson().getFields(type);
    }

    public static ObjectMap<String, ContentType> getNameToType(){
        if(nameToType == null) nameToType = Reflect.get(ContentPatcher.class, "nameToType");
        return nameToType;
    }

    public static Class<?> getType(NodeData node){
        if(node.meta != null) return node.meta.type;
        if(node.getObject() == null) return null;
        Class<?> clazz = node.getObject().getClass();
        while(clazz.isAnonymousClass()) clazz = clazz.getSuperclass();
        return clazz;
    }

    public static JsonValue transformPatch(JsonValue value){
        if(ModifierSign.PLUS.sign.equals(value.name)){
            JsonValue fieldData = value.parent;
            fieldData.remove(value.name);

            JsonValue parent = fieldData.parent;
            if(fieldData.size == 0) parent.remove(fieldData.name);

            parent.addChild(fieldData.name + "." + value.name, value);
        }

        for(JsonValue child : value){
            transformPatch(child);
        }
        return value;
    }

    public static JsonValue simplifyPatch(JsonValue value){
        int singleCount = 0;
        JsonValue singleEnd = value;
        while(singleEnd.child != null && singleEnd.size == 1 && !singleEnd.isValue()){
            singleEnd = singleEnd.child;
            singleCount++;
        }

        if(singleCount >= simplifySingleCount){
            StringBuilder name = new StringBuilder();
            JsonValue current = value;
            while(true){
                name.append(current.name);
                current = current.child;
                if(current != singleEnd) name.append("."); // dot syntax
                else break;
            }

            value.setName(name.toString());
            value.child = singleEnd;
        }

        for(JsonValue child : value){
            simplifyPatch(child);
        }
        return value;
    }
}