package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public ClientboundContainerSetContentPacket(int param0, int param1, NonNullList<ItemStack> param2, ItemStack param3) {
        this.containerId = param0;
        this.stateId = param1;
        this.items = NonNullList.withSize(param2.size(), ItemStack.EMPTY);

        for(int var0 = 0; var0 < param2.size(); ++var0) {
            this.items.set(var0, param2.get(var0).copy());
        }

        this.carriedItem = param3.copy();
    }

    public ClientboundContainerSetContentPacket(FriendlyByteBuf param0) {
        this.containerId = param0.readUnsignedByte();
        this.stateId = param0.readVarInt();
        this.items = param0.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readItem);
        this.carriedItem = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeVarInt(this.stateId);
        param0.writeCollection(this.items, FriendlyByteBuf::writeItem);
        param0.writeItem(this.carriedItem);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerContent(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public int getStateId() {
        return this.stateId;
    }
}
