package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
    private Item item;
    private int duration;

    public ClientboundCooldownPacket() {
    }

    public ClientboundCooldownPacket(Item param0, int param1) {
        this.item = param0;
        this.duration = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.item = Item.byId(param0.readVarInt());
        this.duration = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(Item.getId(this.item));
        param0.writeVarInt(this.duration);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleItemCooldown(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Item getItem() {
        return this.item;
    }

    @OnlyIn(Dist.CLIENT)
    public int getDuration() {
        return this.duration;
    }
}
