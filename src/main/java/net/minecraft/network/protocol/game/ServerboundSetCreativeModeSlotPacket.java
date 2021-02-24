package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
    private final int slotNum;
    private final ItemStack itemStack;

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetCreativeModeSlotPacket(int param0, ItemStack param1) {
        this.slotNum = param0;
        this.itemStack = param1.copy();
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCreativeModeSlot(this);
    }

    public ServerboundSetCreativeModeSlotPacket(FriendlyByteBuf param0) {
        this.slotNum = param0.readShort();
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeShort(this.slotNum);
        param0.writeItem(this.itemStack);
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
