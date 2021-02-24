package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int param0, int param1, ItemStack param2) {
        this.containerId = param0;
        this.slot = param1;
        this.itemStack = param2.copy();
    }

    public ClientboundContainerSetSlotPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.slot = param0.readShort();
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeShort(this.slot);
        param0.writeItem(this.itemStack);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerSetSlot(this);
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
