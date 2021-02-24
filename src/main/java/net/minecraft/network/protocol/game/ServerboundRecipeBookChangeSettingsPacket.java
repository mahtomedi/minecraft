package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener> {
    private final RecipeBookType bookType;
    private final boolean isOpen;
    private final boolean isFiltering;

    @OnlyIn(Dist.CLIENT)
    public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType param0, boolean param1, boolean param2) {
        this.bookType = param0;
        this.isOpen = param1;
        this.isFiltering = param2;
    }

    public ServerboundRecipeBookChangeSettingsPacket(FriendlyByteBuf param0) {
        this.bookType = param0.readEnum(RecipeBookType.class);
        this.isOpen = param0.readBoolean();
        this.isFiltering = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.bookType);
        param0.writeBoolean(this.isOpen);
        param0.writeBoolean(this.isFiltering);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRecipeBookChangeSettingsPacket(this);
    }

    public RecipeBookType getBookType() {
        return this.bookType;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public boolean isFiltering() {
        return this.isFiltering;
    }
}
