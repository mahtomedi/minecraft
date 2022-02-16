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
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.info.WorldgenRegistryDumpReport;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.obfuscate.DontObfuscate;

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
                var11, var10.valuesOf(var9).stream().map(param0x -> Paths.get(param0x)).collect(Collectors.toList()), var13, var14, var15, var16, var17
            );
            var18.run();
        } else {
            var0.printHelpOn(System.out);
        }
    }

    public static DataGenerator createStandardGenerator(
        Path param0, Collection<Path> param1, boolean param2, boolean param3, boolean param4, boolean param5, boolean param6
    ) {
        DataGenerator var0 = new DataGenerator(param0, param1);
        if (param2 || param3) {
            var0.addProvider(new SnbtToNbt(var0).addFilter(new StructureUpdater()));
        }

        if (param2) {
            var0.addProvider(new ModelProvider(var0));
        }

        if (param3) {
            var0.addProvider(new FluidTagsProvider(var0));
            BlockTagsProvider var1 = new BlockTagsProvider(var0);
            var0.addProvider(var1);
            var0.addProvider(new ItemTagsProvider(var0, var1));
            var0.addProvider(new EntityTypeTagsProvider(var0));
            var0.addProvider(new RecipeProvider(var0));
            var0.addProvider(new AdvancementProvider(var0));
            var0.addProvider(new LootTableProvider(var0));
            var0.addProvider(new GameEventTagsProvider(var0));
            var0.addProvider(new BiomeTagsProvider(var0));
        }

        if (param4) {
            var0.addProvider(new NbtToSnbt(var0));
        }

        if (param5) {
            var0.addProvider(new BlockListReport(var0));
            var0.addProvider(new RegistryDumpReport(var0));
            var0.addProvider(new CommandsReport(var0));
            var0.addProvider(new WorldgenRegistryDumpReport(var0));
        }

        return var0;
    }
}
