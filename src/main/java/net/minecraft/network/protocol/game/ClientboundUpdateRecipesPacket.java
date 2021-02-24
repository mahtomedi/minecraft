package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
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
    private final List<Recipe<?>> recipes;

    public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> param0) {
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

    @OnlyIn(Dist.CLIENT)
    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public static Recipe<?> fromNetwork(FriendlyByteBuf param0x) {
        ResourceLocation var0 = param0x.readResourceLocation();
        ResourceLocation var1 = param0x.readResourceLocation();
        return Registry.RECIPE_SERIALIZER
            .getOptional(var0)
            .orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + var0))
            .fromNetwork(var1, param0x);
    }

    public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf param0x, T param1) {
        param0x.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(param1.getSerializer()));
        param0x.writeResourceLocation(param1.getId());
        param1.getSerializer().toNetwork(param0x, param1);
    }
}
