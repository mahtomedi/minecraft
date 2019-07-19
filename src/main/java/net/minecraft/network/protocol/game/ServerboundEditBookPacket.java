package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
    private ItemStack book;
    private boolean signing;
    private InteractionHand hand;

    public ServerboundEditBookPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundEditBookPacket(ItemStack param0, boolean param1, InteractionHand param2) {
        this.book = param0.copy();
        this.signing = param1;
        this.hand = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.book = param0.readItem();
        this.signing = param0.readBoolean();
        this.hand = param0.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeItem(this.book);
        param0.writeBoolean(this.signing);
        param0.writeEnum(this.hand);
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

    public InteractionHand getHand() {
        return this.hand;
    }
}
