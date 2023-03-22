package net.minecraft.world.item.armortrim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");

    public static void bootstrap(BootstapContext<TrimMaterial> param0) {
        register(param0, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.1F);
        register(param0, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), 0.2F, Map.of(ArmorMaterials.IRON, "iron_darker"));
        register(param0, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.3F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"));
        register(param0, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.4F);
        register(param0, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.5F);
        register(param0, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.6F, Map.of(ArmorMaterials.GOLD, "gold_darker"));
        register(param0, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.7F);
        register(param0, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.8F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"));
        register(param0, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.9F);
        register(param0, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 1.0F);
    }

    public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(RegistryAccess param0, ItemStack param1) {
        return param0.registryOrThrow(Registries.TRIM_MATERIAL).holders().filter(param1x -> param1.is(param1x.value().ingredient())).findFirst();
    }

    private static void register(BootstapContext<TrimMaterial> param0, ResourceKey<TrimMaterial> param1, Item param2, Style param3, float param4) {
        register(param0, param1, param2, param3, param4, Map.of());
    }

    private static void register(
        BootstapContext<TrimMaterial> param0, ResourceKey<TrimMaterial> param1, Item param2, Style param3, float param4, Map<ArmorMaterials, String> param5
    ) {
        TrimMaterial var0 = TrimMaterial.create(
            param1.location().getPath(),
            param2,
            param4,
            Component.translatable(Util.makeDescriptionId("trim_material", param1.location())).withStyle(param3),
            param5
        );
        param0.register(param1, var0);
    }

    private static ResourceKey<TrimMaterial> registryKey(String param0) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, new ResourceLocation(param0));
    }
}
