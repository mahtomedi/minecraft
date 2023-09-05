package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record Advancement(
    Optional<ResourceLocation> parent,
    Optional<DisplayInfo> display,
    AdvancementRewards rewards,
    Map<String, Criterion<?>> criteria,
    AdvancementRequirements requirements,
    boolean sendsTelemetryEvent,
    Optional<Component> name
) {
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

    private static Component decorateName(DisplayInfo param0x) {
        Component var0 = param0x.getTitle();
        ChatFormatting var1 = param0x.getFrame().getChatColor();
        Component var2 = ComponentUtils.mergeStyles(var0.copy(), Style.EMPTY.withColor(var1)).append("\n").append(param0x.getDescription());
        Component var3 = var0.copy().withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var2)));
        return ComponentUtils.wrapInSquareBrackets(var3).withStyle(var1);
    }

    public static Component name(AdvancementHolder param0) {
        return param0.value().name().orElseGet(() -> Component.literal(param0.id().toString()));
    }

    public JsonObject serializeToJson() {
        JsonObject var0 = new JsonObject();
        this.parent.ifPresent(param1 -> var0.addProperty("parent", param1.toString()));
        this.display.ifPresent(param1 -> var0.add("display", param1.serializeToJson()));
        var0.add("rewards", this.rewards.serializeToJson());
        JsonObject var1 = new JsonObject();

        for(Entry<String, Criterion<?>> var2 : this.criteria.entrySet()) {
            var1.add(var2.getKey(), var2.getValue().serializeToJson());
        }

        var0.add("criteria", var1);
        var0.add("requirements", this.requirements.toJson());
        var0.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
        return var0;
    }

    public static Advancement fromJson(JsonObject param0, DeserializationContext param1) {
        Optional<ResourceLocation> var0 = param0.has("parent") ? Optional.of(new ResourceLocation(GsonHelper.getAsString(param0, "parent"))) : Optional.empty();
        Optional<DisplayInfo> var1 = param0.has("display")
            ? Optional.of(DisplayInfo.fromJson(GsonHelper.getAsJsonObject(param0, "display")))
            : Optional.empty();
        AdvancementRewards var2 = param0.has("rewards")
            ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(param0, "rewards"))
            : AdvancementRewards.EMPTY;
        Map<String, Criterion<?>> var3 = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(param0, "criteria"), param1);
        if (var3.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
        } else {
            JsonArray var4 = GsonHelper.getAsJsonArray(param0, "requirements", new JsonArray());
            AdvancementRequirements var5;
            if (var4.isEmpty()) {
                var5 = AdvancementRequirements.allOf(var3.keySet());
            } else {
                var5 = AdvancementRequirements.fromJson(var4, var3.keySet());
            }

            boolean var7 = GsonHelper.getAsBoolean(param0, "sends_telemetry_event", false);
            return new Advancement(var0, var1, var2, var3, var5, var7);
        }
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
            FrameType param4,
            boolean param5,
            boolean param6,
            boolean param7
        ) {
            return this.display(new DisplayInfo(param0, param1, param2, param3, param4, param5, param6, param7));
        }

        public Advancement.Builder display(
            ItemLike param0,
            Component param1,
            Component param2,
            @Nullable ResourceLocation param3,
            FrameType param4,
            boolean param5,
            boolean param6,
            boolean param7
        ) {
            return this.display(new DisplayInfo(new ItemStack(param0.asItem()), param1, param2, param3, param4, param5, param6, param7));
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
