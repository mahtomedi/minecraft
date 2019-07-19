package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetEquippedItemPacket implements Packet<ClientGamePacketListener> {
    private int entity;
    private EquipmentSlot slot;
    private ItemStack itemStack = ItemStack.EMPTY;

    public ClientboundSetEquippedItemPacket() {
    }

    public ClientboundSetEquippedItemPacket(int param0, EquipmentSlot param1, ItemStack param2) {
        this.entity = param0;
        this.slot = param1;
        this.itemStack = param2.copy();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entity = param0.readVarInt();
        this.slot = param0.readEnum(EquipmentSlot.class);
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entity);
        param0.writeEnum(this.slot);
        param0.writeItem(this.itemStack);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEquippedItem(this);
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return this.itemStack;
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntity() {
        return this.entity;
    }

    @OnlyIn(Dist.CLIENT)
    public EquipmentSlot getSlot() {
        return this.slot;
    }
}
