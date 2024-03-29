package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final ResourceLocation recipe;

    public ClientboundPlaceGhostRecipePacket(int param0, RecipeHolder<?> param1) {
        this.containerId = param0;
        this.recipe = param1.id();
    }

    public ClientboundPlaceGhostRecipePacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.recipe = param0.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeResourceLocation(this.recipe);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlaceRecipe(this);
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    public int getContainerId() {
        return this.containerId;
    }
}
