package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
    private int containerId;
    private List<ItemStack> items;

    public ClientboundContainerSetContentPacket() {
    }

    public ClientboundContainerSetContentPacket(int param0, NonNullList<ItemStack> param1) {
        this.containerId = param0;
        this.items = NonNullList.withSize(param1.size(), ItemStack.EMPTY);

        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            this.items.set(var0, param1.get(var0).copy());
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readUnsignedByte();
        int var0 = param0.readShort();
        this.items = NonNullList.withSize(var0, ItemStack.EMPTY);

        for(int var1 = 0; var1 < var0; ++var1) {
            this.items.set(var1, param0.readItem());
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeShort(this.items.size());

        for(ItemStack var0 : this.items) {
            param0.writeItem(var0);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleContainerContent(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @OnlyIn(Dist.CLIENT)
    public List<ItemStack> getItems() {
        return this.items;
    }
}
