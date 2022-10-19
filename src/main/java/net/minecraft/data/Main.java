package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
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
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyBlockTagsProvider;
import net.minecraft.data.tags.UpdateOneTwentyItemTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.data.worldgen.BuiltinRegistriesDatapackGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
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
        PackOutput var1 = var0.getVanillaPackOutput();
        var0.addProvider(param2 || param3, new SnbtToNbt(var1, param1).addFilter(new StructureUpdater()));
        var0.addProvider(param2, new ModelProvider(var1));
        var0.addProvider(param3, new BuiltinRegistriesDatapackGenerator(var1));
        var0.addProvider(param3, VanillaAdvancementProvider.create(var1));
        var0.addProvider(param3, VanillaLootTableProvider.create(var1));
        var0.addProvider(param3, new VanillaRecipeProvider(var1));
        TagsProvider<Block> var2 = new VanillaBlockTagsProvider(var1);
        var0.addProvider(param3, var2);
        var0.addProvider(param3, new VanillaItemTagsProvider(var1, var2));
        var0.addProvider(param3, new BannerPatternTagsProvider(var1));
        var0.addProvider(param3, new BiomeTagsProvider(var1));
        var0.addProvider(param3, new CatVariantTagsProvider(var1));
        var0.addProvider(param3, new EntityTypeTagsProvider(var1));
        var0.addProvider(param3, new FlatLevelGeneratorPresetTagsProvider(var1));
        var0.addProvider(param3, new FluidTagsProvider(var1));
        var0.addProvider(param3, new GameEventTagsProvider(var1));
        var0.addProvider(param3, new InstrumentTagsProvider(var1));
        var0.addProvider(param3, new PaintingVariantTagsProvider(var1));
        var0.addProvider(param3, new PoiTypeTagsProvider(var1));
        var0.addProvider(param3, new StructureTagsProvider(var1));
        var0.addProvider(param3, new WorldPresetTagsProvider(var1));
        var0.addProvider(param4, new NbtToSnbt(var1, param1));
        var0.addProvider(param5, new BiomeParametersDumpReport(var1));
        var0.addProvider(param5, new BlockListReport(var1));
        var0.addProvider(param5, new CommandsReport(var1));
        var0.addProvider(param5, new RegistryDumpReport(var1));
        PackOutput var3 = var0.createBuiltinDatapackOutput("bundle");
        var0.addProvider(param3, new BundleRecipeProvider(var3));
        var0.addProvider(
            param3,
            PackMetadataGenerator.forFeaturePack(var3, "bundle", Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE))
        );
        PackOutput var4 = var0.createBuiltinDatapackOutput("update_1_20");
        var0.addProvider(param3, new UpdateOneTwentyRecipeProvider(var4));
        UpdateOneTwentyBlockTagsProvider var5 = new UpdateOneTwentyBlockTagsProvider(var4);
        var0.addProvider(param3, var5);
        var0.addProvider(param3, new UpdateOneTwentyItemTagsProvider(var4, var5));
        var0.addProvider(param3, UpdateOneTwentyLootTableProvider.create(var4));
        var0.addProvider(
            param3,
            PackMetadataGenerator.forFeaturePack(
                var4, "update_1_20", Component.translatable("dataPack.update_1_20.description"), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20)
            )
        );
        return var0;
    }
}
