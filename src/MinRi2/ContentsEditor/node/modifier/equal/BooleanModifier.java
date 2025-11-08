package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.modifier.*;
import arc.util.serialization.JsonValue.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public class BooleanModifier extends EqualModifier<Boolean>{
    public BooleanModifier(){
        builder = new ModifierBuilder.BooleanBuilder(this);
        valueType = ValueType.booleanValue;
    }

    @Override
    public Boolean cast(Object object){
        return (Boolean)object;
    }
}
