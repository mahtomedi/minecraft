package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldCreationUiState {
    private final List<Consumer<WorldCreationUiState>> listeners = new ArrayList<>();
    private String name = I18n.get("selectWorld.newWorld");
    private boolean nameChanged = true;
    private WorldCreationUiState.SelectedGameMode gameMode = WorldCreationUiState.SelectedGameMode.SURVIVAL;
    private Difficulty difficulty = Difficulty.NORMAL;
    @Nullable
    private Boolean allowCheats;
    private String seed;
    private boolean generateStructures;
    private boolean bonusChest;
    private WorldCreationContext settings;
    private WorldCreationUiState.WorldTypeEntry worldType;
    private final List<WorldCreationUiState.WorldTypeEntry> normalPresetList = new ArrayList();
    private final List<WorldCreationUiState.WorldTypeEntry> altPresetList = new ArrayList();
    private GameRules gameRules = new GameRules();

    public WorldCreationUiState(WorldCreationContext param0, Optional<ResourceKey<WorldPreset>> param1, OptionalLong param2) {
        this.settings = param0;
        this.worldType = new WorldCreationUiState.WorldTypeEntry(findPreset(param0, param1).orElse(null));
        this.updatePresetLists();
        this.seed = param2.isPresent() ? Long.toString(param2.getAsLong()) : "";
        this.generateStructures = param0.options().generateStructures();
        this.bonusChest = param0.options().generateBonusChest();
    }

    public void addListener(Consumer<WorldCreationUiState> param0) {
        this.listeners.add(param0);
    }

    public void onChanged() {
        boolean var0 = this.isBonusChest();
        if (var0 != this.settings.options().generateBonusChest()) {
            this.settings = this.settings.withOptions(param1 -> param1.withBonusChest(var0));
        }

        boolean var1 = this.isGenerateStructures();
        if (var1 != this.settings.options().generateStructures()) {
            this.settings = this.settings.withOptions(param1 -> param1.withStructures(var1));
        }

        for(Consumer<WorldCreationUiState> var2 : this.listeners) {
            var2.accept(this);
        }

        this.nameChanged = false;
    }

    public void setName(String param0) {
        this.name = param0;
        this.nameChanged = true;
        this.onChanged();
    }

    public String getName() {
        return this.name;
    }

    public boolean nameChanged() {
        return this.nameChanged;
    }

    public void setGameMode(WorldCreationUiState.SelectedGameMode param0) {
        this.gameMode = param0;
        this.onChanged();
    }

    public WorldCreationUiState.SelectedGameMode getGameMode() {
        return this.isDebug() ? WorldCreationUiState.SelectedGameMode.DEBUG : this.gameMode;
    }

    public void setDifficulty(Difficulty param0) {
        this.difficulty = param0;
        this.onChanged();
    }

    public Difficulty getDifficulty() {
        return this.isHardcore() ? Difficulty.HARD : this.difficulty;
    }

    public boolean isHardcore() {
        return this.getGameMode() == WorldCreationUiState.SelectedGameMode.HARDCORE;
    }

    public void setAllowCheats(boolean param0) {
        this.allowCheats = param0;
        this.onChanged();
    }

    public boolean isAllowCheats() {
        if (this.isDebug()) {
            return true;
        } else if (this.isHardcore()) {
            return false;
        } else if (this.allowCheats == null) {
            return this.getGameMode() == WorldCreationUiState.SelectedGameMode.CREATIVE;
        } else {
            return this.allowCheats;
        }
    }

    public void setSeed(String param0) {
        this.seed = param0;
        this.settings = this.settings.withOptions(param0x -> param0x.withSeed(WorldOptions.parseSeed(this.getSeed())));
        this.onChanged();
    }

    public String getSeed() {
        return this.seed;
    }

    public void setGenerateStructures(boolean param0) {
        this.generateStructures = param0;
        this.onChanged();
    }

    public boolean isGenerateStructures() {
        return this.isDebug() ? false : this.generateStructures;
    }

    public void setBonusChest(boolean param0) {
        this.bonusChest = param0;
        this.onChanged();
    }

    public boolean isBonusChest() {
        return !this.isDebug() && !this.isHardcore() ? this.bonusChest : false;
    }

    public void setSettings(WorldCreationContext param0) {
        this.settings = param0;
        this.updatePresetLists();
        this.onChanged();
    }

    public WorldCreationContext getSettings() {
        return this.settings;
    }

    public void updateDimensions(WorldCreationContext.DimensionsUpdater param0) {
        this.settings = this.settings.withDimensions(param0);
        this.onChanged();
    }

    protected boolean tryUpdateDataConfiguration(WorldDataConfiguration param0) {
        WorldDataConfiguration var0 = this.settings.dataConfiguration();
        if (var0.dataPacks().getEnabled().equals(param0.dataPacks().getEnabled()) && var0.enabledFeatures().equals(param0.enabledFeatures())) {
            this.settings = new WorldCreationContext(
                this.settings.options(),
                this.settings.datapackDimensions(),
                this.settings.selectedDimensions(),
                this.settings.worldgenRegistries(),
                this.settings.dataPackResources(),
                param0
            );
            return true;
        } else {
            return false;
        }
    }

    public boolean isDebug() {
        return this.settings.selectedDimensions().isDebug();
    }

    public void setWorldType(WorldCreationUiState.WorldTypeEntry param0) {
        this.worldType = param0;
        Holder<WorldPreset> var0 = param0.preset();
        if (var0 != null) {
            this.updateDimensions((param1, param2) -> var0.value().createWorldDimensions());
        }

    }

    public WorldCreationUiState.WorldTypeEntry getWorldType() {
        return this.worldType;
    }

    @Nullable
    public PresetEditor getPresetEditor() {
        Holder<WorldPreset> var0 = this.getWorldType().preset();
        return var0 != null ? PresetEditor.EDITORS.get(var0.unwrapKey()) : null;
    }

    public List<WorldCreationUiState.WorldTypeEntry> getNormalPresetList() {
        return this.normalPresetList;
    }

    public List<WorldCreationUiState.WorldTypeEntry> getAltPresetList() {
        return this.altPresetList;
    }

    private void updatePresetLists() {
        Registry<WorldPreset> var0 = this.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET);
        this.normalPresetList.clear();
        this.normalPresetList
            .addAll(getNonEmptyList(var0, WorldPresetTags.NORMAL).orElseGet(() -> var0.holders().map(WorldCreationUiState.WorldTypeEntry::new).toList()));
        this.altPresetList.clear();
        this.altPresetList.addAll(getNonEmptyList(var0, WorldPresetTags.EXTENDED).orElse(this.normalPresetList));
        Holder<WorldPreset> var1 = this.worldType.preset();
        if (var1 != null) {
            this.setWorldType(
                (WorldCreationUiState.WorldTypeEntry)findPreset(this.getSettings(), var1.unwrapKey())
                    .map(WorldCreationUiState.WorldTypeEntry::new)
                    .orElse((WorldCreationUiState.WorldTypeEntry)this.normalPresetList.get(0))
            );
        }

    }

    private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext param0, Optional<ResourceKey<WorldPreset>> param1) {
        return param1.flatMap(param1x -> param0.worldgenLoadContext().<WorldPreset>registryOrThrow(Registries.WORLD_PRESET).getHolder(param1x));
    }

    private static Optional<List<WorldCreationUiState.WorldTypeEntry>> getNonEmptyList(Registry<WorldPreset> param0, TagKey<WorldPreset> param1) {
        return param0.getTag(param1)
            .map(param0x -> param0x.stream().map(WorldCreationUiState.WorldTypeEntry::new).toList())
            .filter(param0x -> !param0x.isEmpty());
    }

    public void setGameRules(GameRules param0) {
        this.gameRules = param0;
        this.onChanged();
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SelectedGameMode {
        SURVIVAL("survival", GameType.SURVIVAL),
        HARDCORE("hardcore", GameType.SURVIVAL),
        CREATIVE("creative", GameType.CREATIVE),
        DEBUG("spectator", GameType.SPECTATOR);

        public final GameType gameType;
        public final Component displayName;
        private final Component info;

        private SelectedGameMode(String param0, GameType param1) {
            this.gameType = param1;
            this.displayName = Component.translatable("selectWorld.gameMode." + param0);
            this.info = Component.translatable("selectWorld.gameMode." + param0 + ".info");
        }

        public Component getInfo() {
            return this.info;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record WorldTypeEntry(@Nullable Holder<WorldPreset> preset) {
        private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");

        public Component describePreset() {
            return Optional.ofNullable(this.preset)
                .flatMap(Holder::unwrapKey)
                .map(param0 -> Component.translatable(param0.location().toLanguageKey("generator")))
                .orElse(CUSTOM_WORLD_DESCRIPTION);
        }

        public boolean isAmplified() {
            return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).filter(param0 -> param0.equals(WorldPresets.AMPLIFIED)).isPresent();
        }
    }
}
