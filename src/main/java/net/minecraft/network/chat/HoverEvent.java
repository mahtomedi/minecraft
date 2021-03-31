package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HoverEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    private final HoverEvent.Action<?> action;
    private final Object value;

    public <T> HoverEvent(HoverEvent.Action<T> param0, T param1) {
        this.action = param0;
        this.value = param1;
    }

    public HoverEvent.Action<?> getAction() {
        return this.action;
    }

    @Nullable
    public <T> T getValue(HoverEvent.Action<T> param0) {
        return this.action == param0 ? param0.cast(this.value) : null;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            HoverEvent var0 = (HoverEvent)param0;
            return this.action == var0.action && Objects.equals(this.value, var0.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
    }

    @Override
    public int hashCode() {
        int var0 = this.action.hashCode();
        return 31 * var0 + (this.value != null ? this.value.hashCode() : 0);
    }

    @Nullable
    public static HoverEvent deserialize(JsonObject param0) {
        String var0 = GsonHelper.getAsString(param0, "action", null);
        if (var0 == null) {
            return null;
        } else {
            HoverEvent.Action<?> var1 = HoverEvent.Action.getByName(var0);
            if (var1 == null) {
                return null;
            } else {
                JsonElement var2 = param0.get("contents");
                if (var2 != null) {
                    return var1.deserialize(var2);
                } else {
                    Component var3 = Component.Serializer.fromJson(param0.get("value"));
                    return var3 != null ? var1.deserializeFromLegacy(var3) : null;
                }
            }
        }
    }

    public JsonObject serialize() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("action", this.action.getName());
        var0.add("contents", this.action.serializeArg(this.value));
        return var0;
    }

    public static class Action<T> {
        public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>(
            "show_text", true, Component.Serializer::fromJson, Component.Serializer::toJsonTree, Function.identity()
        );
        public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>(
            "show_item",
            true,
            param0 -> HoverEvent.ItemStackInfo.create(param0),
            param0 -> param0.serialize(),
            param0 -> HoverEvent.ItemStackInfo.create(param0)
        );
        public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>(
            "show_entity", true, HoverEvent.EntityTooltipInfo::create, HoverEvent.EntityTooltipInfo::serialize, HoverEvent.EntityTooltipInfo::create
        );
        private static final Map<String, HoverEvent.Action<?>> LOOKUP = Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY)
            .collect(
                ImmutableMap.toImmutableMap(HoverEvent.Action::getName, (Function<? super HoverEvent.Action, ? extends HoverEvent.Action<?>>)(param0 -> param0))
            );
        private final String name;
        private final boolean allowFromServer;
        private final Function<JsonElement, T> argDeserializer;
        private final Function<T, JsonElement> argSerializer;
        private final Function<Component, T> legacyArgDeserializer;

        public Action(String param0, boolean param1, Function<JsonElement, T> param2, Function<T, JsonElement> param3, Function<Component, T> param4) {
            this.name = param0;
            this.allowFromServer = param1;
            this.argDeserializer = param2;
            this.argSerializer = param3;
            this.legacyArgDeserializer = param4;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static HoverEvent.Action<?> getByName(String param0) {
            return LOOKUP.get(param0);
        }

        private T cast(Object param0) {
            return (T)param0;
        }

        @Nullable
        public HoverEvent deserialize(JsonElement param0) {
            T var0 = this.argDeserializer.apply(param0);
            return var0 == null ? null : new HoverEvent(this, var0);
        }

        @Nullable
        public HoverEvent deserializeFromLegacy(Component param0) {
            T var0 = this.legacyArgDeserializer.apply(param0);
            return var0 == null ? null : new HoverEvent(this, var0);
        }

        public JsonElement serializeArg(Object param0) {
            return this.argSerializer.apply(this.cast(param0));
        }

        @Override
        public String toString() {
            return "<action " + this.name + ">";
        }
    }

    public static class EntityTooltipInfo {
        public final EntityType<?> type;
        public final UUID id;
        @Nullable
        public final Component name;
        @Nullable
        private List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> param0, UUID param1, @Nullable Component param2) {
            this.type = param0;
            this.id = param1;
            this.name = param2;
        }

        @Nullable
        public static HoverEvent.EntityTooltipInfo create(JsonElement param0) {
            if (!param0.isJsonObject()) {
                return null;
            } else {
                JsonObject var0 = param0.getAsJsonObject();
                EntityType<?> var1 = Registry.ENTITY_TYPE.get(new ResourceLocation(GsonHelper.getAsString(var0, "type")));
                UUID var2 = UUID.fromString(GsonHelper.getAsString(var0, "id"));
                Component var3 = Component.Serializer.fromJson(var0.get("name"));
                return new HoverEvent.EntityTooltipInfo(var1, var2, var3);
            }
        }

        @Nullable
        public static HoverEvent.EntityTooltipInfo create(Component param0) {
            try {
                CompoundTag var0 = TagParser.parseTag(param0.getString());
                Component var1 = Component.Serializer.fromJson(var0.getString("name"));
                EntityType<?> var2 = Registry.ENTITY_TYPE.get(new ResourceLocation(var0.getString("type")));
                UUID var3 = UUID.fromString(var0.getString("id"));
                return new HoverEvent.EntityTooltipInfo(var2, var3, var1);
            } catch (CommandSyntaxException | JsonSyntaxException var5) {
                return null;
            }
        }

        public JsonElement serialize() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("type", Registry.ENTITY_TYPE.getKey(this.type).toString());
            var0.addProperty("id", this.id.toString());
            if (this.name != null) {
                var0.add("name", Component.Serializer.toJsonTree(this.name));
            }

            return var0;
        }

        public List<Component> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = Lists.newArrayList();
                if (this.name != null) {
                    this.linesCache.add(this.name);
                }

                this.linesCache.add(new TranslatableComponent("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(new TextComponent(this.id.toString()));
            }

            return this.linesCache;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                HoverEvent.EntityTooltipInfo var0 = (HoverEvent.EntityTooltipInfo)param0;
                return this.type.equals(var0.type) && this.id.equals(var0.id) && Objects.equals(this.name, var0.name);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.type.hashCode();
            var0 = 31 * var0 + this.id.hashCode();
            return 31 * var0 + (this.name != null ? this.name.hashCode() : 0);
        }
    }

    public static class ItemStackInfo {
        private final Item item;
        private final int count;
        @Nullable
        private final CompoundTag tag;
        @Nullable
        private ItemStack itemStack;

        ItemStackInfo(Item param0, int param1, @Nullable CompoundTag param2) {
            this.item = param0;
            this.count = param1;
            this.tag = param2;
        }

        public ItemStackInfo(ItemStack param0) {
            this(param0.getItem(), param0.getCount(), param0.getTag() != null ? param0.getTag().copy() : null);
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                HoverEvent.ItemStackInfo var0 = (HoverEvent.ItemStackInfo)param0;
                return this.count == var0.count && this.item.equals(var0.item) && Objects.equals(this.tag, var0.tag);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.item.hashCode();
            var0 = 31 * var0 + this.count;
            return 31 * var0 + (this.tag != null ? this.tag.hashCode() : 0);
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count);
                if (this.tag != null) {
                    this.itemStack.setTag(this.tag);
                }
            }

            return this.itemStack;
        }

        private static HoverEvent.ItemStackInfo create(JsonElement param0) {
            if (param0.isJsonPrimitive()) {
                return new HoverEvent.ItemStackInfo(Registry.ITEM.get(new ResourceLocation(param0.getAsString())), 1, null);
            } else {
                JsonObject var0 = GsonHelper.convertToJsonObject(param0, "item");
                Item var1 = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(var0, "id")));
                int var2 = GsonHelper.getAsInt(var0, "count", 1);
                if (var0.has("tag")) {
                    String var3 = GsonHelper.getAsString(var0, "tag");

                    try {
                        CompoundTag var4 = TagParser.parseTag(var3);
                        return new HoverEvent.ItemStackInfo(var1, var2, var4);
                    } catch (CommandSyntaxException var6) {
                        HoverEvent.LOGGER.warn("Failed to parse tag: {}", var3, var6);
                    }
                }

                return new HoverEvent.ItemStackInfo(var1, var2, null);
            }
        }

        @Nullable
        private static HoverEvent.ItemStackInfo create(Component param0) {
            try {
                CompoundTag var0 = TagParser.parseTag(param0.getString());
                return new HoverEvent.ItemStackInfo(ItemStack.of(var0));
            } catch (CommandSyntaxException var2) {
                HoverEvent.LOGGER.warn("Failed to parse item tag: {}", param0, var2);
                return null;
            }
        }

        private JsonElement serialize() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("id", Registry.ITEM.getKey(this.item).toString());
            if (this.count != 1) {
                var0.addProperty("count", this.count);
            }

            if (this.tag != null) {
                var0.addProperty("tag", this.tag.toString());
            }

            return var0;
        }
    }
}
