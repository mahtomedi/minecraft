package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetEquipmentPacket implements Packet<ClientGamePacketListener> {
    private int entity;
    private final List<Pair<EquipmentSlot, ItemStack>> slots;

    public ClientboundSetEquipmentPacket() {
        this.slots = Lists.newArrayList();
    }

    public ClientboundSetEquipmentPacket(int param0, List<Pair<EquipmentSlot, ItemStack>> param1) {
        this.entity = param0;
        this.slots = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entity = param0.readVarInt();
        EquipmentSlot[] var0 = EquipmentSlot.values();

        int var1;
        do {
            var1 = param0.readByte();
            EquipmentSlot var2 = var0[var1 & 127];
            ItemStack var3 = param0.readItem();
            this.slots.add(Pair.of(var2, var3));
        } while((var1 & -128) != 0);

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.entity);
        int var0 = this.slots.size();

        for(int var1 = 0; var1 < var0; ++var1) {
            Pair<EquipmentSlot, ItemStack> var2 = this.slots.get(var1);
            EquipmentSlot var3 = var2.getFirst();
            boolean var4 = var1 != var0 - 1;
            int var5 = var3.ordinal();
            param0.writeByte(var4 ? var5 | -128 : var5);
            param0.writeItem(var2.getSecond());
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEquipment(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntity() {
        return this.entity;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
        return this.slots;
    }
}
