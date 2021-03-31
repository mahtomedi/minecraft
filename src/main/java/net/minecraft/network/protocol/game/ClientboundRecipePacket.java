package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
    private final ClientboundRecipePacket.State state;
    private final List<ResourceLocation> recipes;
    private final List<ResourceLocation> toHighlight;
    private final RecipeBookSettings bookSettings;

    public ClientboundRecipePacket(
        ClientboundRecipePacket.State param0, Collection<ResourceLocation> param1, Collection<ResourceLocation> param2, RecipeBookSettings param3
    ) {
        this.state = param0;
        this.recipes = ImmutableList.copyOf(param1);
        this.toHighlight = ImmutableList.copyOf(param2);
        this.bookSettings = param3;
    }

    public ClientboundRecipePacket(FriendlyByteBuf param0) {
        this.state = param0.readEnum(ClientboundRecipePacket.State.class);
        this.bookSettings = RecipeBookSettings.read(param0);
        this.recipes = param0.readList(FriendlyByteBuf::readResourceLocation);
        if (this.state == ClientboundRecipePacket.State.INIT) {
            this.toHighlight = param0.readList(FriendlyByteBuf::readResourceLocation);
        } else {
            this.toHighlight = ImmutableList.of();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.state);
        this.bookSettings.write(param0);
        param0.writeCollection(this.recipes, FriendlyByteBuf::writeResourceLocation);
        if (this.state == ClientboundRecipePacket.State.INIT) {
            param0.writeCollection(this.toHighlight, FriendlyByteBuf::writeResourceLocation);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddOrRemoveRecipes(this);
    }

    public List<ResourceLocation> getRecipes() {
        return this.recipes;
    }

    public List<ResourceLocation> getHighlights() {
        return this.toHighlight;
    }

    public RecipeBookSettings getBookSettings() {
        return this.bookSettings;
    }

    public ClientboundRecipePacket.State getState() {
        return this.state;
    }

    public static enum State {
        INIT,
        ADD,
        REMOVE;
    }
}
