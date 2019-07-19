package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
    private ClientboundRecipePacket.State state;
    private List<ResourceLocation> recipes;
    private List<ResourceLocation> toHighlight;
    private boolean guiOpen;
    private boolean filteringCraftable;
    private boolean furnaceGuiOpen;
    private boolean furnaceFilteringCraftable;

    public ClientboundRecipePacket() {
    }

    public ClientboundRecipePacket(
        ClientboundRecipePacket.State param0,
        Collection<ResourceLocation> param1,
        Collection<ResourceLocation> param2,
        boolean param3,
        boolean param4,
        boolean param5,
        boolean param6
    ) {
        this.state = param0;
        this.recipes = ImmutableList.copyOf(param1);
        this.toHighlight = ImmutableList.copyOf(param2);
        this.guiOpen = param3;
        this.filteringCraftable = param4;
        this.furnaceGuiOpen = param5;
        this.furnaceFilteringCraftable = param6;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddOrRemoveRecipes(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.state = param0.readEnum(ClientboundRecipePacket.State.class);
        this.guiOpen = param0.readBoolean();
        this.filteringCraftable = param0.readBoolean();
        this.furnaceGuiOpen = param0.readBoolean();
        this.furnaceFilteringCraftable = param0.readBoolean();
        int var0 = param0.readVarInt();
        this.recipes = Lists.newArrayList();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.recipes.add(param0.readResourceLocation());
        }

        if (this.state == ClientboundRecipePacket.State.INIT) {
            var0 = param0.readVarInt();
            this.toHighlight = Lists.newArrayList();

            for(int var2 = 0; var2 < var0; ++var2) {
                this.toHighlight.add(param0.readResourceLocation());
            }
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.state);
        param0.writeBoolean(this.guiOpen);
        param0.writeBoolean(this.filteringCraftable);
        param0.writeBoolean(this.furnaceGuiOpen);
        param0.writeBoolean(this.furnaceFilteringCraftable);
        param0.writeVarInt(this.recipes.size());

        for(ResourceLocation var0 : this.recipes) {
            param0.writeResourceLocation(var0);
        }

        if (this.state == ClientboundRecipePacket.State.INIT) {
            param0.writeVarInt(this.toHighlight.size());

            for(ResourceLocation var1 : this.toHighlight) {
                param0.writeResourceLocation(var1);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public List<ResourceLocation> getRecipes() {
        return this.recipes;
    }

    @OnlyIn(Dist.CLIENT)
    public List<ResourceLocation> getHighlights() {
        return this.toHighlight;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isGuiOpen() {
        return this.guiOpen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFilteringCraftable() {
        return this.filteringCraftable;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFurnaceGuiOpen() {
        return this.furnaceGuiOpen;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFurnaceFilteringCraftable() {
        return this.furnaceFilteringCraftable;
    }

    @OnlyIn(Dist.CLIENT)
    public ClientboundRecipePacket.State getState() {
        return this.state;
    }

    public static enum State {
        INIT,
        ADD,
        REMOVE;
    }
}
