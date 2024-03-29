package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
    private static final Component SET_COMMAND_LABEL = Component.translatable("advMode.setCommand");
    private static final Component COMMAND_LABEL = Component.translatable("advMode.command");
    private static final Component PREVIOUS_OUTPUT_LABEL = Component.translatable("advMode.previousOutput");
    protected EditBox commandEdit;
    protected EditBox previousEdit;
    protected Button doneButton;
    protected Button cancelButton;
    protected CycleButton<Boolean> outputButton;
    CommandSuggestions commandSuggestions;

    public AbstractCommandBlockEditScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public void tick() {
        if (!this.getCommandBlock().isValid()) {
            this.onClose();
        }

    }

    abstract BaseCommandBlock getCommandBlock();

    abstract int getPreviousY();

    @Override
    protected void init() {
        this.doneButton = this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build()
        );
        this.cancelButton = this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onClose()).bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build()
        );
        boolean var0 = this.getCommandBlock().isTrackOutput();
        this.outputButton = this.addRenderableWidget(
            CycleButton.booleanBuilder(Component.literal("O"), Component.literal("X"))
                .withInitialValue(var0)
                .displayOnlyValue()
                .create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, Component.translatable("advMode.trackOutput"), (param0, param1) -> {
                    BaseCommandBlock var0x = this.getCommandBlock();
                    var0x.setTrackOutput(param1);
                    this.updatePreviousOutput(param1);
                })
        );
        this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, Component.translatable("advMode.command")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.commandEdit.setMaxLength(32500);
        this.commandEdit.setResponder(this::onEdited);
        this.addWidget(this.commandEdit);
        this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, Component.translatable("advMode.previousOutput"));
        this.previousEdit.setMaxLength(32500);
        this.previousEdit.setEditable(false);
        this.previousEdit.setValue("-");
        this.addWidget(this.previousEdit);
        this.setInitialFocus(this.commandEdit);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();
        this.updatePreviousOutput(var0);
    }

    @Override
    protected Component getUsageNarration() {
        return this.commandSuggestions.isVisible() ? this.commandSuggestions.getUsageNarration() : super.getUsageNarration();
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.commandEdit.getValue();
        this.init(param0, param1, param2);
        this.commandEdit.setValue(var0);
        this.commandSuggestions.updateCommandInfo();
    }

    protected void updatePreviousOutput(boolean param0) {
        this.previousEdit.setValue(param0 ? this.getCommandBlock().getLastOutput().getString() : "-");
    }

    protected void onDone() {
        BaseCommandBlock var0 = this.getCommandBlock();
        this.populateAndSendPacket(var0);
        if (!var0.isTrackOutput()) {
            var0.setLastOutput(null);
        }

        this.minecraft.setScreen(null);
    }

    protected abstract void populateAndSendPacket(BaseCommandBlock var1);

    private void onEdited(String param0) {
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.commandSuggestions.keyPressed(param0, param1, param2)) {
            return true;
        } else if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        return this.commandSuggestions.mouseScrolled(param3) ? true : super.mouseScrolled(param0, param1, param2, param3);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.commandSuggestions.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
        param0.drawString(this.font, COMMAND_LABEL, this.width / 2 - 150 + 1, 40, 10526880);
        this.commandEdit.render(param0, param1, param2, param3);
        int var0 = 75;
        if (!this.previousEdit.getValue().isEmpty()) {
            var0 += 5 * 9 + 1 + this.getPreviousY() - 135;
            param0.drawString(this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150 + 1, var0 + 4, 10526880);
            this.previousEdit.render(param0, param1, param2, param3);
        }

        this.commandSuggestions.render(param0, param1, param2);
    }
}
