package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootDataResolver;

public record Advancement(
    Optional<ResourceLocation> parent,
    Optional<DisplayInfo> display,
    AdvancementRewards rewards,
    Map<String, Criterion<?>> criteria,
    AdvancementRequirements requirements,
    boolean sendsTelemetryEvent,
    Optional<Component> name
) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = ExtraCodecs.validate(
        Codec.unboundedMap(Codec.STRING, Criterion.CODEC),
        param0 -> param0.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success(param0)
    );
    public static final Codec<Advancement> CODEC = ExtraCodecs.validate(
        RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "parent").forGetter(Advancement::parent),
                        ExtraCodecs.strictOptionalField(DisplayInfo.CODEC, "display").forGetter(Advancement::display),
                        ExtraCodecs.strictOptionalField(AdvancementRewards.CODEC, "rewards", AdvancementRewards.EMPTY).forGetter(Advancement::rewards),
                        CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria),
                        ExtraCodecs.strictOptionalField(AdvancementRequirements.CODEC, "requirements")
                            .forGetter(param0x -> Optional.of(param0x.requirements())),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "sends_telemetry_event", false).forGetter(Advancement::sendsTelemetryEvent)
                    )
                    .apply(param0, (param0x, param1, param2, param3, param4, param5) -> {
                        AdvancementRequirements var0x = param4.orElseGet(() -> AdvancementRequirements.allOf(param3.keySet()));
                        return new Advancement(param0x, param1, param2, param3, var0x, param5);
                    })
        ),
        Advancement::validate
    );

    public Advancement(
        Optional<ResourceLocation> param0,
        Optional<DisplayInfo> param1,
        AdvancementRewards param2,
        Map<String, Criterion<?>> param3,
        AdvancementRequirements param4,
        boolean param5
    ) {
        this(param0, param1, param2, Map.copyOf(param3), param4, param5, param1.map(Advancement::decorateName));
    }

    private static DataResult<Advancement> validate(Advancement param0) {
        return param0.requirements().validate(param0.criteria().keySet()).map(param1 -> param0);
    }

    private static Component decorateName(DisplayInfo param0x) {
        Component var0 = param0x.getTitle();
        ChatFormatting var1 = param0x.getType().getChatColor();
        Component var2 = ComponentUtils.mergeStyles(var0.copy(), Style.EMPTY.withColor(var1)).append("\n").append(param0x.getDescription());
        Component var3 = var0.copy().withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var2)));
        return ComponentUtils.wrapInSquareBrackets(var3).withStyle(var1);
    }

    public static Component name(AdvancementHolder param0) {
        return param0.value().name().orElseGet(() -> Component.literal(param0.id().toString()));
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeOptional(this.parent, FriendlyByteBuf::writeResourceLocation);
        param0.writeOptional(this.display, (param0x, param1) -> param1.serializeToNetwork(param0x));
        this.requirements.write(param0);
        param0.writeBoolean(this.sendsTelemetryEvent);
    }

    public static Advancement read(FriendlyByteBuf param0) {
        return new Advancement(
            param0.readOptional(FriendlyByteBuf::readResourceLocation),
            param0.readOptional(DisplayInfo::fromNetwork),
            AdvancementRewards.EMPTY,
            Map.of(),
            new AdvancementRequirements(param0),
            param0.readBoolean()
        );
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ProblemReporter param0, LootDataResolver param1) {
        this.criteria.forEach((param2, param3) -> {
            CriterionValidator var0 = new CriterionValidator(param0.forChild(param2), param1);
            param3.triggerInstance().validate(var0);
        });
    }

    public static class Builder {
        private Optional<ResourceLocation> parent = Optional.empty();
        private Optional<DisplayInfo> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private final ImmutableMap.Builder<String, Criterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private boolean sendsTelemetryEvent;

        public static Advancement.Builder advancement() {
            return new Advancement.Builder().sendsTelemetryEvent();
        }

        public static Advancement.Builder recipeAdvancement() {
            return new Advancement.Builder();
        }

        public Advancement.Builder parent(AdvancementHolder param0) {
            this.parent = Optional.of(param0.id());
            return this;
        }

        @Deprecated(
            forRemoval = true
        )
        public Advancement.Builder parent(ResourceLocation param0) {
            this.parent = Optional.of(param0);
            return this;
        }

        public Advancement.Builder display(
            ItemStack param0,
            Component param1,
            Component param2,
            @Nullable ResourceLocation param3,
            AdvancementType param4,
            boolean param5,
            boolean param6,
            boolean param7
        ) {
            return this.display(new DisplayInfo(param0, param1, param2, Optional.ofNullable(param3), param4, param5, param6, param7));
        }

        public Advancement.Builder display(
            ItemLike param0,
            Component param1,
            Component param2,
            @Nullable ResourceLocation param3,
            AdvancementType param4,
            boolean param5,
            boolean param6,
            boolean param7
        ) {
            return this.display(new DisplayInfo(new ItemStack(param0.asItem()), param1, param2, Optional.ofNullable(param3), param4, param5, param6, param7));
        }

        public Advancement.Builder display(DisplayInfo param0) {
            this.display = Optional.of(param0);
            return this;
        }

        public Advancement.Builder rewards(AdvancementRewards.Builder param0) {
            return this.rewards(param0.build());
        }

        public Advancement.Builder rewards(AdvancementRewards param0) {
            this.rewards = param0;
            return this;
        }

        public Advancement.Builder addCriterion(String param0, Criterion<?> param1) {
            this.criteria.put(param0, param1);
            return this;
        }

        public Advancement.Builder requirements(AdvancementRequirements.Strategy param0) {
            this.requirementsStrategy = param0;
            return this;
        }

        public Advancement.Builder requirements(AdvancementRequirements param0) {
            this.requirements = Optional.of(param0);
            return this;
        }

        public Advancement.Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementHolder build(ResourceLocation param0) {
            Map<String, Criterion<?>> var0 = this.criteria.buildOrThrow();
            AdvancementRequirements var1 = this.requirements.orElseGet(() -> this.requirementsStrategy.create(var0.keySet()));
            return new AdvancementHolder(param0, new Advancement(this.parent, this.display, this.rewards, var0, var1, this.sendsTelemetryEvent));
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> param0, String param1) {
            AdvancementHolder var0 = this.build(new ResourceLocation(param1));
            param0.accept(var0);
            return var0;
        }
    }
}
