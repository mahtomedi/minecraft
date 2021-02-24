package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
    private final ItemStack book;
    private final boolean signing;
    private final int slot;

    @OnlyIn(Dist.CLIENT)
    public ServerboundEditBookPacket(ItemStack param0, boolean param1, int param2) {
        this.book = param0.copy();
        this.signing = param1;
        this.slot = param2;
    }

    public ServerboundEditBookPacket(FriendlyByteBuf param0) {
        this.book = param0.readItem();
        this.signing = param0.readBoolean();
        this.slot = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeItem(this.book);
        param0.writeBoolean(this.signing);
        param0.writeVarInt(this.slot);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleEditBook(this);
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean isSigning() {
        return this.signing;
    }

    public int getSlot() {
        return this.slot;
    }
}
