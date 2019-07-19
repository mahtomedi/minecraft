package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JigsawBlockEditScreen extends Screen {
    private final JigsawBlockEntity jigsawEntity;
    private EditBox attachementTypeEdit;
    private EditBox targetPoolEdit;
    private EditBox finalStateEdit;
    private Button doneButton;

    public JigsawBlockEditScreen(JigsawBlockEntity param0) {
        super(NarratorChatListener.NO_TITLE);
        this.jigsawEntity = param0;
    }

    @Override
    public void tick() {
        this.attachementTypeEdit.tick();
        this.targetPoolEdit.tick();
        this.finalStateEdit.tick();
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
                    new ResourceLocation(this.attachementTypeEdit.getValue()),
                    new ResourceLocation(this.targetPoolEdit.getValue()),
                    this.finalStateEdit.getValue()
                )
            );
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, I18n.get("gui.done"), param0 -> this.onDone()));
        this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, I18n.get("gui.cancel"), param0 -> this.onCancel()));
        this.targetPoolEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, I18n.get("jigsaw_block.target_pool"));
        this.targetPoolEdit.setMaxLength(128);
        this.targetPoolEdit.setValue(this.jigsawEntity.getTargetPool().toString());
        this.targetPoolEdit.setResponder(param0 -> this.updateValidity());
        this.children.add(this.targetPoolEdit);
        this.attachementTypeEdit = new EditBox(this.font, this.width / 2 - 152, 80, 300, 20, I18n.get("jigsaw_block.attachement_type"));
        this.attachementTypeEdit.setMaxLength(128);
        this.attachementTypeEdit.setValue(this.jigsawEntity.getAttachementType().toString());
        this.attachementTypeEdit.setResponder(param0 -> this.updateValidity());
        this.children.add(this.attachementTypeEdit);
        this.finalStateEdit = new EditBox(this.font, this.width / 2 - 152, 120, 300, 20, I18n.get("jigsaw_block.final_state"));
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
        this.children.add(this.finalStateEdit);
        this.setInitialFocus(this.targetPoolEdit);
        this.updateValidity();
    }

    protected void updateValidity() {
        this.doneButton.active = ResourceLocation.isValidResourceLocation(this.attachementTypeEdit.getValue())
            & ResourceLocation.isValidResourceLocation(this.targetPoolEdit.getValue());
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.attachementTypeEdit.getValue();
        String var1 = this.targetPoolEdit.getValue();
        String var2 = this.finalStateEdit.getValue();
        this.init(param0, param1, param2);
        this.attachementTypeEdit.setValue(var0);
        this.targetPoolEdit.setValue(var1);
        this.finalStateEdit.setValue(var2);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawString(this.font, I18n.get("jigsaw_block.target_pool"), this.width / 2 - 153, 30, 10526880);
        this.targetPoolEdit.render(param0, param1, param2);
        this.drawString(this.font, I18n.get("jigsaw_block.attachement_type"), this.width / 2 - 153, 70, 10526880);
        this.attachementTypeEdit.render(param0, param1, param2);
        this.drawString(this.font, I18n.get("jigsaw_block.final_state"), this.width / 2 - 153, 110, 10526880);
        this.finalStateEdit.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }
}
