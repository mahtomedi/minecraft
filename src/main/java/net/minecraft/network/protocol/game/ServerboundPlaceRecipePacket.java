package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPlaceRecipePacket implements Packet<ServerGamePacketListener> {
    private int containerId;
    private ResourceLocation recipe;
    private boolean shiftDown;

    public ServerboundPlaceRecipePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlaceRecipePacket(int param0, Recipe<?> param1, boolean param2) {
        this.containerId = param0;
        this.recipe = param1.getId();
        this.shiftDown = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readByte();
        this.recipe = param0.readResourceLocation();
        this.shiftDown = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
