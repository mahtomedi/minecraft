package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final String directory;
    private TagCollection<T> source = TagCollection.empty();
    private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.newArrayList();

    public StaticTagHelper(ResourceKey<? extends Registry<T>> param0, String param1) {
        this.key = param0;
        this.directory = param1;
    }

    public Tag.Named<T> bind(String param0) {
        StaticTagHelper.Wrapper<T> var0 = new StaticTagHelper.Wrapper<>(new ResourceLocation(param0));
        this.wrappers.add(var0);
        return var0;
    }

    public void resetToEmpty() {
        this.source = TagCollection.empty();
        Tag<T> var0 = SetTag.empty();
        this.wrappers.forEach(param1 -> param1.rebind(param1x -> var0));
    }

    public void reset(TagContainer param0) {
        TagCollection<T> var0 = param0.getOrEmpty(this.key);
        this.source = var0;
        this.wrappers.forEach(param1 -> param1.rebind(var0::getTag));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
    }

    public Set<ResourceLocation> getMissingTags(TagContainer param0) {
        TagCollection<T> var0 = param0.getOrEmpty(this.key);
        Set<ResourceLocation> var1 = this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
        ImmutableSet<ResourceLocation> var2 = ImmutableSet.copyOf(var0.getAvailableTags());
        return Sets.difference(var1, var2);
    }

    public ResourceKey<? extends Registry<T>> getKey() {
        return this.key;
    }

    public String getDirectory() {
        return this.directory;
    }

    protected void addToCollection(TagContainer.Builder param0) {
        param0.add(
            this.key,
            TagCollection.of(
                this.wrappers
                    .stream()
                    .collect(
                        Collectors.toMap(
                            Tag.Named::getName, (Function<? super StaticTagHelper.Wrapper<T>, ? extends StaticTagHelper.Wrapper<T>>)(param0x -> param0x)
                        )
                    )
            )
        );
    }

    static class Wrapper<T> implements Tag.Named<T> {
        @Nullable
        private Tag<T> tag;
        protected final ResourceLocation name;

        private Wrapper(ResourceLocation param0) {
            this.name = param0;
        }

        @Override
        public ResourceLocation getName() {
            return this.name;
        }

        private Tag<T> resolve() {
            if (this.tag == null) {
                throw new IllegalStateException("Tag " + this.name + " used before it was bound");
            } else {
                return this.tag;
            }
        }

        void rebind(Function<ResourceLocation, Tag<T>> param0) {
            this.tag = param0.apply(this.name);
        }

        @Override
        public boolean contains(T param0) {
            return this.resolve().contains(param0);
        }

        @Override
        public List<T> getValues() {
            return this.resolve().getValues();
        }
    }
}
