package net.minecraft.tags;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
    private TagCollection<T> source = new TagCollection<>(param0 -> Optional.empty(), "", "");
    private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.newArrayList();

    public Tag.Named<T> bind(String param0) {
        StaticTagHelper.Wrapper<T> var0 = new StaticTagHelper.Wrapper<>(new ResourceLocation(param0));
        this.wrappers.add(var0);
        return var0;
    }

    public void reset(TagCollection<T> param0) {
        this.source = param0;
        this.wrappers.forEach(param1 -> param1.rebind(param0));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
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

        void rebind(TagCollection<T> param0) {
            this.tag = param0.getTag(this.name);
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
