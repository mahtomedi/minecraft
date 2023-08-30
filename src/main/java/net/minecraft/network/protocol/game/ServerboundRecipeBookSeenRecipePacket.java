package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
    private final ResourceLocation recipe;

    public ServerboundRecipeBookSeenRecipePacket(RecipeHolder<?> param0) {
        this.recipe = param0.id();
    }

    public ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf param0) {
        this.recipe = param0.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.recipe);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleRecipeBookSeenRecipePacket(this);
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }
}
