package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.modifier.*;
import arc.util.serialization.JsonValue.*;
import mindustry.ctype.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public class ContentTypeModifier extends EqualModifier<UnlockableContent>{
    public ContentTypeModifier(){
        builder = new ModifierBuilder.ContentBuilder(this);
        valueType = ValueType.stringValue;
    }

    @Override
    public UnlockableContent cast(Object object){
        return (UnlockableContent)object;
    }

}
