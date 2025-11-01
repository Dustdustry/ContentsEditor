package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ContentsEditor.ui.EUI.*;
import arc.*;
import arc.graphics.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import java.util.Map.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeCard extends Table{
    public static float buttonWidth = 320f;
    public static float buttonHeight = buttonWidth / 3f;

    private final Table cardCont, nodesTable; // workingTable / childrenNodesTable
    public boolean isChild, editing;
    public NodeCard parent, childCard;
    private NodeData nodeData;
    private Seq<Entry<String, CTNode>> sortedChildren;

    private NodeData lastChildData;

    private String searchText = "";

    public NodeCard(){
        cardCont = new Table();
        nodesTable = new Table();

        top().left();
        cardCont.top();
        nodesTable.top().left();
    }

    public void setNodeData(NodeData nodeData){
        this.nodeData = nodeData;
    }

    public void setParent(NodeCard parent){
        isChild = parent != null;
        this.parent = parent;
    }

    public NodeCard getFrontCard(){
        NodeCard card = this;

        while(card.editing && card.childCard != null){
            card = card.childCard;
        }

        return card;
    }

    private void editChildNode(NodeData childNodeData){
        if(childCard == null){
            childCard = new NodeCard();
            childCard.setParent(this);
        }else if(childCard.editing){
            childCard.editChildNode(null);
        }

        editing = childNodeData != null;

        childCard.setNodeData(childNodeData);
        childCard.rebuild();

        rebuild();
    }

    public void extractWorking(){
        if(parent != null){
            parent.lastChildData = nodeData;
            parent.editChildNode(null);
        }
    }

    public void editLastData(){
        // 仅支持最前面的卡片
        if((childCard == null || !childCard.editing) && lastChildData != null){
            editChildNode(lastChildData);
        }
    }

    public void rebuild(){
        clearChildren();

        if(nodeData == null){
            return;
        }

        defaults().growX();

        buildTitle(this);

        row();

        rebuildCont();
        add(cardCont).grow();
    }

    private void rebuildCont(){
        cardCont.clearChildren();

        cardCont.defaults().padLeft(16f);

        if(editing){
            childCard.rebuildCont();
            cardCont.add(childCard).grow();
        }else{
            cardCont.table(this::setupSearchTable).pad(8f).growX();

            cardCont.row();

            cardCont.add(nodesTable).fill();

            // 下一帧再重构
            Core.app.post(this::rebuildNodesTable);
        }
    }

    private void setupSearchTable(Table table){
        table.image(Icon.zoom).size(64f);

        TextField field = table.add(EUI.deboundTextField(searchText, text -> {
            searchText = text;
            rebuildNodesTable();
        })).pad(8f).growX().get();

        if(Core.app.isDesktop()){
            Core.scene.setKeyboardFocus(field);
        }

        table.button(Icon.cancel, Styles.clearNonei, () -> {
            searchText = "";
            field.setText(searchText);
            rebuildNodesTable();
        }).size(64f);
    }

    private void rebuildNodesTable(){
        nodesTable.clearChildren();

        // 下一帧可能正好被清除
        if(nodeData == null){
            return;
        }

        int columns = Math.max(1, (int)(nodesTable.getWidth() / Scl.scl() / buttonWidth));

        nodesTable.defaults().size(buttonWidth, buttonWidth / 4).pad(4f).margin(8f).top().left();

        Seq<Entry<String, CTNode>> children = getSortedChildren();

        int index = 0;
        for(Entry<String, CTNode> entry : children){
            CTNode childNode = entry.getValue();
            String childNodeName = entry.getKey();

            if(!searchText.isEmpty()){
                String displayName = NodeHelper.getDisplayName(childNode);

                if(!Strings.matches(searchText, childNodeName)
                && (displayName == null || !Strings.matches(searchText, displayName))){
                    continue;
                }
            }

            if(NodeModifier.modifiable(childNode)){
                addEditTable(nodesTable, childNodeName);
            }else{
                addChildButton(nodesTable, childNode, childNodeName);
            }

            if(++index % columns == 0){
                nodesTable.row();
            }
        }
    }

    private void addEditTable(Table table, String childNodeName){
        NodeData childNodeData = nodeData.getChild(childNodeName);

        table.table(t -> NodeModifier.setupModifierTable(t, childNodeData));
    }

    private void addChildButton(Table table, CTNode childNode, String childNodeName){
        ImageButtonStyle style = nodeData.hasJsonChild(childNodeName) ? EStyles.cardModifiedButtoni : EStyles.cardButtoni;

        table.button(b -> {
            NodeDisplay.display(b, childNode, childNodeName);

            b.image().width(4f).color(Color.darkGray).growY().right();
            b.row();
            Cell<?> horizontalLine = b.image().height(4f).color(Color.darkGray).growX();
            horizontalLine.colspan(b.getColumns());
        }, style, () -> {
            NodeData childNodeData = nodeData.getChild(childNodeName);
            editChildNode(childNodeData);

            rebuildCont();
        });
    }

    private void buildTitle(Table table){
        Color titleColor = !isChild ? EPalettes.purpleAccent2 : EPalettes.purpleAccent3;
        table.table(Tex.whiteui, nodeTitle -> {
            nodeTitle.table(Tex.whiteui, nameTable -> {
                NodeDisplay.display(nameTable, nodeData);

                nameTable.image().width(4f).color(Color.darkGray).growY().right();
                nameTable.row();
                Cell<?> horizontalLine = nameTable.image().height(4f).color(Color.darkGray).growX();
                horizontalLine.colspan(nameTable.getColumns());
            }).color(Pal.darkestGray).size(buttonWidth, buttonHeight).pad(8f).expandX().left();

            nodeTitle.table(buttonTable -> {
                buttonTable.defaults().size(64f).pad(8f);

                // Clear data
                buttonTable.button(Icon.refresh, Styles.cleari, () -> {
                    Vars.ui.showConfirm(Core.bundle.format("node-card.clear-data.confirm", nodeData.nodeName), () -> {
                        nodeData.clearJson();
                        getFrontCard().rebuildNodesTable();
                    });
                }).tooltip("@node-card.clear-data", true);

                if(parent != null){
                    buttonTable.button(Icon.upOpen, Styles.cleari, this::extractWorking).tooltip("@node-card.extract", false);
                }
            }).growY();
        }).color(titleColor);
    }

    private Seq<Entry<String, CTNode>> getSortedChildren(){
        if(sortedChildren == null){
            sortedChildren = new Seq<>();
        }

        NodeHelper.getEntries(nodeData.node, sortedChildren);

        sortedChildren.sort((e1, e2) -> {
            CTNode n1 = e1.getValue();
            CTNode n2 = e2.getValue();

            int modifiable = Boolean.compare(!NodeModifier.modifiable(n1), !NodeModifier.modifiable(n2));
            if(modifiable != 0) return modifiable;

            String name1 = e1.getKey();
            String name2 = e2.getKey();

            return Boolean.compare(!nodeData.hasJsonChild(name1), !nodeData.hasJsonChild(name2));
        });

        return sortedChildren;
    }

    @Override
    public String toString(){
        return "NodeCard{" +
        "nodeData=" + nodeData +
        '}';
    }
}