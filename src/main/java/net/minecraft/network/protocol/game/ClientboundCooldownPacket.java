package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
    private final Item item;
    private final int duration;

    public ClientboundCooldownPacket(Item param0, int param1) {
        this.item = param0;
        this.duration = param1;
    }

    public ClientboundCooldownPacket(FriendlyByteBuf param0) {
        this.item = param0.readById(BuiltInRegistries.ITEM);
        this.duration = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeId(BuiltInRegistries.ITEM, this.item);
        param0.writeVarInt(this.duration);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleItemCooldown(this);
    }

    public Item getItem() {
        return this.item;
    }

    public int getDuration() {
        return this.duration;
    }
}
