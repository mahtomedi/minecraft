package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public KnowledgeBookItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        CompoundTag var1 = var0.getTag();
        if (!param1.getAbilities().instabuild) {
            param1.setItemInHand(param2, ItemStack.EMPTY);
        }

        if (var1 != null && var1.contains("Recipes", 9)) {
            if (!param0.isClientSide) {
                ListTag var2 = var1.getList("Recipes", 8);
                List<Recipe<?>> var3 = Lists.newArrayList();
                RecipeManager var4 = param0.getServer().getRecipeManager();

                for(int var5 = 0; var5 < var2.size(); ++var5) {
                    String var6 = var2.getString(var5);
                    Optional<? extends Recipe<?>> var7 = var4.byKey(new ResourceLocation(var6));
                    if (!var7.isPresent()) {
                        LOGGER.error("Invalid recipe: {}", var6);
                        return InteractionResultHolder.fail(var0);
                    }

                    var3.add(var7.get());
                }

                param1.awardRecipes(var3);
                param1.awardStat(Stats.ITEM_USED.get(this));
            }

            return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
        } else {
            LOGGER.error("Tag not valid: {}", var1);
            return InteractionResultHolder.fail(var0);
        }
    }
}
