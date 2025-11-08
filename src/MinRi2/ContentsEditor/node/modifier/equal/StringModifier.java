package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.modifier.*;
import arc.util.serialization.JsonValue.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public class StringModifier extends EqualModifier<String>{
    public StringModifier(){
        builder = new ModifierBuilder.TextBuilder(this);
        valueType = ValueType.stringValue;
    }

    @Override
    public String cast(Object object){
        return String.valueOf(object);
    }
}
