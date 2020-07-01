package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
    private ResourceLocation recipe;

    public ServerboundRecipeBookSeenRecipePacket() {
    }

    public ServerboundRecipeBookSeenRecipePacket(Recipe<?> param0) {
        this.recipe = param0.getId();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.recipe = param0.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeResourceLocation(this.recipe);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRecipeBookSeenRecipePacket(this);
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }
}
