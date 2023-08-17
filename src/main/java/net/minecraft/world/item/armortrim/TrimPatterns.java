package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = registryKey("host");

    public static void bootstrap(BootstapContext<TrimPattern> param0) {
        register(param0, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        register(param0, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        register(param0, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        register(param0, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        register(param0, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        register(param0, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        register(param0, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        register(param0, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        register(param0, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        register(param0, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        register(param0, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
        register(param0, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
        register(param0, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
        register(param0, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
        register(param0, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
        register(param0, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
    }

    public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(RegistryAccess param0, ItemStack param1) {
        return param0.registryOrThrow(Registries.TRIM_PATTERN).holders().filter(param1x -> param1.is(param1x.value().templateItem())).findFirst();
    }

    private static void register(BootstapContext<TrimPattern> param0, Item param1, ResourceKey<TrimPattern> param2) {
        TrimPattern var0 = new TrimPattern(
            param2.location(),
            BuiltInRegistries.ITEM.wrapAsHolder(param1),
            Component.translatable(Util.makeDescriptionId("trim_pattern", param2.location())),
            false
        );
        param0.register(param2, var0);
    }

    private static ResourceKey<TrimPattern> registryKey(String param0) {
        return ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(param0));
    }
}
