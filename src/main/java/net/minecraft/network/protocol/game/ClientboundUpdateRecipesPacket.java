package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
    private List<Recipe<?>> recipes;

    public ClientboundUpdateRecipesPacket() {
    }

    public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> param0) {
        this.recipes = Lists.newArrayList(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateRecipes(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.recipes = Lists.newArrayList();
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            this.recipes.add(fromNetwork(param0));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.recipes.size());

        for(Recipe<?> var0 : this.recipes) {
            toNetwork(var0, param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public static Recipe<?> fromNetwork(FriendlyByteBuf param0) {
        ResourceLocation var0 = param0.readResourceLocation();
        ResourceLocation var1 = param0.readResourceLocation();
        return Registry.RECIPE_SERIALIZER
            .getOptional(var0)
            .orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + var0))
            .fromNetwork(var1, param0);
    }

    public static <T extends Recipe<?>> void toNetwork(T param0, FriendlyByteBuf param1) {
        param1.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(param0.getSerializer()));
        param1.writeResourceLocation(param0.getId());
        param0.getSerializer().toNetwork(param1, param0);
    }
}
