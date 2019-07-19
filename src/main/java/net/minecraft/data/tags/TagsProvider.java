package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    protected final Map<Tag<T>, Tag.Builder<T>> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator param0, Registry<T> param1) {
        this.generator = param0;
        this.registry = param1;
    }

    protected abstract void addTags();

    @Override
    public void run(HashCache param0) {
        this.builders.clear();
        this.addTags();
        TagCollection<T> var0 = new TagCollection<>(param0x -> Optional.empty(), "", false, "generated");
        Map<ResourceLocation, Tag.Builder<T>> var1 = this.builders
            .entrySet()
            .stream()
            .collect(Collectors.toMap(param0x -> param0x.getKey().getId(), Entry::getValue));
        var0.load(var1);
        var0.getAllTags().forEach((param1, param2) -> {
            JsonObject var0x = param2.serializeToJson(this.registry::getKey);
            Path var1x = this.getPath(param1);

            try {
                String var2 = GSON.toJson((JsonElement)var0x);
                String var3x = SHA1.hashUnencodedChars(var2).toString();
                if (!Objects.equals(param0.getHash(var1x), var3x) || !Files.exists(var1x)) {
                    Files.createDirectories(var1x.getParent());

                    try (BufferedWriter var4 = Files.newBufferedWriter(var1x)) {
                        var4.write(var2);
                    }
                }

                param0.putNew(var1x, var3x);
            } catch (IOException var21) {
                LOGGER.error("Couldn't save tags to {}", var1x, var21);
            }

        });
        this.useTags(var0);
    }

    protected abstract void useTags(TagCollection<T> var1);

    protected abstract Path getPath(ResourceLocation var1);

    protected Tag.Builder<T> tag(Tag<T> param0) {
        return this.builders.computeIfAbsent(param0, param0x -> Tag.Builder.tag());
    }
}
