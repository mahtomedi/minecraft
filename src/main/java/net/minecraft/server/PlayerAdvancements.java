package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
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
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PlayerList playerList;
    private final Path playerSavePath;
    private AdvancementTree tree;
    private final Map<AdvancementHolder, AdvancementProgress> progress = new LinkedHashMap<>();
    private final Set<AdvancementHolder> visible = new HashSet<>();
    private final Set<AdvancementHolder> progressChanged = new HashSet<>();
    private final Set<AdvancementNode> rootsToUpdate = new HashSet<>();
    private ServerPlayer player;
    @Nullable
    private AdvancementHolder lastSelectedTab;
    private boolean isFirstPacket = true;
    private final Codec<PlayerAdvancements.Data> codec;

    public PlayerAdvancements(DataFixer param0, PlayerList param1, ServerAdvancementManager param2, Path param3, ServerPlayer param4) {
        this.playerList = param1;
        this.playerSavePath = param3;
        this.player = param4;
        this.tree = param2.tree();
        int var0 = 1343;
        this.codec = DataFixTypes.ADVANCEMENTS.wrapCodec(PlayerAdvancements.Data.CODEC, param0, 1343);
        this.load(param2);
    }

    public void setPlayer(ServerPlayer param0) {
        this.player = param0;
    }

    public void stopListening() {
        for(CriterionTrigger<?> var0 : BuiltInRegistries.TRIGGER_TYPES) {
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
        this.tree = param0.tree();
        this.load(param0);
    }

    private void registerListeners(ServerAdvancementManager param0) {
        for(AdvancementHolder var0 : param0.getAllAdvancements()) {
            this.registerListeners(var0);
        }

    }

    private void checkForAutomaticTriggers(ServerAdvancementManager param0) {
        for(AdvancementHolder var0 : param0.getAllAdvancements()) {
            Advancement var1 = var0.value();
            if (var1.criteria().isEmpty()) {
                this.award(var0, "");
                var1.rewards().grant(this.player);
            }
        }

    }

    private void load(ServerAdvancementManager param0) {
        if (Files.isRegularFile(this.playerSavePath)) {
            try (JsonReader var0 = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8))) {
                var0.setLenient(false);
                JsonElement var1 = Streams.parse(var0);
                PlayerAdvancements.Data var2 = Util.getOrThrow(this.codec.parse(JsonOps.INSTANCE, var1), JsonParseException::new);
                this.applyFrom(param0, var2);
            } catch (JsonParseException var7) {
                LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, var7);
            } catch (IOException var8) {
                LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, var8);
            }
        }

        this.checkForAutomaticTriggers(param0);
        this.registerListeners(param0);
    }

    public void save() {
        JsonElement var0 = Util.getOrThrow(this.codec.encodeStart(JsonOps.INSTANCE, this.asData()), IllegalStateException::new);

        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());

            try (Writer var1 = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8)) {
                GSON.toJson(var0, var1);
            }
        } catch (IOException var7) {
            LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, var7);
        }

    }

    private void applyFrom(ServerAdvancementManager param0, PlayerAdvancements.Data param1) {
        param1.forEach((param1x, param2) -> {
            AdvancementHolder var0 = param0.get(param1x);
            if (var0 == null) {
                LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", param1x, this.playerSavePath);
            } else {
                this.startProgress(var0, param2);
                this.progressChanged.add(var0);
                this.markForVisibilityUpdate(var0);
            }
        });
    }

    private PlayerAdvancements.Data asData() {
        Map<ResourceLocation, AdvancementProgress> var0 = new LinkedHashMap<>();
        this.progress.forEach((param1, param2) -> {
            if (param2.hasProgress()) {
                var0.put(param1.id(), param2);
            }

        });
        return new PlayerAdvancements.Data(var0);
    }

    public boolean award(AdvancementHolder param0, String param1) {
        boolean var0 = false;
        AdvancementProgress var1 = this.getOrStartProgress(param0);
        boolean var2 = var1.isDone();
        if (var1.grantProgress(param1)) {
            this.unregisterListeners(param0);
            this.progressChanged.add(param0);
            var0 = true;
            if (!var2 && var1.isDone()) {
                param0.value().rewards().grant(this.player);
                param0.value().display().ifPresent(param1x -> {
                    if (param1x.shouldAnnounceChat() && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
                        this.playerList.broadcastSystemMessage(param1x.getType().createAnnouncement(param0, this.player), false);
                    }

                });
            }
        }

        if (!var2 && var1.isDone()) {
            this.markForVisibilityUpdate(param0);
        }

        return var0;
    }

    public boolean revoke(AdvancementHolder param0, String param1) {
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

    private void markForVisibilityUpdate(AdvancementHolder param0) {
        AdvancementNode var0 = this.tree.get(param0);
        if (var0 != null) {
            this.rootsToUpdate.add(var0.root());
        }

    }

    private void registerListeners(AdvancementHolder param0) {
        AdvancementProgress var0 = this.getOrStartProgress(param0);
        if (!var0.isDone()) {
            for(Entry<String, Criterion<?>> var1 : param0.value().criteria().entrySet()) {
                CriterionProgress var2 = var0.getCriterion(var1.getKey());
                if (var2 != null && !var2.isDone()) {
                    this.registerListener(param0, var1.getKey(), var1.getValue());
                }
            }

        }
    }

    private <T extends CriterionTriggerInstance> void registerListener(AdvancementHolder param0, String param1, Criterion<T> param2) {
        param2.trigger().addPlayerListener(this, new CriterionTrigger.Listener<>(param2.triggerInstance(), param0, param1));
    }

    private void unregisterListeners(AdvancementHolder param0) {
        AdvancementProgress var0 = this.getOrStartProgress(param0);

        for(Entry<String, Criterion<?>> var1 : param0.value().criteria().entrySet()) {
            CriterionProgress var2 = var0.getCriterion(var1.getKey());
            if (var2 != null && (var2.isDone() || var0.isDone())) {
                this.removeListener(param0, var1.getKey(), var1.getValue());
            }
        }

    }

    private <T extends CriterionTriggerInstance> void removeListener(AdvancementHolder param0, String param1, Criterion<T> param2) {
        param2.trigger().removePlayerListener(this, new CriterionTrigger.Listener<>(param2.triggerInstance(), param0, param1));
    }

    public void flushDirty(ServerPlayer param0) {
        if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
            Map<ResourceLocation, AdvancementProgress> var0 = new HashMap<>();
            Set<AdvancementHolder> var1 = new HashSet<>();
            Set<ResourceLocation> var2 = new HashSet<>();

            for(AdvancementNode var3 : this.rootsToUpdate) {
                this.updateTreeVisibility(var3, var1, var2);
            }

            this.rootsToUpdate.clear();

            for(AdvancementHolder var4 : this.progressChanged) {
                if (this.visible.contains(var4)) {
                    var0.put(var4.id(), this.progress.get(var4));
                }
            }

            this.progressChanged.clear();
            if (!var0.isEmpty() || !var1.isEmpty() || !var2.isEmpty()) {
                param0.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, var1, var2, var0));
            }
        }

        this.isFirstPacket = false;
    }

    public void setSelectedTab(@Nullable AdvancementHolder param0) {
        AdvancementHolder var0 = this.lastSelectedTab;
        if (param0 != null && param0.value().isRoot() && param0.value().display().isPresent()) {
            this.lastSelectedTab = param0;
        } else {
            this.lastSelectedTab = null;
        }

        if (var0 != this.lastSelectedTab) {
            this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.id()));
        }

    }

    public AdvancementProgress getOrStartProgress(AdvancementHolder param0) {
        AdvancementProgress var0 = this.progress.get(param0);
        if (var0 == null) {
            var0 = new AdvancementProgress();
            this.startProgress(param0, var0);
        }

        return var0;
    }

    private void startProgress(AdvancementHolder param0, AdvancementProgress param1) {
        param1.update(param0.value().requirements());
        this.progress.put(param0, param1);
    }

    private void updateTreeVisibility(AdvancementNode param0, Set<AdvancementHolder> param1, Set<ResourceLocation> param2) {
        AdvancementVisibilityEvaluator.evaluateVisibility(param0, param0x -> this.getOrStartProgress(param0x.holder()).isDone(), (param2x, param3) -> {
            AdvancementHolder var0 = param2x.holder();
            if (param3) {
                if (this.visible.add(var0)) {
                    param1.add(var0);
                    if (this.progress.containsKey(var0)) {
                        this.progressChanged.add(var0);
                    }
                }
            } else if (this.visible.remove(var0)) {
                param2.add(var0.id());
            }

        });
    }

    static record Data(Map<ResourceLocation, AdvancementProgress> map) {
        public static final Codec<PlayerAdvancements.Data> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, AdvancementProgress.CODEC)
            .xmap(PlayerAdvancements.Data::new, PlayerAdvancements.Data::map);

        public void forEach(BiConsumer<ResourceLocation, AdvancementProgress> param0) {
            this.map.entrySet().stream().sorted(Entry.comparingByValue()).forEach(param1 -> param0.accept(param1.getKey(), param1.getValue()));
        }
    }
}
