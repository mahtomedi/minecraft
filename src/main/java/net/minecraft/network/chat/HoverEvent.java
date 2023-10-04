package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoverEvent {
    public static final Codec<HoverEvent> CODEC = Codec.either(HoverEvent.TypedHoverEvent.CODEC.codec(), HoverEvent.TypedHoverEvent.LEGACY_CODEC.codec())
        .xmap(
            param0 -> new HoverEvent(
                    param0.map(
                        (Function<? super HoverEvent.TypedHoverEvent<?>, ? extends HoverEvent.TypedHoverEvent<?>>)(param0x -> param0x),
                        (Function<? super HoverEvent.TypedHoverEvent<?>, ? extends HoverEvent.TypedHoverEvent<?>>)(param0x -> param0x)
                    )
                ),
            param0 -> Either.left(param0.event)
        );
    private final HoverEvent.TypedHoverEvent<?> event;

    public <T> HoverEvent(HoverEvent.Action<T> param0, T param1) {
        this(new HoverEvent.TypedHoverEvent<>(param0, param1));
    }

    private HoverEvent(HoverEvent.TypedHoverEvent<?> param0) {
        this.event = param0;
    }

    public HoverEvent.Action<?> getAction() {
        return this.event.action;
    }

    @Nullable
    public <T> T getValue(HoverEvent.Action<T> param0) {
        return this.event.action == param0 ? param0.cast(this.event.value) : null;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            return param0 != null && this.getClass() == param0.getClass() ? ((HoverEvent)param0).event.equals(this.event) : false;
        }
    }

    @Override
    public String toString() {
        return this.event.toString();
    }

    @Override
    public int hashCode() {
        return this.event.hashCode();
    }

    public static class Action<T> implements StringRepresentable {
        public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>(
            "show_text", true, ComponentSerialization.CODEC, DataResult::success
        );
        public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>(
            "show_item", true, HoverEvent.ItemStackInfo.CODEC, HoverEvent.ItemStackInfo::legacyCreate
        );
        public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>(
            "show_entity", true, HoverEvent.EntityTooltipInfo.CODEC, HoverEvent.EntityTooltipInfo::legacyCreate
        );
        public static final Codec<HoverEvent.Action<?>> UNSAFE_CODEC = StringRepresentable.fromValues(
            () -> new HoverEvent.Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY}
        );
        public static final Codec<HoverEvent.Action<?>> CODEC = ExtraCodecs.validate(UNSAFE_CODEC, HoverEvent.Action::filterForSerialization);
        private final String name;
        private final boolean allowFromServer;
        final Codec<HoverEvent.TypedHoverEvent<T>> codec;
        final Codec<HoverEvent.TypedHoverEvent<T>> legacyCodec;

        public Action(String param0, boolean param1, Codec<T> param2, Function<Component, DataResult<T>> param3) {
            this.name = param0;
            this.allowFromServer = param1;
            this.codec = param2.xmap(param0x -> new HoverEvent.TypedHoverEvent<>(this, param0x), param0x -> param0x.value).fieldOf("contents").codec();
            this.legacyCodec = Codec.of(
                Encoder.error("Can't encode in legacy format"),
                ComponentSerialization.CODEC.flatMap(param3).map(param0x -> new HoverEvent.TypedHoverEvent<>(this, param0x))
            );
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        T cast(Object param0) {
            return (T)param0;
        }

        @Override
        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<HoverEvent.Action<?>> filterForSerialization(@Nullable HoverEvent.Action<?> param0) {
            if (param0 == null) {
                return DataResult.error(() -> "Unknown action");
            } else {
                return !param0.isAllowedFromServer() ? DataResult.error(() -> "Action not allowed: " + param0) : DataResult.success(param0, Lifecycle.stable());
            }
        }
    }

    public static class EntityTooltipInfo {
        public static final Codec<HoverEvent.EntityTooltipInfo> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(param0x -> param0x.type),
                        UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter(param0x -> param0x.id),
                        ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "name").forGetter(param0x -> param0x.name)
                    )
                    .apply(param0, HoverEvent.EntityTooltipInfo::new)
        );
        public final EntityType<?> type;
        public final UUID id;
        public final Optional<Component> name;
        @Nullable
        private List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> param0, UUID param1, @Nullable Component param2) {
            this(param0, param1, Optional.ofNullable(param2));
        }

        public EntityTooltipInfo(EntityType<?> param0, UUID param1, Optional<Component> param2) {
            this.type = param0;
            this.id = param1;
            this.name = param2;
        }

        public static DataResult<HoverEvent.EntityTooltipInfo> legacyCreate(Component param0) {
            try {
                CompoundTag var0 = TagParser.parseTag(param0.getString());
                Component var1 = Component.Serializer.fromJson(var0.getString("name"));
                EntityType<?> var2 = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(var0.getString("type")));
                UUID var3 = UUID.fromString(var0.getString("id"));
                return DataResult.success(new HoverEvent.EntityTooltipInfo(var2, var3, var1));
            } catch (Exception var5) {
                return DataResult.error(() -> "Failed to parse tooltip: " + var5.getMessage());
            }
        }

        public List<Component> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList<>();
                this.name.ifPresent(this.linesCache::add);
                this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(Component.literal(this.id.toString()));
            }

            return this.linesCache;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                HoverEvent.EntityTooltipInfo var0 = (HoverEvent.EntityTooltipInfo)param0;
                return this.type.equals(var0.type) && this.id.equals(var0.id) && this.name.equals(var0.name);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.type.hashCode();
            var0 = 31 * var0 + this.id.hashCode();
            return 31 * var0 + this.name.hashCode();
        }
    }

    public static class ItemStackInfo {
        public static final Codec<HoverEvent.ItemStackInfo> FULL_CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(param0x -> param0x.item),
                        ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter(param0x -> param0x.count),
                        ExtraCodecs.strictOptionalField(TagParser.AS_CODEC, "tag").forGetter(param0x -> param0x.tag)
                    )
                    .apply(param0, HoverEvent.ItemStackInfo::new)
        );
        public static final Codec<HoverEvent.ItemStackInfo> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), FULL_CODEC)
            .xmap(param0 -> param0.map(param0x -> new HoverEvent.ItemStackInfo(param0x, 1, Optional.empty()), param0x -> param0x), Either::right);
        private final Item item;
        private final int count;
        private final Optional<CompoundTag> tag;
        @Nullable
        private ItemStack itemStack;

        ItemStackInfo(Item param0, int param1, @Nullable CompoundTag param2) {
            this(param0, param1, Optional.ofNullable(param2));
        }

        ItemStackInfo(Item param0, int param1, Optional<CompoundTag> param2) {
            this.item = param0;
            this.count = param1;
            this.tag = param2;
        }

        public ItemStackInfo(ItemStack param0) {
            this(param0.getItem(), param0.getCount(), param0.getTag() != null ? Optional.of(param0.getTag().copy()) : Optional.empty());
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                HoverEvent.ItemStackInfo var0 = (HoverEvent.ItemStackInfo)param0;
                return this.count == var0.count && this.item.equals(var0.item) && this.tag.equals(var0.tag);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.item.hashCode();
            var0 = 31 * var0 + this.count;
            return 31 * var0 + this.tag.hashCode();
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count);
                this.tag.ifPresent(this.itemStack::setTag);
            }

            return this.itemStack;
        }

        private static DataResult<HoverEvent.ItemStackInfo> legacyCreate(Component param0) {
            try {
                CompoundTag var0 = TagParser.parseTag(param0.getString());
                return DataResult.success(new HoverEvent.ItemStackInfo(ItemStack.of(var0)));
            } catch (CommandSyntaxException var2) {
                return DataResult.error(() -> "Failed to parse item tag: " + var2.getMessage());
            }
        }
    }

    static record TypedHoverEvent<T>(HoverEvent.Action<T> action, T value) {
        public static final MapCodec<HoverEvent.TypedHoverEvent<?>> CODEC = HoverEvent.Action.CODEC
            .dispatchMap("action", HoverEvent.TypedHoverEvent::action, param0 -> param0.codec);
        public static final MapCodec<HoverEvent.TypedHoverEvent<?>> LEGACY_CODEC = HoverEvent.Action.CODEC
            .dispatchMap("action", HoverEvent.TypedHoverEvent::action, param0 -> param0.legacyCodec);
    }
}
