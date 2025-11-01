package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ContentsEditor.ui.editor.*;
import arc.graphics.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.gen.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeModifier{
    private NodeModifier(){
    }

    public static boolean modifiable(CTNode node){
        return BaseModifier.modifiable(node);
    }

    public static void setupModifierTable(Table table, NodeData nodeData){
        BaseModifier<?> modifier = BaseModifier.getModifier(nodeData);
        assert modifier != null;

        table.table(infoTable -> {
            // Add node info
            NodeDisplay.displayNameType(infoTable, nodeData);
        }).fill();

        table.table(modifier::build).pad(4).grow();

        table.image().width(4f).color(Color.darkGray).growY().right();
        table.row();
        Cell<?> horizontalLine = table.image().height(4f).color(Color.darkGray).growX();
        horizontalLine.colspan(table.getColumns());

        table.background(Tex.whiteui);
        table.setColor(modifier.isModified() ? EPalettes.modified : EPalettes.unmodified);

        modifier.onModified(modified -> {
            Color color = modified ? EPalettes.modified : EPalettes.unmodified;
            table.addAction(Actions.color(color, 0.2f));
        });
    }

}
