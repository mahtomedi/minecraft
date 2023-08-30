package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
    private final List<RecipeHolder<?>> recipes;

    public ClientboundUpdateRecipesPacket(Collection<RecipeHolder<?>> param0) {
        this.recipes = Lists.newArrayList(param0);
    }

    public ClientboundUpdateRecipesPacket(FriendlyByteBuf param0) {
        this.recipes = param0.readList(ClientboundUpdateRecipesPacket::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.recipes, ClientboundUpdateRecipesPacket::toNetwork);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateRecipes(this);
    }

    public List<RecipeHolder<?>> getRecipes() {
        return this.recipes;
    }

    private static RecipeHolder<?> fromNetwork(FriendlyByteBuf param0x) {
        ResourceLocation var0 = param0x.readResourceLocation();
        ResourceLocation var1 = param0x.readResourceLocation();
        Recipe<?> var2 = BuiltInRegistries.RECIPE_SERIALIZER
            .getOptional(var0)
            .orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + var0))
            .fromNetwork(param0x);
        return new RecipeHolder<>(var1, var2);
    }

    public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf param0x, RecipeHolder<?> param1) {
        param0x.writeResourceLocation(BuiltInRegistries.RECIPE_SERIALIZER.getKey(param1.value().getSerializer()));
        param0x.writeResourceLocation(param1.id());
        param1.value().getSerializer().toNetwork(param0x, param1.value());
    }
}
