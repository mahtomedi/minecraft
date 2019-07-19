package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
    private int containerId;
    private ResourceLocation recipe;

    public ClientboundPlaceGhostRecipePacket() {
    }

    public ClientboundPlaceGhostRecipePacket(int param0, Recipe<?> param1) {
        this.containerId = param0;
        this.recipe = param1.getId();
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    @OnlyIn(Dist.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.containerId = param0.readByte();
        this.recipe = param0.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.containerId);
        param0.writeResourceLocation(this.recipe);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlaceRecipe(this);
    }
}
