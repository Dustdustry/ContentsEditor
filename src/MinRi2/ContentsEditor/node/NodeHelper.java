package MinRi2.ContentsEditor.node;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.Json.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeHelper{
    private static Object root;
    private static ContentParser parser;
    private static ObjectMap<String, ContentType> nameToType;

    public static ObjectMap<Class<?>, ContentType> contentClassTypeMap = ObjectMap.of(
        Block.class, ContentType.block,
        Item.class, ContentType.item,
        Liquid.class, ContentType.liquid,
        StatusEffect.class, ContentType.status,
        UnitType.class, ContentType.unit
    );

    public static Object getRootObj(){
        if(root == null) root = Reflect.get(ContentPatcher.class, "root");
        return root;
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
}
