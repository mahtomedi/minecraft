package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class PlayerAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VISIBILITY_DEPTH = 2;
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer())
        .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
        .setPrettyPrinting()
        .create();
    private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
    };
    private final DataFixer dataFixer;
    private final PlayerList playerList;
    private final File file;
    private final Map<Advancement, AdvancementProgress> advancements = Maps.newLinkedHashMap();
    private final Set<Advancement> visible = Sets.newLinkedHashSet();
    private final Set<Advancement> visibilityChanged = Sets.newLinkedHashSet();
    private final Set<Advancement> progressChanged = Sets.newLinkedHashSet();
    private ServerPlayer player;
    @Nullable
    private Advancement lastSelectedTab;
    private boolean isFirstPacket = true;

    public PlayerAdvancements(DataFixer param0, PlayerList param1, ServerAdvancementManager param2, File param3, ServerPlayer param4) {
        this.dataFixer = param0;
        this.playerList = param1;
        this.file = param3;
        this.player = param4;
        this.load(param2);
    }

    public void setPlayer(ServerPlayer param0) {
        this.player = param0;
    }

    public void stopListening() {
        for(CriterionTrigger<?> var0 : CriteriaTriggers.all()) {
            var0.removePlayerListeners(this);
        }

    }

    public void reload(ServerAdvancementManager param0) {
        this.stopListening();
        this.advancements.clear();
        this.visible.clear();
        this.visibilityChanged.clear();
        this.progressChanged.clear();
        this.isFirstPacket = true;
        this.lastSelectedTab = null;
        this.load(param0);
    }

    private void registerListeners(ServerAdvancementManager param0) {
        for(Advancement var0 : param0.getAllAdvancements()) {
            this.registerListeners(var0);
        }

    }

    private void ensureAllVisible() {
        List<Advancement> var0 = Lists.newArrayList();

        for(Entry<Advancement, AdvancementProgress> var1 : this.advancements.entrySet()) {
            if (var1.getValue().isDone()) {
                var0.add(var1.getKey());
                this.progressChanged.add(var1.getKey());
            }
        }

        for(Advancement var2 : var0) {
            this.ensureVisibility(var2);
        }

    }

    private void checkForAutomaticTriggers(ServerAdvancementManager param0) {
        for(Advancement var0 : param0.getAllAdvancements()) {
            if (var0.getCriteria().isEmpty()) {
                this.award(var0, "");
                var0.getRewards().grant(this.player);
            }
        }

    }

    private void load(ServerAdvancementManager param0) {
        if (this.file.isFile()) {
            try (JsonReader var0 = new JsonReader(new StringReader(Files.toString(this.file, StandardCharsets.UTF_8)))) {
                var0.setLenient(false);
                Dynamic<JsonElement> var1 = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(var0));
                if (!var1.get("DataVersion").asNumber().result().isPresent()) {
                    var1 = var1.set("DataVersion", var1.createInt(1343));
                }

                var1 = this.dataFixer
                    .update(DataFixTypes.ADVANCEMENTS.getType(), var1, var1.get("DataVersion").asInt(0), SharedConstants.getCurrentVersion().getWorldVersion());
                var1 = var1.remove("DataVersion");
                Map<ResourceLocation, AdvancementProgress> var2 = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(var1.getValue());
                if (var2 == null) {
                    throw new JsonParseException("Found null for advancements");
                }

                Stream<Entry<ResourceLocation, AdvancementProgress>> var3 = var2.entrySet().stream().sorted(Comparator.comparing(Entry::getValue));

                for(Entry<ResourceLocation, AdvancementProgress> var4 : var3.collect(Collectors.toList())) {
                    Advancement var5 = param0.getAdvancement(var4.getKey());
                    if (var5 == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", var4.getKey(), this.file);
                    } else {
                        this.startProgress(var5, var4.getValue());
                    }
                }
            } catch (JsonParseException var11) {
                LOGGER.error("Couldn't parse player advancements in {}", this.file, var11);
            } catch (IOException var12) {
                LOGGER.error("Couldn't access player advancements in {}", this.file, var12);
            }
        }

        this.checkForAutomaticTriggers(param0);
        this.ensureAllVisible();
        this.registerListeners(param0);
    }

    public void save() {
        Map<ResourceLocation, AdvancementProgress> var0 = Maps.newHashMap();

        for(Entry<Advancement, AdvancementProgress> var1 : this.advancements.entrySet()) {
            AdvancementProgress var2 = var1.getValue();
            if (var2.hasProgress()) {
                var0.put(var1.getKey().getId(), var2);
            }
        }

        if (this.file.getParentFile() != null) {
            this.file.getParentFile().mkdirs();
        }

        JsonElement var3 = GSON.toJsonTree(var0);
        var3.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

        try (
            OutputStream var4 = new FileOutputStream(this.file);
            Writer var5 = new OutputStreamWriter(var4, Charsets.UTF_8.newEncoder());
        ) {
            GSON.toJson(var3, var5);
        } catch (IOException var11) {
            LOGGER.error("Couldn't save player advancements to {}", this.file, var11);
        }

    }

    public boolean award(Advancement param0, String param1) {
        boolean var0 = false;
        AdvancementProgress var1 = this.getOrStartProgress(param0);
        boolean var2 = var1.isDone();
        if (var1.grantProgress(param1)) {
            this.unregisterListeners(param0);
            this.progressChanged.add(param0);
            var0 = true;
            if (!var2 && var1.isDone()) {
                param0.getRewards().grant(this.player);
                if (param0.getDisplay() != null
                    && param0.getDisplay().shouldAnnounceChat()
                    && this.player.level.getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
                    this.playerList
                        .broadcastSystemMessage(
                            Component.translatable(
                                "chat.type.advancement." + param0.getDisplay().getFrame().getName(), this.player.getDisplayName(), param0.getChatComponent()
                            ),
                            false
                        );
                }
            }
        }

        if (var1.isDone()) {
            this.ensureVisibility(param0);
        }

        return var0;
    }

    public boolean revoke(Advancement param0, String param1) {
        boolean var0 = false;
        AdvancementProgress var1 = this.getOrStartProgress(param0);
        if (var1.revokeProgress(param1)) {
            this.registerListeners(param0);
            this.progressChanged.add(param0);
            var0 = true;
        }

        if (!var1.hasProgress()) {
            this.ensureVisibility(param0);
        }

        return var0;
    }

    private void registerListeners(Advancement param0) {
        AdvancementProgress var0 = this.getOrStartProgress(param0);
        if (!var0.isDone()) {
            for(Entry<String, Criterion> var1 : param0.getCriteria().entrySet()) {
                CriterionProgress var2 = var0.getCriterion(var1.getKey());
                if (var2 != null && !var2.isDone()) {
                    CriterionTriggerInstance var3 = var1.getValue().getTrigger();
                    if (var3 != null) {
                        CriterionTrigger<CriterionTriggerInstance> var4 = CriteriaTriggers.getCriterion(var3.getCriterion());
                        if (var4 != null) {
                            var4.addPlayerListener(this, new CriterionTrigger.Listener<>(var3, param0, var1.getKey()));
                        }
                    }
                }
            }

        }
    }

    private void unregisterListeners(Advancement param0) {
        AdvancementProgress var0 = this.getOrStartProgress(param0);

        for(Entry<String, Criterion> var1 : param0.getCriteria().entrySet()) {
            CriterionProgress var2 = var0.getCriterion(var1.getKey());
            if (var2 != null && (var2.isDone() || var0.isDone())) {
                CriterionTriggerInstance var3 = var1.getValue().getTrigger();
                if (var3 != null) {
                    CriterionTrigger<CriterionTriggerInstance> var4 = CriteriaTriggers.getCriterion(var3.getCriterion());
                    if (var4 != null) {
                        var4.removePlayerListener(this, new CriterionTrigger.Listener<>(var3, param0, var1.getKey()));
                    }
                }
            }
        }

    }

    public void flushDirty(ServerPlayer param0) {
        if (this.isFirstPacket || !this.visibilityChanged.isEmpty() || !this.progressChanged.isEmpty()) {
            Map<ResourceLocation, AdvancementProgress> var0 = Maps.newHashMap();
            Set<Advancement> var1 = Sets.newLinkedHashSet();
            Set<ResourceLocation> var2 = Sets.newLinkedHashSet();

            for(Advancement var3 : this.progressChanged) {
                if (this.visible.contains(var3)) {
                    var0.put(var3.getId(), this.advancements.get(var3));
                }
            }

            for(Advancement var4 : this.visibilityChanged) {
                if (this.visible.contains(var4)) {
                    var1.add(var4);
                } else {
                    var2.add(var4.getId());
                }
            }

            if (this.isFirstPacket || !var0.isEmpty() || !var1.isEmpty() || !var2.isEmpty()) {
                param0.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, var1, var2, var0));
                this.visibilityChanged.clear();
                this.progressChanged.clear();
            }
        }

        this.isFirstPacket = false;
    }

    public void setSelectedTab(@Nullable Advancement param0) {
        Advancement var0 = this.lastSelectedTab;
        if (param0 != null && param0.getParent() == null && param0.getDisplay() != null) {
            this.lastSelectedTab = param0;
        } else {
            this.lastSelectedTab = null;
        }

        if (var0 != this.lastSelectedTab) {
            this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
        }

    }

    public AdvancementProgress getOrStartProgress(Advancement param0) {
        AdvancementProgress var0 = this.advancements.get(param0);
        if (var0 == null) {
            var0 = new AdvancementProgress();
            this.startProgress(param0, var0);
        }

        return var0;
    }

    private void startProgress(Advancement param0, AdvancementProgress param1) {
        param1.update(param0.getCriteria(), param0.getRequirements());
        this.advancements.put(param0, param1);
    }

    private void ensureVisibility(Advancement param0) {
        boolean var0 = this.shouldBeVisible(param0);
        boolean var1 = this.visible.contains(param0);
        if (var0 && !var1) {
            this.visible.add(param0);
            this.visibilityChanged.add(param0);
            if (this.advancements.containsKey(param0)) {
                this.progressChanged.add(param0);
            }
        } else if (!var0 && var1) {
            this.visible.remove(param0);
            this.visibilityChanged.add(param0);
        }

        if (var0 != var1 && param0.getParent() != null) {
            this.ensureVisibility(param0.getParent());
        }

        for(Advancement var2 : param0.getChildren()) {
            this.ensureVisibility(var2);
        }

    }

    private boolean shouldBeVisible(Advancement param0) {
        for(int var0 = 0; param0 != null && var0 <= 2; ++var0) {
            if (var0 == 0 && this.hasCompletedChildrenOrSelf(param0)) {
                return true;
            }

            if (param0.getDisplay() == null) {
                return false;
            }

            AdvancementProgress var1 = this.getOrStartProgress(param0);
            if (var1.isDone()) {
                return true;
            }

            if (param0.getDisplay().isHidden()) {
                return false;
            }

            param0 = param0.getParent();
        }

        return false;
    }

    private boolean hasCompletedChildrenOrSelf(Advancement param0) {
        AdvancementProgress var0 = this.getOrStartProgress(param0);
        if (var0.isDone()) {
            return true;
        } else {
            for(Advancement var1 : param0.getChildren()) {
                if (this.hasCompletedChildrenOrSelf(var1)) {
                    return true;
                }
            }

            return false;
        }
    }
}
