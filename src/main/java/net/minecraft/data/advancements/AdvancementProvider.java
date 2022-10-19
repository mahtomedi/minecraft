package net.minecraft.data.advancements;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders;

    public AdvancementProvider(String param0, PackOutput param1, List<AdvancementSubProvider> param2) {
        this.name = param0;
        this.pathProvider = param1.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
        this.subProviders = param2;
    }

    @Override
    public void run(CachedOutput param0) {
        Set<ResourceLocation> var0 = new HashSet<>();
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

        for(AdvancementSubProvider var2 : this.subProviders) {
            var2.generate(var1);
        }

    }

    @Override
    public String getName() {
        return this.name;
    }
}
