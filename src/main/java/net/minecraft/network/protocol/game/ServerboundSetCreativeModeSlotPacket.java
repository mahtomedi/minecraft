package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
    private int slotNum;
    private ItemStack itemStack = ItemStack.EMPTY;

    public ServerboundSetCreativeModeSlotPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetCreativeModeSlotPacket(int param0, ItemStack param1) {
        this.slotNum = param0;
        this.itemStack = param1.copy();
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCreativeModeSlot(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.slotNum = param0.readShort();
        this.itemStack = param0.readItem();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
