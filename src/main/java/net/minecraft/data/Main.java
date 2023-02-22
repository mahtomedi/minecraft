package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.packs.UpdateOneTwentyVanillaAdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.UpdateOneTwentyLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.packs.BundleRecipeProvider;
import net.minecraft.data.recipes.packs.UpdateOneTwentyRecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.UpdateOneTwentyRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyBiomeTagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyBlockTagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyItemTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Main {
    @DontObfuscate
    public static void main(String[] param0) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser var0 = new OptionParser();
        OptionSpec<Void> var1 = var0.accepts("help", "Show the help menu").forHelp();
        OptionSpec<Void> var2 = var0.accepts("server", "Include server generators");
        OptionSpec<Void> var3 = var0.accepts("client", "Include client generators");
        OptionSpec<Void> var4 = var0.accepts("dev", "Include development tools");
        OptionSpec<Void> var5 = var0.accepts("reports", "Include data reports");
        OptionSpec<Void> var6 = var0.accepts("validate", "Validate inputs");
        OptionSpec<Void> var7 = var0.accepts("all", "Include all generators");
        OptionSpec<String> var8 = var0.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated");
        OptionSpec<String> var9 = var0.accepts("input", "Input folder").withRequiredArg();
        OptionSet var10 = var0.parse(param0);
        if (!var10.has(var1) && var10.hasOptions()) {
            Path var11 = Paths.get(var8.value(var10));
            boolean var12 = var10.has(var7);
            boolean var13 = var12 || var10.has(var3);
            boolean var14 = var12 || var10.has(var2);
            boolean var15 = var12 || var10.has(var4);
            boolean var16 = var12 || var10.has(var5);
            boolean var17 = var12 || var10.has(var6);
            DataGenerator var18 = createStandardGenerator(
                var11,
                var10.valuesOf(var9).stream().map(param0x -> Paths.get(param0x)).collect(Collectors.toList()),
                var13,
                var14,
                var15,
                var16,
                var17,
                SharedConstants.getCurrentVersion(),
                true
            );
            var18.run();
        } else {
            var0.printHelpOn(System.out);
        }
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
        BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> param0, CompletableFuture<HolderLookup.Provider> param1
    ) {
        return param2 -> param0.apply(param2, param1);
    }

    public static DataGenerator createStandardGenerator(
        Path param0,
        Collection<Path> param1,
        boolean param2,
        boolean param3,
        boolean param4,
        boolean param5,
        boolean param6,
        WorldVersion param7,
        boolean param8
    ) {
        DataGenerator var0 = new DataGenerator(param0, param7, param8);
        DataGenerator.PackGenerator var1 = var0.getVanillaPack(param2 || param3);
        var1.addProvider(param1x -> new SnbtToNbt(param1x, param1).addFilter(new StructureUpdater()));
        CompletableFuture<HolderLookup.Provider> var2 = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        DataGenerator.PackGenerator var3 = var0.getVanillaPack(param2);
        var3.addProvider(ModelProvider::new);
        DataGenerator.PackGenerator var4 = var0.getVanillaPack(param3);
        var4.addProvider(bindRegistries(RegistriesDatapackGenerator::new, var2));
        var4.addProvider(bindRegistries(VanillaAdvancementProvider::create, var2));
        var4.addProvider(VanillaLootTableProvider::create);
        var4.addProvider(VanillaRecipeProvider::new);
        TagsProvider<Block> var5 = var4.addProvider(bindRegistries(VanillaBlockTagsProvider::new, var2));
        TagsProvider<Item> var6 = var4.addProvider(param2x -> new VanillaItemTagsProvider(param2x, var2, var5.contentsGetter()));
        var4.addProvider(bindRegistries(BannerPatternTagsProvider::new, var2));
        var4.addProvider(bindRegistries(BiomeTagsProvider::new, var2));
        var4.addProvider(bindRegistries(CatVariantTagsProvider::new, var2));
        var4.addProvider(bindRegistries(DamageTypeTagsProvider::new, var2));
        var4.addProvider(bindRegistries(EntityTypeTagsProvider::new, var2));
        var4.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, var2));
        var4.addProvider(bindRegistries(FluidTagsProvider::new, var2));
        var4.addProvider(bindRegistries(GameEventTagsProvider::new, var2));
        var4.addProvider(bindRegistries(InstrumentTagsProvider::new, var2));
        var4.addProvider(bindRegistries(PaintingVariantTagsProvider::new, var2));
        var4.addProvider(bindRegistries(PoiTypeTagsProvider::new, var2));
        var4.addProvider(bindRegistries(StructureTagsProvider::new, var2));
        var4.addProvider(bindRegistries(WorldPresetTagsProvider::new, var2));
        var4 = var0.getVanillaPack(param4);
        var4.addProvider(param1x -> new NbtToSnbt(param1x, param1));
        var4 = var0.getVanillaPack(param5);
        var4.addProvider(bindRegistries(BiomeParametersDumpReport::new, var2));
        var4.addProvider(BlockListReport::new);
        var4.addProvider(bindRegistries(CommandsReport::new, var2));
        var4.addProvider(RegistryDumpReport::new);
        var4 = var0.getBuiltinDatapack(param3, "bundle");
        var4.addProvider(BundleRecipeProvider::new);
        var4.addProvider(
            param0x -> PackMetadataGenerator.forFeaturePack(
                    param0x, Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE)
                )
        );
        CompletableFuture<HolderLookup.Provider> var10 = UpdateOneTwentyRegistries.createLookup(var2);
        DataGenerator.PackGenerator var11 = var0.getBuiltinDatapack(param3, "update_1_20");
        var11.addProvider(UpdateOneTwentyRecipeProvider::new);
        TagsProvider<Block> var12 = var11.addProvider(param2x -> new UpdateOneTwentyBlockTagsProvider(param2x, var10, var5.contentsGetter()));
        var11.addProvider(param3x -> new UpdateOneTwentyItemTagsProvider(param3x, var10, var6.contentsGetter(), var12.contentsGetter()));
        var11.addProvider(bindRegistries(UpdateOneTwentyBiomeTagsProvider::new, var10));
        var11.addProvider(UpdateOneTwentyLootTableProvider::create);
        var11.addProvider(bindRegistries(UpdateOneTwentyVanillaAdvancementProvider::create, var10));
        var11.addProvider(bindRegistries(RegistriesDatapackGenerator::new, var10));
        var11.addProvider(
            param0x -> PackMetadataGenerator.forFeaturePack(
                    param0x, Component.translatable("dataPack.update_1_20.description"), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20)
                )
        );
        return var0;
    }
}
