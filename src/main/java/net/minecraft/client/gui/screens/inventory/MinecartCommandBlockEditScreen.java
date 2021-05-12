package net.minecraft.client.gui.screens.inventory;

import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartCommandBlockEditScreen extends AbstractCommandBlockEditScreen {
    private final BaseCommandBlock commandBlock;

    public MinecartCommandBlockEditScreen(BaseCommandBlock param0) {
        this.commandBlock = param0;
    }

    @Override
    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override
    int getPreviousY() {
        return 150;
    }

    @Override
    protected void init() {
        super.init();
        this.commandEdit.setValue(this.getCommandBlock().getCommand());
    }

    @Override
    protected void populateAndSendPacket(BaseCommandBlock param0) {
        if (param0 instanceof MinecartCommandBlock.MinecartCommandBase var0) {
            this.minecraft
                .getConnection()
                .send(new ServerboundSetCommandMinecartPacket(var0.getMinecart().getId(), this.commandEdit.getValue(), param0.isTrackOutput()));
        }

    }
}
