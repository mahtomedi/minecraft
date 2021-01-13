package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandBlockEditScreen extends AbstractCommandBlockEditScreen {
    private final CommandBlockEntity autoCommandBlock;
    private Button modeButton;
    private Button conditionalButton;
    private Button autoexecButton;
    private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
    private boolean conditional;
    private boolean autoexec;

    public CommandBlockEditScreen(CommandBlockEntity param0) {
        this.autoCommandBlock = param0;
    }

    @Override
    BaseCommandBlock getCommandBlock() {
        return this.autoCommandBlock.getCommandBlock();
    }

    @Override
    int getPreviousY() {
        return 135;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addButton(
            new Button(this.width / 2 - 50 - 100 - 4, 165, 100, 20, new TranslatableComponent("advMode.mode.sequence"), param0 -> {
                this.nextMode();
                this.updateMode();
            })
        );
        this.conditionalButton = this.addButton(
            new Button(this.width / 2 - 50, 165, 100, 20, new TranslatableComponent("advMode.mode.unconditional"), param0 -> {
                this.conditional = !this.conditional;
                this.updateConditional();
            })
        );
        this.autoexecButton = this.addButton(
            new Button(this.width / 2 + 50 + 4, 165, 100, 20, new TranslatableComponent("advMode.mode.redstoneTriggered"), param0 -> {
                this.autoexec = !this.autoexec;
                this.updateAutoexec();
            })
        );
        this.doneButton.active = false;
        this.outputButton.active = false;
        this.modeButton.active = false;
        this.conditionalButton.active = false;
        this.autoexecButton.active = false;
    }

    public void updateGui() {
        BaseCommandBlock var0 = this.autoCommandBlock.getCommandBlock();
        this.commandEdit.setValue(var0.getCommand());
        this.trackOutput = var0.isTrackOutput();
        this.mode = this.autoCommandBlock.getMode();
        this.conditional = this.autoCommandBlock.isConditional();
        this.autoexec = this.autoCommandBlock.isAutomatic();
        this.updateCommandOutput();
        this.updateMode();
        this.updateConditional();
        this.updateAutoexec();
        this.doneButton.active = true;
        this.outputButton.active = true;
        this.modeButton.active = true;
        this.conditionalButton.active = true;
        this.autoexecButton.active = true;
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        super.resize(param0, param1, param2);
        this.updateCommandOutput();
        this.updateMode();
        this.updateConditional();
        this.updateAutoexec();
        this.doneButton.active = true;
        this.outputButton.active = true;
        this.modeButton.active = true;
        this.conditionalButton.active = true;
        this.autoexecButton.active = true;
    }

    @Override
    protected void populateAndSendPacket(BaseCommandBlock param0) {
        this.minecraft
            .getConnection()
            .send(
                new ServerboundSetCommandBlockPacket(
                    new BlockPos(param0.getPosition()), this.commandEdit.getValue(), this.mode, param0.isTrackOutput(), this.conditional, this.autoexec
                )
            );
    }

    private void updateMode() {
        switch(this.mode) {
            case SEQUENCE:
                this.modeButton.setMessage(new TranslatableComponent("advMode.mode.sequence"));
                break;
            case AUTO:
                this.modeButton.setMessage(new TranslatableComponent("advMode.mode.auto"));
                break;
            case REDSTONE:
                this.modeButton.setMessage(new TranslatableComponent("advMode.mode.redstone"));
        }

    }

    private void nextMode() {
        switch(this.mode) {
            case SEQUENCE:
                this.mode = CommandBlockEntity.Mode.AUTO;
                break;
            case AUTO:
                this.mode = CommandBlockEntity.Mode.REDSTONE;
                break;
            case REDSTONE:
                this.mode = CommandBlockEntity.Mode.SEQUENCE;
        }

    }

    private void updateConditional() {
        if (this.conditional) {
            this.conditionalButton.setMessage(new TranslatableComponent("advMode.mode.conditional"));
        } else {
            this.conditionalButton.setMessage(new TranslatableComponent("advMode.mode.unconditional"));
        }

    }

    private void updateAutoexec() {
        if (this.autoexec) {
            this.autoexecButton.setMessage(new TranslatableComponent("advMode.mode.autoexec.bat"));
        } else {
            this.autoexecButton.setMessage(new TranslatableComponent("advMode.mode.redstoneTriggered"));
        }

    }
}
