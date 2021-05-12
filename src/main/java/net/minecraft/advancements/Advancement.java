package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
    private final Advancement parent;
    private final DisplayInfo display;
    private final AdvancementRewards rewards;
    private final ResourceLocation id;
    private final Map<String, Criterion> criteria;
    private final String[][] requirements;
    private final Set<Advancement> children = Sets.newLinkedHashSet();
    private final Component chatComponent;

    public Advancement(
        ResourceLocation param0,
        @Nullable Advancement param1,
        @Nullable DisplayInfo param2,
        AdvancementRewards param3,
        Map<String, Criterion> param4,
        String[][] param5
    ) {
        this.id = param0;
        this.display = param2;
        this.criteria = ImmutableMap.copyOf(param4);
        this.parent = param1;
        this.rewards = param3;
        this.requirements = param5;
        if (param1 != null) {
            param1.addChild(this);
        }

        if (param2 == null) {
            this.chatComponent = new TextComponent(param0.toString());
        } else {
            Component var0 = param2.getTitle();
            ChatFormatting var1 = param2.getFrame().getChatColor();
            Component var2 = ComponentUtils.mergeStyles(var0.copy(), Style.EMPTY.withColor(var1)).append("\n").append(param2.getDescription());
            Component var3 = var0.copy().withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var2)));
            this.chatComponent = ComponentUtils.wrapInSquareBrackets(var3).withStyle(var1);
        }

    }

    public Advancement.Builder deconstruct() {
        return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
    }

    @Nullable
    public Advancement getParent() {
        return this.parent;
    }

    @Nullable
    public DisplayInfo getDisplay() {
        return this.display;
    }

    public AdvancementRewards getRewards() {
        return this.rewards;
    }

    @Override
    public String toString() {
        return "SimpleAdvancement{id="
            + this.getId()
            + ", parent="
            + (this.parent == null ? "null" : this.parent.getId())
            + ", display="
            + this.display
            + ", rewards="
            + this.rewards
            + ", criteria="
            + this.criteria
            + ", requirements="
            + Arrays.deepToString(this.requirements)
            + "}";
    }

    public Iterable<Advancement> getChildren() {
        return this.children;
    }

    public Map<String, Criterion> getCriteria() {
        return this.criteria;
    }

    public int getMaxCriteraRequired() {
        return this.requirements.length;
    }

    public void addChild(Advancement param0) {
        this.children.add(param0);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Advancement)) {
            return false;
        } else {
            Advancement var0 = (Advancement)param0;
            return this.id.equals(var0.id);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public String[][] getRequirements() {
        return this.requirements;
    }

    public Component getChatComponent() {
        return this.chatComponent;
    }

    public static class Builder {
        private ResourceLocation parentId;
        private Advancement parent;
        private DisplayInfo display;
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
        private String[][] requirements;
        private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;

        Builder(@Nullable ResourceLocation param0, @Nullable DisplayInfo param1, AdvancementRewards param2, Map<String, Criterion> param3, String[][] param4) {
            this.parentId = param0;
            this.display = param1;
            this.rewards = param2;
            this.criteria = param3;
            this.requirements = param4;
        }

        private Builder() {
        }

        public static Advancement.Builder advancement() {
            return new Advancement.Builder();
        }

        public Advancement.Builder parent(Advancement param0) {
            this.parent = param0;
            return this;
        }

        public Advancement.Builder parent(ResourceLocation param0) {
            this.parentId = param0;
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
            this.display = param0;
            return this;
        }

        public Advancement.Builder rewards(AdvancementRewards.Builder param0) {
            return this.rewards(param0.build());
        }

        public Advancement.Builder rewards(AdvancementRewards param0) {
            this.rewards = param0;
            return this;
        }

        public Advancement.Builder addCriterion(String param0, CriterionTriggerInstance param1) {
            return this.addCriterion(param0, new Criterion(param1));
        }

        public Advancement.Builder addCriterion(String param0, Criterion param1) {
            if (this.criteria.containsKey(param0)) {
                throw new IllegalArgumentException("Duplicate criterion " + param0);
            } else {
                this.criteria.put(param0, param1);
                return this;
            }
        }

        public Advancement.Builder requirements(RequirementsStrategy param0) {
            this.requirementsStrategy = param0;
            return this;
        }

        public Advancement.Builder requirements(String[][] param0) {
            this.requirements = param0;
            return this;
        }

        public boolean canBuild(Function<ResourceLocation, Advancement> param0) {
            if (this.parentId == null) {
                return true;
            } else {
                if (this.parent == null) {
                    this.parent = param0.apply(this.parentId);
                }

                return this.parent != null;
            }
        }

        public Advancement build(ResourceLocation param0) {
            if (!this.canBuild(param0x -> null)) {
                throw new IllegalStateException("Tried to build incomplete advancement!");
            } else {
                if (this.requirements == null) {
                    this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
                }

                return new Advancement(param0, this.parent, this.display, this.rewards, this.criteria, this.requirements);
            }
        }

        public Advancement save(Consumer<Advancement> param0, String param1) {
            Advancement var0 = this.build(new ResourceLocation(param1));
            param0.accept(var0);
            return var0;
        }

        public JsonObject serializeToJson() {
            if (this.requirements == null) {
                this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            JsonObject var0 = new JsonObject();
            if (this.parent != null) {
                var0.addProperty("parent", this.parent.getId().toString());
            } else if (this.parentId != null) {
                var0.addProperty("parent", this.parentId.toString());
            }

            if (this.display != null) {
                var0.add("display", this.display.serializeToJson());
            }

            var0.add("rewards", this.rewards.serializeToJson());
            JsonObject var1 = new JsonObject();

            for(Entry<String, Criterion> var2 : this.criteria.entrySet()) {
                var1.add(var2.getKey(), var2.getValue().serializeToJson());
            }

            var0.add("criteria", var1);
            JsonArray var3 = new JsonArray();

            for(String[] var4 : this.requirements) {
                JsonArray var5 = new JsonArray();

                for(String var6 : var4) {
                    var5.add(var6);
                }

                var3.add(var5);
            }

            var0.add("requirements", var3);
            return var0;
        }

        public void serializeToNetwork(FriendlyByteBuf param0) {
            if (this.parentId == null) {
                param0.writeBoolean(false);
            } else {
                param0.writeBoolean(true);
                param0.writeResourceLocation(this.parentId);
            }

            if (this.display == null) {
                param0.writeBoolean(false);
            } else {
                param0.writeBoolean(true);
                this.display.serializeToNetwork(param0);
            }

            Criterion.serializeToNetwork(this.criteria, param0);
            param0.writeVarInt(this.requirements.length);

            for(String[] var0 : this.requirements) {
                param0.writeVarInt(var0.length);

                for(String var1 : var0) {
                    param0.writeUtf(var1);
                }
            }

        }

        @Override
        public String toString() {
            return "Task Advancement{parentId="
                + this.parentId
                + ", display="
                + this.display
                + ", rewards="
                + this.rewards
                + ", criteria="
                + this.criteria
                + ", requirements="
                + Arrays.deepToString(this.requirements)
                + "}";
        }

        public static Advancement.Builder fromJson(JsonObject param0, DeserializationContext param1) {
            ResourceLocation var0 = param0.has("parent") ? new ResourceLocation(GsonHelper.getAsString(param0, "parent")) : null;
            DisplayInfo var1 = param0.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(param0, "display")) : null;
            AdvancementRewards var2 = param0.has("rewards")
                ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(param0, "rewards"))
                : AdvancementRewards.EMPTY;
            Map<String, Criterion> var3 = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(param0, "criteria"), param1);
            if (var3.isEmpty()) {
                throw new JsonSyntaxException("Advancement criteria cannot be empty");
            } else {
                JsonArray var4 = GsonHelper.getAsJsonArray(param0, "requirements", new JsonArray());
                String[][] var5 = new String[var4.size()][];

                for(int var6 = 0; var6 < var4.size(); ++var6) {
                    JsonArray var7 = GsonHelper.convertToJsonArray(var4.get(var6), "requirements[" + var6 + "]");
                    var5[var6] = new String[var7.size()];

                    for(int var8 = 0; var8 < var7.size(); ++var8) {
                        var5[var6][var8] = GsonHelper.convertToString(var7.get(var8), "requirements[" + var6 + "][" + var8 + "]");
                    }
                }

                if (var5.length == 0) {
                    var5 = new String[var3.size()][];
                    int var9 = 0;

                    for(String var10 : var3.keySet()) {
                        var5[var9++] = new String[]{var10};
                    }
                }

                for(String[] var11 : var5) {
                    if (var11.length == 0 && var3.isEmpty()) {
                        throw new JsonSyntaxException("Requirement entry cannot be empty");
                    }

                    for(String var12 : var11) {
                        if (!var3.containsKey(var12)) {
                            throw new JsonSyntaxException("Unknown required criterion '" + var12 + "'");
                        }
                    }
                }

                for(String var13 : var3.keySet()) {
                    boolean var14 = false;

                    for(String[] var15 : var5) {
                        if (ArrayUtils.contains(var15, var13)) {
                            var14 = true;
                            break;
                        }
                    }

                    if (!var14) {
                        throw new JsonSyntaxException(
                            "Criterion '" + var13 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required."
                        );
                    }
                }

                return new Advancement.Builder(var0, var1, var2, var3, var5);
            }
        }

        public static Advancement.Builder fromNetwork(FriendlyByteBuf param0) {
            ResourceLocation var0 = param0.readBoolean() ? param0.readResourceLocation() : null;
            DisplayInfo var1 = param0.readBoolean() ? DisplayInfo.fromNetwork(param0) : null;
            Map<String, Criterion> var2 = Criterion.criteriaFromNetwork(param0);
            String[][] var3 = new String[param0.readVarInt()][];

            for(int var4 = 0; var4 < var3.length; ++var4) {
                var3[var4] = new String[param0.readVarInt()];

                for(int var5 = 0; var5 < var3[var4].length; ++var5) {
                    var3[var4][var5] = param0.readUtf();
                }
            }

            return new Advancement.Builder(var0, var1, AdvancementRewards.EMPTY, var2, var3);
        }

        public Map<String, Criterion> getCriteria() {
            return this.criteria;
        }
    }
}
