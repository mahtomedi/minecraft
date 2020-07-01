package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StaticTagHelper<T> {
    private TagCollection<T> source = TagCollection.empty();
    private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.newArrayList();
    private final Function<TagContainer, TagCollection<T>> collectionGetter;

    public StaticTagHelper(Function<TagContainer, TagCollection<T>> param0) {
        this.collectionGetter = param0;
    }

    public Tag.Named<T> bind(String param0) {
        StaticTagHelper.Wrapper<T> var0 = new StaticTagHelper.Wrapper<>(new ResourceLocation(param0));
        this.wrappers.add(var0);
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public void resetToEmpty() {
        this.source = TagCollection.empty();
        Tag<T> var0 = SetTag.empty();
        this.wrappers.forEach(param1 -> param1.rebind(param1x -> var0));
    }

    public void reset(TagContainer param0) {
        TagCollection<T> var0 = this.collectionGetter.apply(param0);
        this.source = var0;
        this.wrappers.forEach(param1 -> param1.rebind(var0::getTag));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
    }

    public List<? extends Tag<T>> getWrappers() {
        return this.wrappers;
    }

    public Set<ResourceLocation> getMissingTags(TagContainer param0) {
        TagCollection<T> var0 = this.collectionGetter.apply(param0);
        Set<ResourceLocation> var1 = this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
        ImmutableSet<ResourceLocation> var2 = ImmutableSet.copyOf(var0.getAvailableTags());
        return Sets.difference(var1, var2);
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
