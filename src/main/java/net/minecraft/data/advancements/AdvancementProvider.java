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
    private final DataGenerator.PathProvider pathProvider;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(
        new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements()
    );

    public AdvancementProvider(DataGenerator param0) {
        this.pathProvider = param0.createPathProvider(DataGenerator.Target.DATA_PACK, "advancements");
    }

    @Override
    public void run(CachedOutput param0) {
        Set<ResourceLocation> var0 = Sets.newHashSet();
        Consumer<Advancement> var1 = param2 -> {
            if (!var0.add(param2.getId())) {
                throw new IllegalStateException("Duplicate advancement " + param2.getId());
            } else {
                Path var0x = this.pathProvider.json(param2.getId());

                try {
                    DataProvider.saveStable(param0, param2.deconstruct().serializeToJson(), var0x);
                } catch (IOException var6) {
                    LOGGER.error("Couldn't save advancement {}", var0x, var6);
                }

            }
        };

        for(Consumer<Consumer<Advancement>> var2 : this.tabs) {
            var2.accept(var1);
        }

    }

    @Override
    public String getName() {
        return "Advancements";
    }
}
