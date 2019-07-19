package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Tag<T> {
    private final ResourceLocation id;
    private final Set<T> values;
    private final Collection<Tag.Entry<T>> source;

    public Tag(ResourceLocation param0) {
        this.id = param0;
        this.values = Collections.emptySet();
        this.source = Collections.emptyList();
    }

    public Tag(ResourceLocation param0, Collection<Tag.Entry<T>> param1, boolean param2) {
        this.id = param0;
        this.values = (Set<T>)(param2 ? Sets.newLinkedHashSet() : Sets.newHashSet());
        this.source = param1;

        for(Tag.Entry<T> var0 : param1) {
            var0.build(this.values);
        }

    }

    public JsonObject serializeToJson(Function<T, ResourceLocation> param0) {
        JsonObject var0 = new JsonObject();
        JsonArray var1 = new JsonArray();

        for(Tag.Entry<T> var2 : this.source) {
            var2.serializeTo(var1, param0);
        }

        var0.addProperty("replace", false);
        var0.add("values", var1);
        return var0;
    }

    public boolean contains(T param0) {
        return this.values.contains(param0);
    }

    public Collection<T> getValues() {
        return this.values;
    }

    public Collection<Tag.Entry<T>> getSource() {
        return this.source;
    }

    public T getRandomElement(Random param0) {
        List<T> var0 = Lists.newArrayList(this.getValues());
        return var0.get(param0.nextInt(var0.size()));
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public static class Builder<T> {
        private final Set<Tag.Entry<T>> values = Sets.newLinkedHashSet();
        private boolean ordered;

        public static <T> Tag.Builder<T> tag() {
            return new Tag.Builder<>();
        }

        public Tag.Builder<T> add(Tag.Entry<T> param0) {
            this.values.add(param0);
            return this;
        }

        public Tag.Builder<T> add(T param0) {
            this.values.add(new Tag.ValuesEntry<>(Collections.singleton(param0)));
            return this;
        }

        @SafeVarargs
        public final Tag.Builder<T> add(T... param0) {
            this.values.add(new Tag.ValuesEntry<>(Lists.newArrayList(param0)));
            return this;
        }

        public Tag.Builder<T> addTag(Tag<T> param0) {
            this.values.add(new Tag.TagEntry<>(param0));
            return this;
        }

        public Tag.Builder<T> keepOrder(boolean param0) {
            this.ordered = param0;
            return this;
        }

        public boolean canBuild(Function<ResourceLocation, Tag<T>> param0) {
            for(Tag.Entry<T> var0 : this.values) {
                if (!var0.canBuild(param0)) {
                    return false;
                }
            }

            return true;
        }

        public Tag<T> build(ResourceLocation param0) {
            return new Tag<>(param0, this.values, this.ordered);
        }

        public Tag.Builder<T> addFromJson(Function<ResourceLocation, Optional<T>> param0, JsonObject param1) {
            JsonArray var0 = GsonHelper.getAsJsonArray(param1, "values");
            List<Tag.Entry<T>> var1 = Lists.newArrayList();

            for(JsonElement var2 : var0) {
                String var3 = GsonHelper.convertToString(var2, "value");
                if (var3.startsWith("#")) {
                    var1.add(new Tag.TagEntry<>(new ResourceLocation(var3.substring(1))));
                } else {
                    ResourceLocation var4 = new ResourceLocation(var3);
                    var1.add(
                        new Tag.ValuesEntry<>(
                            Collections.singleton(param0.apply(var4).orElseThrow(() -> new JsonParseException("Unknown value '" + var4 + "'")))
                        )
                    );
                }
            }

            if (GsonHelper.getAsBoolean(param1, "replace", false)) {
                this.values.clear();
            }

            this.values.addAll(var1);
            return this;
        }
    }

    public interface Entry<T> {
        default boolean canBuild(Function<ResourceLocation, Tag<T>> param0) {
            return true;
        }

        void build(Collection<T> var1);

        void serializeTo(JsonArray var1, Function<T, ResourceLocation> var2);
    }

    public static class TagEntry<T> implements Tag.Entry<T> {
        @Nullable
        private final ResourceLocation id;
        @Nullable
        private Tag<T> tag;

        public TagEntry(ResourceLocation param0) {
            this.id = param0;
        }

        public TagEntry(Tag<T> param0) {
            this.id = param0.getId();
            this.tag = param0;
        }

        @Override
        public boolean canBuild(Function<ResourceLocation, Tag<T>> param0) {
            if (this.tag == null) {
                this.tag = param0.apply(this.id);
            }

            return this.tag != null;
        }

        @Override
        public void build(Collection<T> param0) {
            if (this.tag == null) {
                throw new IllegalStateException("Cannot build unresolved tag entry");
            } else {
                param0.addAll(this.tag.getValues());
            }
        }

        public ResourceLocation getId() {
            if (this.tag != null) {
                return this.tag.getId();
            } else if (this.id != null) {
                return this.id;
            } else {
                throw new IllegalStateException("Cannot serialize an anonymous tag to json!");
            }
        }

        @Override
        public void serializeTo(JsonArray param0, Function<T, ResourceLocation> param1) {
            param0.add("#" + this.getId());
        }
    }

    public static class ValuesEntry<T> implements Tag.Entry<T> {
        private final Collection<T> values;

        public ValuesEntry(Collection<T> param0) {
            this.values = param0;
        }

        @Override
        public void build(Collection<T> param0) {
            param0.addAll(this.values);
        }

        @Override
        public void serializeTo(JsonArray param0, Function<T, ResourceLocation> param1) {
            for(T var0 : this.values) {
                ResourceLocation var1 = param1.apply(var0);
                if (var1 == null) {
                    throw new IllegalStateException("Unable to serialize an anonymous value to json!");
                }

                param0.add(var1.toString());
            }

        }

        public Collection<T> getValues() {
            return this.values;
        }
    }
}
