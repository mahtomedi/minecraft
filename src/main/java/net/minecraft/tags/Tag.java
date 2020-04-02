package net.minecraft.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
    boolean contains(T var1);

    List<T> getValues();

    default T getRandomElement(Random param0) {
        List<T> var0 = this.getValues();
        return var0.get(param0.nextInt(var0.size()));
    }

    static <T> Tag<T> fromSet(final Set<T> param0) {
        final ImmutableList<T> var0 = ImmutableList.copyOf(param0);
        return new Tag<T>() {
            @Override
            public boolean contains(T param0x) {
                return param0.contains(param0);
            }

            @Override
            public List<T> getValues() {
                return var0;
            }
        };
    }

    public static class Builder {
        private final Set<Tag.Entry> entries = Sets.newLinkedHashSet();

        public static Tag.Builder tag() {
            return new Tag.Builder();
        }

        public Tag.Builder add(Tag.Entry param0) {
            this.entries.add(param0);
            return this;
        }

        public Tag.Builder addElement(ResourceLocation param0) {
            return this.add(new Tag.ElementEntry(param0));
        }

        public Tag.Builder addTag(ResourceLocation param0) {
            return this.add(new Tag.TagEntry(param0));
        }

        public <T> Optional<Tag<T>> build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1) {
            ImmutableSet.Builder<T> var0 = ImmutableSet.builder();

            for(Tag.Entry var1 : this.entries) {
                if (!var1.build(param0, param1, var0::add)) {
                    return Optional.empty();
                }
            }

            return Optional.of(Tag.fromSet(var0.build()));
        }

        public Stream<Tag.Entry> getEntries() {
            return this.entries.stream();
        }

        public <T> Stream<Tag.Entry> getUnresolvedEntries(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1) {
            return this.getEntries().filter(param2 -> !param2.build(param0, param1, param0x -> {
                }));
        }

        public Tag.Builder addFromJson(JsonObject param0) {
            JsonArray var0 = GsonHelper.getAsJsonArray(param0, "values");
            List<Tag.Entry> var1 = Lists.newArrayList();

            for(JsonElement var2 : var0) {
                String var3 = GsonHelper.convertToString(var2, "value");
                if (var3.startsWith("#")) {
                    var1.add(new Tag.TagEntry(new ResourceLocation(var3.substring(1))));
                } else {
                    var1.add(new Tag.ElementEntry(new ResourceLocation(var3)));
                }
            }

            if (GsonHelper.getAsBoolean(param0, "replace", false)) {
                this.entries.clear();
            }

            this.entries.addAll(var1);
            return this;
        }

        public JsonObject serializeToJson() {
            JsonObject var0 = new JsonObject();
            JsonArray var1 = new JsonArray();

            for(Tag.Entry var2 : this.entries) {
                var2.serializeTo(var1);
            }

            var0.addProperty("replace", false);
            var0.add("values", var1);
            return var0;
        }
    }

    public static class ElementEntry implements Tag.Entry {
        private final ResourceLocation id;

        public ElementEntry(ResourceLocation param0) {
            this.id = param0;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1, Consumer<T> param2) {
            T var0 = param1.apply(this.id);
            if (var0 == null) {
                return false;
            } else {
                param2.accept(var0);
                return true;
            }
        }

        @Override
        public void serializeTo(JsonArray param0) {
            param0.add(this.id.toString());
        }

        @Override
        public String toString() {
            return this.id.toString();
        }
    }

    public interface Entry {
        <T> boolean build(Function<ResourceLocation, Tag<T>> var1, Function<ResourceLocation, T> var2, Consumer<T> var3);

        void serializeTo(JsonArray var1);
    }

    public interface Named<T> extends Tag<T> {
        ResourceLocation getName();
    }

    public static class TagEntry implements Tag.Entry {
        private final ResourceLocation id;

        public TagEntry(ResourceLocation param0) {
            this.id = param0;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1, Consumer<T> param2) {
            Tag<T> var0 = param0.apply(this.id);
            if (var0 == null) {
                return false;
            } else {
                var0.getValues().forEach(param2);
                return true;
            }
        }

        @Override
        public void serializeTo(JsonArray param0) {
            param0.add("#" + this.id);
        }

        @Override
        public String toString() {
            return "#" + this.id;
        }
    }

    public static class TypedBuilder<T> extends Tag.Builder {
        private final Function<T, ResourceLocation> elementLookup;

        public TypedBuilder(Function<T, ResourceLocation> param0) {
            this.elementLookup = param0;
        }

        public Tag.TypedBuilder<T> add(T param0) {
            this.addElement(this.elementLookup.apply(param0));
            return this;
        }

        public Tag.TypedBuilder<T> add(Collection<T> param0) {
            param0.stream().map(this.elementLookup).forEach(this::addElement);
            return this;
        }

        @SafeVarargs
        public final Tag.TypedBuilder<T> add(T... param0) {
            this.add(Arrays.asList(param0));
            return this;
        }

        public Tag.TypedBuilder<T> addTag(Tag.Named<T> param0) {
            this.addTag(param0.getName());
            return this;
        }
    }
}
