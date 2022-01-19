package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
    static <T> Codec<Tag<T>> codec(Supplier<TagCollection<T>> param0) {
        return ResourceLocation.CODEC
            .flatXmap(
                param1 -> Optional.ofNullable(param0.get().getTag(param1)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown tag: " + param1)),
                param1 -> Optional.ofNullable(param0.get().getId(param1)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown tag: " + param1))
            );
    }

    boolean contains(T var1);

    List<T> getValues();

    default Optional<T> getRandomElement(Random param0) {
        List<T> var0 = this.getValues();
        int var1 = var0.size();
        return var1 > 0 ? Optional.of(var0.get(param0.nextInt(var1))) : Optional.empty();
    }

    static <T> Tag<T> fromSet(Set<T> param0) {
        return SetTag.create(param0);
    }

    public static class Builder {
        private final List<Tag.BuilderEntry> entries = Lists.newArrayList();

        public static Tag.Builder tag() {
            return new Tag.Builder();
        }

        public Tag.Builder add(Tag.BuilderEntry param0) {
            this.entries.add(param0);
            return this;
        }

        public Tag.Builder add(Tag.Entry param0, String param1) {
            return this.add(new Tag.BuilderEntry(param0, param1));
        }

        public Tag.Builder addElement(ResourceLocation param0, String param1) {
            return this.add(new Tag.ElementEntry(param0), param1);
        }

        public Tag.Builder addOptionalElement(ResourceLocation param0, String param1) {
            return this.add(new Tag.OptionalElementEntry(param0), param1);
        }

        public Tag.Builder addTag(ResourceLocation param0, String param1) {
            return this.add(new Tag.TagEntry(param0), param1);
        }

        public Tag.Builder addOptionalTag(ResourceLocation param0, String param1) {
            return this.add(new Tag.OptionalTagEntry(param0), param1);
        }

        public <T> Either<Collection<Tag.BuilderEntry>, Tag<T>> build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1) {
            ImmutableSet.Builder<T> var0 = ImmutableSet.builder();
            List<Tag.BuilderEntry> var1 = Lists.newArrayList();

            for(Tag.BuilderEntry var2 : this.entries) {
                if (!var2.getEntry().build(param0, param1, var0::add)) {
                    var1.add(var2);
                }
            }

            return var1.isEmpty() ? Either.right(Tag.fromSet(var0.build())) : Either.left(var1);
        }

        public Stream<Tag.BuilderEntry> getEntries() {
            return this.entries.stream();
        }

        public void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
            this.entries.forEach(param1 -> param1.entry.visitRequiredDependencies(param0));
        }

        public void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
            this.entries.forEach(param1 -> param1.entry.visitOptionalDependencies(param0));
        }

        public Tag.Builder addFromJson(JsonObject param0, String param1) {
            JsonArray var0 = GsonHelper.getAsJsonArray(param0, "values");
            List<Tag.Entry> var1 = Lists.newArrayList();

            for(JsonElement var2 : var0) {
                var1.add(parseEntry(var2));
            }

            if (GsonHelper.getAsBoolean(param0, "replace", false)) {
                this.entries.clear();
            }

            var1.forEach(param1x -> this.entries.add(new Tag.BuilderEntry(param1x, param1)));
            return this;
        }

        private static Tag.Entry parseEntry(JsonElement param0) {
            String var1;
            boolean var2;
            if (param0.isJsonObject()) {
                JsonObject var0 = param0.getAsJsonObject();
                var1 = GsonHelper.getAsString(var0, "id");
                var2 = GsonHelper.getAsBoolean(var0, "required", true);
            } else {
                var1 = GsonHelper.convertToString(param0, "id");
                var2 = true;
            }

            if (var1.startsWith("#")) {
                ResourceLocation var5 = new ResourceLocation(var1.substring(1));
                return (Tag.Entry)(var2 ? new Tag.TagEntry(var5) : new Tag.OptionalTagEntry(var5));
            } else {
                ResourceLocation var6 = new ResourceLocation(var1);
                return (Tag.Entry)(var2 ? new Tag.ElementEntry(var6) : new Tag.OptionalElementEntry(var6));
            }
        }

        public JsonObject serializeToJson() {
            JsonObject var0 = new JsonObject();
            JsonArray var1 = new JsonArray();

            for(Tag.BuilderEntry var2 : this.entries) {
                var2.getEntry().serializeTo(var1);
            }

            var0.addProperty("replace", false);
            var0.add("values", var1);
            return var0;
        }
    }

    public static class BuilderEntry {
        final Tag.Entry entry;
        private final String source;

        BuilderEntry(Tag.Entry param0, String param1) {
            this.entry = param0;
            this.source = param1;
        }

        public Tag.Entry getEntry() {
            return this.entry;
        }

        public String getSource() {
            return this.source;
        }

        @Override
        public String toString() {
            return this.entry + " (from " + this.source + ")";
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
        public boolean verifyIfPresent(Predicate<ResourceLocation> param0, Predicate<ResourceLocation> param1) {
            return param0.test(this.id);
        }

        @Override
        public String toString() {
            return this.id.toString();
        }
    }

    public interface Entry {
        <T> boolean build(Function<ResourceLocation, Tag<T>> var1, Function<ResourceLocation, T> var2, Consumer<T> var3);

        void serializeTo(JsonArray var1);

        default void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
        }

        default void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
        }

        boolean verifyIfPresent(Predicate<ResourceLocation> var1, Predicate<ResourceLocation> var2);
    }

    public interface Named<T> extends Tag<T> {
        ResourceLocation getName();
    }

    public static class OptionalElementEntry implements Tag.Entry {
        private final ResourceLocation id;

        public OptionalElementEntry(ResourceLocation param0) {
            this.id = param0;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1, Consumer<T> param2) {
            T var0 = param1.apply(this.id);
            if (var0 != null) {
                param2.accept(var0);
            }

            return true;
        }

        @Override
        public void serializeTo(JsonArray param0) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("id", this.id.toString());
            var0.addProperty("required", false);
            param0.add(var0);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> param0, Predicate<ResourceLocation> param1) {
            return true;
        }

        @Override
        public String toString() {
            return this.id + "?";
        }
    }

    public static class OptionalTagEntry implements Tag.Entry {
        private final ResourceLocation id;

        public OptionalTagEntry(ResourceLocation param0) {
            this.id = param0;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> param0, Function<ResourceLocation, T> param1, Consumer<T> param2) {
            Tag<T> var0 = param0.apply(this.id);
            if (var0 != null) {
                var0.getValues().forEach(param2);
            }

            return true;
        }

        @Override
        public void serializeTo(JsonArray param0) {
            JsonObject var0 = new JsonObject();
            var0.addProperty("id", "#" + this.id);
            var0.addProperty("required", false);
            param0.add(var0);
        }

        @Override
        public String toString() {
            return "#" + this.id + "?";
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> param0) {
            param0.accept(this.id);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> param0, Predicate<ResourceLocation> param1) {
            return true;
        }
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

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> param0, Predicate<ResourceLocation> param1) {
            return param1.test(this.id);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> param0) {
            param0.accept(this.id);
        }
    }
}
