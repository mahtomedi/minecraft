package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final ResourceLocation recipe;
    private final boolean shiftDown;

    public ServerboundPlaceRecipePacket(int param0, Recipe<?> param1, boolean param2) {
        this.containerId = param0;
        this.recipe = param1.getId();
        this.shiftDown = param2;
    }

    public ServerboundPlaceRecipePacket(FriendlyByteBuf param0) {
        this.containerId = param0.readByte();
        this.recipe = param0.readResourceLocation();
        this.shiftDown = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByte(this.containerId);
        param0.writeResourceLocation(this.recipe);
        param0.writeBoolean(this.shiftDown);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlaceRecipe(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    public boolean isShiftDown() {
        return this.shiftDown;
    }
}
