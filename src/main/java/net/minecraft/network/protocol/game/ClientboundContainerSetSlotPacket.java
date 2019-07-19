package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
    private int containerId;
    private int slot;
    private ItemStack itemStack = ItemStack.EMPTY;

    public ClientboundContainerSetSlotPacket() {
    }

    public ClientboundContainerSetSlotPacket(int param0, int param1, ItemStack param2) {
        this.containerId = param0;
        this.slot = param1;
        this.itemStack = param2.copy();
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerSetSlot(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readByte();
        this.slot = param0.readShort();
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeShort(this.slot);
        param0.writeItem(this.itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSlot() {
        return this.slot;
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return this.itemStack;
    }
}
