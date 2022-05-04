package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(
        new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements()
    );

    public AdvancementProvider(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) {
        Path var0 = this.generator.getOutputFolder();
        Set<ResourceLocation> var1 = Sets.newHashSet();
        Consumer<Advancement> var2 = param3 -> {
            if (!var1.add(param3.getId())) {
                throw new IllegalStateException("Duplicate advancement " + param3.getId());
            } else {
                Path var0x = createPath(var0, param3);

                try {
                    DataProvider.saveStable(param0, param3.deconstruct().serializeToJson(), var0x);
                } catch (IOException var6x) {
                    LOGGER.error("Couldn't save advancement {}", var0x, var6x);
                }

            }
        };

        for(Consumer<Consumer<Advancement>> var3 : this.tabs) {
            var3.accept(var2);
        }

    }

    private static Path createPath(Path param0, Advancement param1) {
        return param0.resolve("data/" + param1.getId().getNamespace() + "/advancements/" + param1.getId().getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}
