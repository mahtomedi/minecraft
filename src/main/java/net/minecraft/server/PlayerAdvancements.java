package net.minecraft.server;

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
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
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
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class PlayerAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer())
        .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
        .setPrettyPrinting()
        .create();
    private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
    };
    private final DataFixer dataFixer;
    private final PlayerList playerList;
    private final Path playerSavePath;
    private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<>();
    private final Set<Advancement> visible = new HashSet<>();
    private final Set<Advancement> progressChanged = new HashSet<>();
    private final Set<Advancement> rootsToUpdate = new HashSet<>();
    private ServerPlayer player;
    @Nullable
    private Advancement lastSelectedTab;
    private boolean isFirstPacket = true;

    public PlayerAdvancements(DataFixer param0, PlayerList param1, ServerAdvancementManager param2, Path param3, ServerPlayer param4) {
        this.dataFixer = param0;
        this.playerList = param1;
        this.playerSavePath = param3;
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
        this.progress.clear();
        this.visible.clear();
        this.rootsToUpdate.clear();
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

    private void checkForAutomaticTriggers(ServerAdvancementManager param0) {
        for(Advancement var0 : param0.getAllAdvancements()) {
            if (var0.getCriteria().isEmpty()) {
                this.award(var0, "");
                var0.getRewards().grant(this.player);
            }
        }

    }

    private void load(ServerAdvancementManager param0) {
        if (Files.isRegularFile(this.playerSavePath)) {
            try (JsonReader var0 = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8))) {
                var0.setLenient(false);
                Dynamic<JsonElement> var1 = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(var0));
                int var2 = var1.get("DataVersion").asInt(1343);
                var1 = var1.remove("DataVersion");
                var1 = DataFixTypes.ADVANCEMENTS.updateToCurrentVersion(this.dataFixer, var1, var2);
                Map<ResourceLocation, AdvancementProgress> var3 = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(var1.getValue());
                if (var3 == null) {
                    throw new JsonParseException("Found null for advancements");
                }

                var3.entrySet().stream().sorted(Entry.comparingByValue()).forEach(param1 -> {
                    Advancement var0x = param0.getAdvancement(param1.getKey());
                    if (var0x == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", param1.getKey(), this.playerSavePath);
                    } else {
                        this.startProgress(var0x, param1.getValue());
                        this.progressChanged.add(var0x);
                        this.markForVisibilityUpdate(var0x);
                    }
                });
            } catch (JsonParseException var8) {
                LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, var8);
            } catch (IOException var9) {
                LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, var9);
            }
        }

        this.checkForAutomaticTriggers(param0);
        this.registerListeners(param0);
    }

    public void save() {
        Map<ResourceLocation, AdvancementProgress> var0 = new LinkedHashMap<>();

        for(Entry<Advancement, AdvancementProgress> var1 : this.progress.entrySet()) {
            AdvancementProgress var2 = var1.getValue();
            if (var2.hasProgress()) {
                var0.put(var1.getKey().getId(), var2);
            }
        }

        JsonElement var3 = GSON.toJsonTree(var0);
        var3.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());

            try (Writer var4 = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8)) {
                GSON.toJson(var3, var4);
            }
        } catch (IOException var8) {
            LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, var8);
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
                    && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
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

        if (!var2 && var1.isDone()) {
            this.markForVisibilityUpdate(param0);
        }

        return var0;
    }

    public boolean revoke(Advancement param0, String param1) {
        boolean var0 = false;
        AdvancementProgress var1 = this.getOrStartProgress(param0);
        boolean var2 = var1.isDone();
        if (var1.revokeProgress(param1)) {
            this.registerListeners(param0);
            this.progressChanged.add(param0);
            var0 = true;
        }

        if (var2 && !var1.isDone()) {
            this.markForVisibilityUpdate(param0);
        }

        return var0;
    }

    private void markForVisibilityUpdate(Advancement param0) {
        this.rootsToUpdate.add(param0.getRoot());
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
        if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
            Map<ResourceLocation, AdvancementProgress> var0 = new HashMap<>();
            Set<Advancement> var1 = new HashSet<>();
            Set<ResourceLocation> var2 = new HashSet<>();

            for(Advancement var3 : this.rootsToUpdate) {
                this.updateTreeVisibility(var3, var1, var2);
            }

            this.rootsToUpdate.clear();

            for(Advancement var4 : this.progressChanged) {
                if (this.visible.contains(var4)) {
                    var0.put(var4.getId(), this.progress.get(var4));
                }
            }

            this.progressChanged.clear();
            if (!var0.isEmpty() || !var1.isEmpty() || !var2.isEmpty()) {
                param0.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, var1, var2, var0));
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
        AdvancementProgress var0 = this.progress.get(param0);
        if (var0 == null) {
            var0 = new AdvancementProgress();
            this.startProgress(param0, var0);
        }

        return var0;
    }

    private void startProgress(Advancement param0, AdvancementProgress param1) {
        param1.update(param0.getCriteria(), param0.getRequirements());
        this.progress.put(param0, param1);
    }

    private void updateTreeVisibility(Advancement param0, Set<Advancement> param1, Set<ResourceLocation> param2) {
        AdvancementVisibilityEvaluator.evaluateVisibility(param0, param0x -> this.getOrStartProgress(param0x).isDone(), (param2x, param3) -> {
            if (param3) {
                if (this.visible.add(param2x)) {
                    param1.add(param2x);
                    if (this.progress.containsKey(param2x)) {
                        this.progressChanged.add(param2x);
                    }
                }
            } else if (this.visible.remove(param2x)) {
                param2.add(param2x.getId());
            }

        });
    }
}
