package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
    public static final int CARRIED_ITEM = -1;
    public static final int PLAYER_INVENTORY = -2;
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int param0, int param1, int param2, ItemStack param3) {
        this.containerId = param0;
        this.stateId = param1;
        this.slot = param2;
        this.itemStack = param3.copy();
    }

    public ClientboundContainerSetSlotPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.stateId = param0.readVarInt();
        this.slot = param0.readShort();
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeVarInt(this.stateId);
        param0.writeShort(this.slot);
        param0.writeItem(this.itemStack);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerSetSlot(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }
}
