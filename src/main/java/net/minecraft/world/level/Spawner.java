package net.minecraft.world.level;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public interface Spawner {
    void setEntityId(EntityType<?> var1, RandomSource var2);

    static void appendHoverText(ItemStack param0, List<Component> param1, String param2) {
        Component var0 = getSpawnEntityDisplayName(param0, param2);
        if (var0 != null) {
            param1.add(var0);
        } else {
            param1.add(CommonComponents.EMPTY);
            param1.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
            param1.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
        }

    }

    @Nullable
    static Component getSpawnEntityDisplayName(ItemStack param0, String param1) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        if (var0 != null) {
            ResourceLocation var1 = getEntityKey(var0, param1);
            if (var1 != null) {
                return BuiltInRegistries.ENTITY_TYPE
                    .getOptional(var1)
                    .map(param0x -> Component.translatable(param0x.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                    .orElse(null);
            }
        }

        return null;
    }

    @Nullable
    private static ResourceLocation getEntityKey(CompoundTag param0, String param1) {
        if (param0.contains(param1, 10)) {
            String var0 = param0.getCompound(param1).getCompound("entity").getString("id");
            return ResourceLocation.tryParse(var0);
        } else {
            return null;
        }
    }
}
