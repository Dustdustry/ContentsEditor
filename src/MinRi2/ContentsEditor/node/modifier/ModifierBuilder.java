package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.ui.*;

public abstract class ModifierBuilder<T>{
    protected T value;
    protected Button resetButton;
    protected final ModifyConsumer<T> consumer;

    public ModifierBuilder(ModifyConsumer<T> consumer){
        this.consumer = consumer;
    }

    public void build(Table table){
        value = consumer.getValue();
    }

    public static class TextBuilder extends ModifierBuilder<String>{

        public TextBuilder(ModifyConsumer<String> consumer){
            super(consumer);
        }

        @Override
        public void build(Table table){
            super.build(table);

            TextField field = table.field(value, t -> {
                consumer.onModify(t);
                resetButton.visible = consumer.isModified();
            }).valid(consumer::checkValue).pad(4f).width(100f).get();

            resetButton = addResetButton(table, consumer, () -> {
                value = consumer.getValue();
                field.setText(value);
            });
        }
    };

    public static class BooleanBuilder extends ModifierBuilder<Boolean>{

        public BooleanBuilder(ModifyConsumer<Boolean> consumer){
            super(consumer);
        }

        @Override
        public void build(Table table){
            super.build(table);

            BorderImage image = new BorderImage();
            image.addAction(Actions.color(value ? Color.green : Color.red, 0.3f));

            Cons<Boolean> setColor = bool -> {
                value = bool;
                image.addAction(Actions.color(bool ? Color.green : Color.red, 0.3f));
                resetButton.visible = consumer.isModified();
            };

            table.button(b -> {
                b.add(image).size(32f).pad(8f).expandX().left();
                b.label(() -> value ? "[green]true" : "[red]false").expandX();
            }, Styles.clearNonei, () -> {
                setColor.get(!value);
                consumer.onModify(value);
            }).grow();

            resetButton = addResetButton(table, consumer, () -> setColor.get(consumer.getValue()));
        }
    }

    public static class ContentBuilder extends ModifierBuilder<UnlockableContent>{
        protected Table contentTable;

        public ContentBuilder(ModifyConsumer<UnlockableContent> consumer){
            super(consumer);
        }

        @Override
        public void build(Table table){
            super.build(table);

            contentTable = table.button(b -> {}, Styles.clearNonei, () -> {
                Class<?> type = consumer.getTypeMeta();
                ContentType contentType = PatchJsonIO.getContentType(type);

                EUI.selector.select(contentType, type, c -> c != value, c -> {
                    setValue(c);
                    consumer.onModify(value);
                    return true;
                });
            }).grow().get();

            resetButton = addResetButton(table, consumer, () -> setValue(consumer.getValue()));
            setValue(consumer.getValue());
        }

        private void setValue(UnlockableContent value){
            this.value = value;
            resetButton.visible = consumer.isModified();

            if(contentTable == null) return;

            contentTable.clearChildren();

            TextureRegion icon;
            String displayName;
            if(value == null){
                icon = Icon.none.getRegion();
                displayName = "null";
            }else{
                icon = value.uiIcon;
                displayName = value.localizedName;
            }

            contentTable.image(icon).scaling(Scaling.fit).size(40f).pad(8f).expandX().left();
            contentTable.add(displayName).pad(4f).ellipsis(true).width(64f);
        }
    }

    private static Button addResetButton(Table table, ModifyConsumer<?> consumer, Runnable clicked){
        return table.button(Icon.undo, Styles.clearNonei, () -> {
            consumer.resetModify();
            clicked.run();
        }).width(32f).pad(4f).growY().expandX().right().visible(consumer::isModified).tooltip("@node-modifier.undo", true).get();
    }
}