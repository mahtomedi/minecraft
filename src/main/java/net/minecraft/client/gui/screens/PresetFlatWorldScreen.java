package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PresetFlatWorldScreen extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_TEX_SIZE = 128;
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
    public static final Component UNKNOWN_PRESET = Component.translatable("flat_world_preset.unknown");
    private final CreateFlatWorldScreen parent;
    private Component shareText;
    private Component listText;
    private PresetFlatWorldScreen.PresetsList list;
    private Button selectButton;
    EditBox export;
    FlatLevelGeneratorSettings settings;

    public PresetFlatWorldScreen(CreateFlatWorldScreen param0) {
        super(Component.translatable("createWorld.customize.presets.title"));
        this.parent = param0;
    }

    @Nullable
    private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> param0, String param1, int param2) {
        List<String> var0 = Splitter.on('*').limit(2).splitToList(param1);
        int var2;
        String var1;
        if (var0.size() == 2) {
            var1 = var0.get(1);

            try {
                var2 = Math.max(Integer.parseInt(var0.get(0)), 0);
            } catch (NumberFormatException var11) {
                LOGGER.error("Error while parsing flat world string", (Throwable)var11);
                return null;
            }
        } else {
            var1 = var0.get(0);
            var2 = 1;
        }

        int var6 = Math.min(param2 + var2, DimensionType.Y_SIZE);
        int var7 = var6 - param2;

        Optional<Holder.Reference<Block>> var8;
        try {
            var8 = param0.get(ResourceKey.create(Registries.BLOCK, new ResourceLocation(var1)));
        } catch (Exception var101) {
            LOGGER.error("Error while parsing flat world string", (Throwable)var101);
            return null;
        }

        if (var8.isEmpty()) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", var1);
            return null;
        } else {
            return new FlatLayerInfo(var7, var8.get().value());
        }
    }

    private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> param0, String param1) {
        List<FlatLayerInfo> var0 = Lists.newArrayList();
        String[] var1 = param1.split(",");
        int var2 = 0;

        for(String var3 : var1) {
            FlatLayerInfo var4 = getLayerInfoFromString(param0, var3, var2);
            if (var4 == null) {
                return Collections.emptyList();
            }

            var0.add(var4);
            var2 += var4.getHeight();
        }

        return var0;
    }

    public static FlatLevelGeneratorSettings fromString(
        HolderGetter<Block> param0,
        HolderGetter<Biome> param1,
        HolderGetter<StructureSet> param2,
        HolderGetter<PlacedFeature> param3,
        String param4,
        FlatLevelGeneratorSettings param5
    ) {
        Iterator<String> var0 = Splitter.on(';').split(param4).iterator();
        if (!var0.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault(param1, param2, param3);
        } else {
            List<FlatLayerInfo> var1 = getLayersInfoFromString(param0, var0.next());
            if (var1.isEmpty()) {
                return FlatLevelGeneratorSettings.getDefault(param1, param2, param3);
            } else {
                Holder.Reference<Biome> var2 = param1.getOrThrow(DEFAULT_BIOME);
                Holder<Biome> var3 = var2;
                if (var0.hasNext()) {
                    String var4 = var0.next();
                    var3 = Optional.ofNullable(ResourceLocation.tryParse(var4))
                        .map(param0x -> ResourceKey.create(Registries.BIOME, param0x))
                        .flatMap(param1::get)
                        .orElseGet(() -> {
                            LOGGER.warn("Invalid biome: {}", var4);
                            return var2;
                        });
                }

                return param5.withBiomeAndLayers(var1, param5.structureOverrides(), var3);
            }
        }
    }

    static String save(FlatLevelGeneratorSettings param0) {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < param0.getLayersInfo().size(); ++var1) {
            if (var1 > 0) {
                var0.append(",");
            }

            var0.append(param0.getLayersInfo().get(var1));
        }

        var0.append(";");
        var0.append(param0.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> new IllegalStateException("Biome not registered")));
        return var0.toString();
    }

    @Override
    protected void init() {
        this.shareText = Component.translatable("createWorld.customize.presets.share");
        this.listText = Component.translatable("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        WorldCreationContext var0 = this.parent.parent.getUiState().getSettings();
        RegistryAccess var1 = var0.worldgenLoadContext();
        FeatureFlagSet var2 = var0.dataConfiguration().enabledFeatures();
        HolderGetter<Biome> var3 = var1.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> var4 = var1.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> var5 = var1.lookupOrThrow(Registries.PLACED_FEATURE);
        HolderGetter<Block> var6 = var1.lookupOrThrow(Registries.BLOCK).filterFeatures(var2);
        this.export.setValue(save(this.parent.settings()));
        this.settings = this.parent.settings();
        this.addWidget(this.export);
        this.list = new PresetFlatWorldScreen.PresetsList(var1, var2);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), param4 -> {
            FlatLevelGeneratorSettings var0x = fromString(var6, var3, var4, var5, this.export.getValue(), this.settings);
            this.parent.setConfig(var0x);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent))
                .bounds(this.width / 2 + 5, this.height - 28, 150, 20)
                .build()
        );
        this.updateButtonValidity(this.list.getSelected() != null);
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.list.mouseScrolled(param0, param1, param2);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.export.getValue();
        this.init(param0, param1, param2);
        this.export.setValue(var0);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, 400.0F);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        param0.drawString(this.font, this.shareText, 50, 30, 10526880);
        param0.drawString(this.font, this.listText, 50, 70, 10526880);
        param0.pose().popPose();
        this.export.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public void tick() {
        this.export.tick();
        super.tick();
    }

    public void updateButtonValidity(boolean param0) {
        this.selectButton.active = param0 || this.export.getValue().length() > 1;
    }

    @OnlyIn(Dist.CLIENT)
    class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
        public PresetsList(RegistryAccess param0, FeatureFlagSet param1) {
            super(
                PresetFlatWorldScreen.this.minecraft,
                PresetFlatWorldScreen.this.width,
                PresetFlatWorldScreen.this.height,
                80,
                PresetFlatWorldScreen.this.height - 37,
                24
            );

            for(Holder<FlatLevelGeneratorPreset> param2 : param0.registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET)
                .getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
                Set<Block> var0 = param2.value()
                    .settings()
                    .getLayersInfo()
                    .stream()
                    .map(param0x -> param0x.getBlockState().getBlock())
                    .filter(param1x -> !param1x.isEnabled(param1))
                    .collect(Collectors.toSet());
                if (!var0.isEmpty()) {
                    PresetFlatWorldScreen.LOGGER
                        .info(
                            "Discarding flat world preset {} since it contains experimental blocks {}",
                            param2.unwrapKey().map(param0x -> param0x.location().toString()).orElse("<unknown>"),
                            var0
                        );
                } else {
                    this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(param2));
                }
            }

        }

        public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry param0) {
            super.setSelected(param0);
            PresetFlatWorldScreen.this.updateButtonValidity(param0 != null);
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (super.keyPressed(param0, param1, param2)) {
                return true;
            } else {
                if (CommonInputs.selected(param0) && this.getSelected() != null) {
                    this.getSelected().select();
                }

                return false;
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
            private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
            private final FlatLevelGeneratorPreset preset;
            private final Component name;

            public Entry(Holder<FlatLevelGeneratorPreset> param1) {
                this.preset = param1.value();
                this.name = param1.unwrapKey()
                    .map(param0x -> Component.translatable(param0x.location().toLanguageKey("flat_world_preset")))
                    .orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
            }

            @Override
            public void render(
                GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                this.blitSlot(param0, param3, param2, this.preset.displayItem().value());
                param0.drawString(PresetFlatWorldScreen.this.font, this.name, param3 + 18 + 5, param2 + 6, 16777215, false);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    this.select();
                }

                return false;
            }

            void select() {
                PresetsList.this.setSelected(this);
                PresetFlatWorldScreen.this.settings = this.preset.settings();
                PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
                PresetFlatWorldScreen.this.export.moveCursorToStart();
            }

            private void blitSlot(GuiGraphics param0, int param1, int param2, Item param3) {
                this.blitSlotBg(param0, param1 + 1, param2 + 1);
                param0.renderFakeItem(new ItemStack(param3), param1 + 2, param2 + 2);
            }

            private void blitSlotBg(GuiGraphics param0, int param1, int param2) {
                param0.blit(STATS_ICON_LOCATION, param1, param2, 0, 0.0F, 0.0F, 18, 18, 128, 128);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}
