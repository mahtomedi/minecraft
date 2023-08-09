package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JigsawBlockEditScreen extends Screen {
    private static final int MAX_LEVELS = 7;
    private static final Component JOINT_LABEL = Component.translatable("jigsaw_block.joint_label");
    private static final Component POOL_LABEL = Component.translatable("jigsaw_block.pool");
    private static final Component NAME_LABEL = Component.translatable("jigsaw_block.name");
    private static final Component TARGET_LABEL = Component.translatable("jigsaw_block.target");
    private static final Component FINAL_STATE_LABEL = Component.translatable("jigsaw_block.final_state");
    private final JigsawBlockEntity jigsawEntity;
    private EditBox nameEdit;
    private EditBox targetEdit;
    private EditBox poolEdit;
    private EditBox finalStateEdit;
    int levels;
    private boolean keepJigsaws = true;
    private CycleButton<JigsawBlockEntity.JointType> jointButton;
    private Button doneButton;
    private Button generateButton;
    private JigsawBlockEntity.JointType joint;

    public JigsawBlockEditScreen(JigsawBlockEntity param0) {
        super(GameNarrator.NO_TITLE);
        this.jigsawEntity = param0;
    }

    private void onDone() {
        this.sendToServer();
        this.minecraft.setScreen(null);
    }

    private void onCancel() {
        this.minecraft.setScreen(null);
    }

    private void sendToServer() {
        this.minecraft
            .getConnection()
            .send(
                new ServerboundSetJigsawBlockPacket(
                    this.jigsawEntity.getBlockPos(),
                    new ResourceLocation(this.nameEdit.getValue()),
                    new ResourceLocation(this.targetEdit.getValue()),
                    new ResourceLocation(this.poolEdit.getValue()),
                    this.finalStateEdit.getValue(),
                    this.joint
                )
            );
    }

    private void sendGenerate() {
        this.minecraft.getConnection().send(new ServerboundJigsawGeneratePacket(this.jigsawEntity.getBlockPos(), this.levels, this.keepJigsaws));
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    protected void init() {
        this.poolEdit = new EditBox(this.font, this.width / 2 - 153, 20, 300, 20, Component.translatable("jigsaw_block.pool"));
        this.poolEdit.setMaxLength(128);
        this.poolEdit.setValue(this.jigsawEntity.getPool().location().toString());
        this.poolEdit.setResponder(param0 -> this.updateValidity());
        this.addWidget(this.poolEdit);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 153, 55, 300, 20, Component.translatable("jigsaw_block.name"));
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.jigsawEntity.getName().toString());
        this.nameEdit.setResponder(param0 -> this.updateValidity());
        this.addWidget(this.nameEdit);
        this.targetEdit = new EditBox(this.font, this.width / 2 - 153, 90, 300, 20, Component.translatable("jigsaw_block.target"));
        this.targetEdit.setMaxLength(128);
        this.targetEdit.setValue(this.jigsawEntity.getTarget().toString());
        this.targetEdit.setResponder(param0 -> this.updateValidity());
        this.addWidget(this.targetEdit);
        this.finalStateEdit = new EditBox(this.font, this.width / 2 - 153, 125, 300, 20, Component.translatable("jigsaw_block.final_state"));
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
        this.addWidget(this.finalStateEdit);
        this.joint = this.jigsawEntity.getJoint();
        int var0 = this.font.width(JOINT_LABEL) + 10;
        this.jointButton = this.addRenderableWidget(
            CycleButton.builder(JigsawBlockEntity.JointType::getTranslatedName)
                .withValues(JigsawBlockEntity.JointType.values())
                .withInitialValue(this.joint)
                .displayOnlyValue()
                .create(this.width / 2 - 152 + var0, 150, 300 - var0, 20, JOINT_LABEL, (param0, param1) -> this.joint = param1)
        );
        boolean var1 = JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical();
        this.jointButton.active = var1;
        this.jointButton.visible = var1;
        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 154, 180, 100, 20, CommonComponents.EMPTY, 0.0) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("jigsaw_block.levels", JigsawBlockEditScreen.this.levels));
            }

            @Override
            protected void applyValue() {
                JigsawBlockEditScreen.this.levels = Mth.floor(Mth.clampedLerp(0.0, 7.0, this.value));
            }
        });
        this.addRenderableWidget(
            CycleButton.onOffBuilder(this.keepJigsaws)
                .create(this.width / 2 - 50, 180, 100, 20, Component.translatable("jigsaw_block.keep_jigsaws"), (param0, param1) -> this.keepJigsaws = param1)
        );
        this.generateButton = this.addRenderableWidget(Button.builder(Component.translatable("jigsaw_block.generate"), param0 -> {
            this.onDone();
            this.sendGenerate();
        }).bounds(this.width / 2 + 54, 180, 100, 20).build());
        this.doneButton = this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build()
        );
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.setInitialFocus(this.poolEdit);
        this.updateValidity();
    }

    private void updateValidity() {
        boolean var0 = ResourceLocation.isValidResourceLocation(this.nameEdit.getValue())
            && ResourceLocation.isValidResourceLocation(this.targetEdit.getValue())
            && ResourceLocation.isValidResourceLocation(this.poolEdit.getValue());
        this.doneButton.active = var0;
        this.generateButton.active = var0;
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.nameEdit.getValue();
        String var1 = this.targetEdit.getValue();
        String var2 = this.poolEdit.getValue();
        String var3 = this.finalStateEdit.getValue();
        int var4 = this.levels;
        JigsawBlockEntity.JointType var5 = this.joint;
        this.init(param0, param1, param2);
        this.nameEdit.setValue(var0);
        this.targetEdit.setValue(var1);
        this.poolEdit.setValue(var2);
        this.finalStateEdit.setValue(var3);
        this.levels = var4;
        this.joint = var5;
        this.jointButton.setValue(var5);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (!this.doneButton.active || param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawString(this.font, POOL_LABEL, this.width / 2 - 153, 10, 10526880);
        this.poolEdit.render(param0, param1, param2, param3);
        param0.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 45, 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        param0.drawString(this.font, TARGET_LABEL, this.width / 2 - 153, 80, 10526880);
        this.targetEdit.render(param0, param1, param2, param3);
        param0.drawString(this.font, FINAL_STATE_LABEL, this.width / 2 - 153, 115, 10526880);
        this.finalStateEdit.render(param0, param1, param2, param3);
        if (JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical()) {
            param0.drawString(this.font, JOINT_LABEL, this.width / 2 - 153, 156, 16777215);
        }

    }
}
