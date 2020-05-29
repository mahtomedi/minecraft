package net.minecraft.stats;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRecipeBook extends RecipeBook {
    private static final Logger LOGGER = LogManager.getLogger();

    public int addRecipes(Collection<Recipe<?>> param0, ServerPlayer param1) {
        List<ResourceLocation> var0 = Lists.newArrayList();
        int var1 = 0;

        for(Recipe<?> var2 : param0) {
            ResourceLocation var3 = var2.getId();
            if (!this.known.contains(var3) && !var2.isSpecial()) {
                this.add(var3);
                this.addHighlight(var3);
                var0.add(var3);
                CriteriaTriggers.RECIPE_UNLOCKED.trigger(param1, var2);
                ++var1;
            }
        }

        this.sendRecipes(ClientboundRecipePacket.State.ADD, param1, var0);
        return var1;
    }

    public int removeRecipes(Collection<Recipe<?>> param0, ServerPlayer param1) {
        List<ResourceLocation> var0 = Lists.newArrayList();
        int var1 = 0;

        for(Recipe<?> var2 : param0) {
            ResourceLocation var3 = var2.getId();
            if (this.known.contains(var3)) {
                this.remove(var3);
                var0.add(var3);
                ++var1;
            }
        }

        this.sendRecipes(ClientboundRecipePacket.State.REMOVE, param1, var0);
        return var1;
    }

    private void sendRecipes(ClientboundRecipePacket.State param0, ServerPlayer param1, List<ResourceLocation> param2) {
        param1.connection
            .send(
                new ClientboundRecipePacket(
                    param0, param2, Collections.emptyList(), this.guiOpen, this.filteringCraftable, this.furnaceGuiOpen, this.furnaceFilteringCraftable
                )
            );
    }

    public CompoundTag toNbt() {
        CompoundTag var0 = new CompoundTag();
        var0.putBoolean("isGuiOpen", this.guiOpen);
        var0.putBoolean("isFilteringCraftable", this.filteringCraftable);
        var0.putBoolean("isFurnaceGuiOpen", this.furnaceGuiOpen);
        var0.putBoolean("isFurnaceFilteringCraftable", this.furnaceFilteringCraftable);
        var0.putBoolean("isBlastingFurnaceGuiOpen", this.blastingFurnaceGuiOpen);
        var0.putBoolean("isBlastingFurnaceFilteringCraftable", this.blastingFurnaceFilteringCraftable);
        var0.putBoolean("isSmokerGuiOpen", this.smokerGuiOpen);
        var0.putBoolean("isSmokerFilteringCraftable", this.smokerFilteringCraftable);
        ListTag var1 = new ListTag();

        for(ResourceLocation var2 : this.known) {
            var1.add(StringTag.valueOf(var2.toString()));
        }

        var0.put("recipes", var1);
        ListTag var3 = new ListTag();

        for(ResourceLocation var4 : this.highlight) {
            var3.add(StringTag.valueOf(var4.toString()));
        }

        var0.put("toBeDisplayed", var3);
        return var0;
    }

    public void fromNbt(CompoundTag param0, RecipeManager param1) {
        this.guiOpen = param0.getBoolean("isGuiOpen");
        this.filteringCraftable = param0.getBoolean("isFilteringCraftable");
        this.furnaceGuiOpen = param0.getBoolean("isFurnaceGuiOpen");
        this.furnaceFilteringCraftable = param0.getBoolean("isFurnaceFilteringCraftable");
        this.blastingFurnaceGuiOpen = param0.getBoolean("isBlastingFurnaceGuiOpen");
        this.blastingFurnaceFilteringCraftable = param0.getBoolean("isBlastingFurnaceFilteringCraftable");
        this.smokerGuiOpen = param0.getBoolean("isSmokerGuiOpen");
        this.smokerFilteringCraftable = param0.getBoolean("isSmokerFilteringCraftable");
        ListTag var0 = param0.getList("recipes", 8);
        this.loadRecipes(var0, this::add, param1);
        ListTag var1 = param0.getList("toBeDisplayed", 8);
        this.loadRecipes(var1, this::addHighlight, param1);
    }

    private void loadRecipes(ListTag param0, Consumer<Recipe<?>> param1, RecipeManager param2) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            String var1 = param0.getString(var0);

            try {
                ResourceLocation var2 = new ResourceLocation(var1);
                Optional<? extends Recipe<?>> var3 = param2.byKey(var2);
                if (!var3.isPresent()) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", var2);
                } else {
                    param1.accept(var3.get());
                }
            } catch (ResourceLocationException var8) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", var1);
            }
        }

    }

    public void sendInitialRecipeBook(ServerPlayer param0) {
        param0.connection
            .send(
                new ClientboundRecipePacket(
                    ClientboundRecipePacket.State.INIT,
                    this.known,
                    this.highlight,
                    this.guiOpen,
                    this.filteringCraftable,
                    this.furnaceGuiOpen,
                    this.furnaceFilteringCraftable
                )
            );
    }
}
