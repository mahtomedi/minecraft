package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PresetFlatWorldScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<PresetFlatWorldScreen.PresetInfo> PRESETS = Lists.newArrayList();
    private final CreateFlatWorldScreen parent;
    private Component shareText;
    private Component listText;
    private PresetFlatWorldScreen.PresetsList list;
    private Button selectButton;
    private EditBox export;
    private FlatLevelGeneratorSettings settings;

    public PresetFlatWorldScreen(CreateFlatWorldScreen param0) {
        super(new TranslatableComponent("createWorld.customize.presets.title"));
        this.parent = param0;
    }

    @Nullable
    private static FlatLayerInfo getLayerInfoFromString(String param0, int param1) {
        String[] var0 = param0.split("\\*", 2);
        int var1;
        if (var0.length == 2) {
            try {
                var1 = Math.max(Integer.parseInt(var0[0]), 0);
            } catch (NumberFormatException var101) {
                LOGGER.error("Error while parsing flat world string => {}", var101.getMessage());
                return null;
            }
        } else {
            var1 = 1;
        }

        int var4 = Math.min(param1 + var1, 256);
        int var5 = var4 - param1;
        String var6 = var0[var0.length - 1];

        Block var7;
        try {
            var7 = Registry.BLOCK.getOptional(new ResourceLocation(var6)).orElse(null);
        } catch (Exception var91) {
            LOGGER.error("Error while parsing flat world string => {}", var91.getMessage());
            return null;
        }

        if (var7 == null) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", var6);
            return null;
        } else {
            FlatLayerInfo var10 = new FlatLayerInfo(var5, var7);
            var10.setStart(param1);
            return var10;
        }
    }

    private static List<FlatLayerInfo> getLayersInfoFromString(String param0) {
        List<FlatLayerInfo> var0 = Lists.newArrayList();
        String[] var1 = param0.split(",");
        int var2 = 0;

        for(String var3 : var1) {
            FlatLayerInfo var4 = getLayerInfoFromString(var3, var2);
            if (var4 == null) {
                return Collections.emptyList();
            }

            var0.add(var4);
            var2 += var4.getHeight();
        }

        return var0;
    }

    public static FlatLevelGeneratorSettings fromString(Registry<Biome> param0, String param1, FlatLevelGeneratorSettings param2) {
        Iterator<String> var0 = Splitter.on(';').split(param1).iterator();
        if (!var0.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault();
        } else {
            List<FlatLayerInfo> var1 = getLayersInfoFromString(var0.next());
            if (var1.isEmpty()) {
                return FlatLevelGeneratorSettings.getDefault();
            } else {
                FlatLevelGeneratorSettings var2 = param2.withLayers(var1, param2.structureSettings());
                Biome var3 = param0.getOrThrow(Biomes.PLAINS);
                if (var0.hasNext()) {
                    try {
                        ResourceLocation var4 = new ResourceLocation(var0.next());
                        var3 = param0.getOptional(var4).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + var4));
                    } catch (Exception var8) {
                        LOGGER.error("Error while parsing flat world string => {}", var8.getMessage());
                    }
                }

                var2.setBiome(var3);
                return var2;
            }
        }
    }

    private static String save(Registry<Biome> param0, FlatLevelGeneratorSettings param1) {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < param1.getLayersInfo().size(); ++var1) {
            if (var1 > 0) {
                var0.append(",");
            }

            var0.append(param1.getLayersInfo().get(var1));
        }

        var0.append(";");
        var0.append(param0.getKey(param1.getBiome()));
        return var0.toString();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.shareText = new TranslatableComponent("createWorld.customize.presets.share");
        this.listText = new TranslatableComponent("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        Registry<Biome> var0 = this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
        this.export.setValue(save(var0, this.parent.settings()));
        this.settings = this.parent.settings();
        this.children.add(this.export);
        this.list = new PresetFlatWorldScreen.PresetsList();
        this.children.add(this.list);
        this.selectButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("createWorld.customize.presets.select"), param1 -> {
                FlatLevelGeneratorSettings var0x = fromString(var0, this.export.getValue(), this.settings);
                this.parent.setConfig(var0x);
                this.minecraft.setScreen(this.parent);
            })
        );
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)));
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
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.0F, 400.0F);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        drawString(param0, this.font, this.shareText, 50, 30, 10526880);
        drawString(param0, this.font, this.listText, 50, 70, 10526880);
        RenderSystem.popMatrix();
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

    private static void preset(
        Component param0,
        ItemLike param1,
        ResourceKey<Biome> param2,
        List<StructureFeature<?>> param3,
        boolean param4,
        boolean param5,
        boolean param6,
        FlatLayerInfo... param7
    ) {
        PRESETS.add(new PresetFlatWorldScreen.PresetInfo(param1.asItem(), param0, param6x -> {
            Map<StructureFeature<?>, StructureFeatureConfiguration> var0x = Maps.newHashMap();

            for(StructureFeature<?> var3x : param3) {
                var0x.put(var3x, StructureSettings.DEFAULTS.get(var3x));
            }

            StructureSettings var2 = new StructureSettings(param4 ? Optional.of(StructureSettings.DEFAULT_STRONGHOLD) : Optional.empty(), var0x);
            FlatLevelGeneratorSettings var3 = new FlatLevelGeneratorSettings(var2);
            if (param5) {
                var3.setDecoration();
            }

            if (param6) {
                var3.setAddLakes();
            }

            for(int var4x = param7.length - 1; var4x >= 0; --var4x) {
                var3.getLayersInfo().add(param7[var4x]);
            }

            var3.setBiome(param6x.getOrThrow(param2));
            var3.updateLayers();
            return var3.withStructureSettings(var2);
        }));
    }

    static {
        preset(
            new TranslatableComponent("createWorld.customize.preset.classic_flat"),
            Blocks.GRASS_BLOCK,
            Biomes.PLAINS,
            Arrays.asList(StructureFeature.VILLAGE),
            false,
            false,
            false,
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(2, Blocks.DIRT),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.tunnelers_dream"),
            Blocks.STONE,
            Biomes.MOUNTAINS,
            Arrays.asList(StructureFeature.MINESHAFT),
            true,
            true,
            false,
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(5, Blocks.DIRT),
            new FlatLayerInfo(230, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.water_world"),
            Items.WATER_BUCKET,
            Biomes.DEEP_OCEAN,
            Arrays.asList(StructureFeature.OCEAN_RUIN, StructureFeature.SHIPWRECK, StructureFeature.OCEAN_MONUMENT),
            false,
            false,
            false,
            new FlatLayerInfo(90, Blocks.WATER),
            new FlatLayerInfo(5, Blocks.SAND),
            new FlatLayerInfo(5, Blocks.DIRT),
            new FlatLayerInfo(5, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.overworld"),
            Blocks.GRASS,
            Biomes.PLAINS,
            Arrays.asList(StructureFeature.VILLAGE, StructureFeature.MINESHAFT, StructureFeature.PILLAGER_OUTPOST, StructureFeature.RUINED_PORTAL),
            true,
            true,
            true,
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(59, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.snowy_kingdom"),
            Blocks.SNOW,
            Biomes.SNOWY_TUNDRA,
            Arrays.asList(StructureFeature.VILLAGE, StructureFeature.IGLOO),
            false,
            false,
            false,
            new FlatLayerInfo(1, Blocks.SNOW),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(59, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.bottomless_pit"),
            Items.FEATHER,
            Biomes.PLAINS,
            Arrays.asList(StructureFeature.VILLAGE),
            false,
            false,
            false,
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(2, Blocks.COBBLESTONE)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.desert"),
            Blocks.SAND,
            Biomes.DESERT,
            Arrays.asList(StructureFeature.VILLAGE, StructureFeature.DESERT_PYRAMID, StructureFeature.MINESHAFT),
            true,
            true,
            false,
            new FlatLayerInfo(8, Blocks.SAND),
            new FlatLayerInfo(52, Blocks.SANDSTONE),
            new FlatLayerInfo(3, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.redstone_ready"),
            Items.REDSTONE,
            Biomes.DESERT,
            Collections.emptyList(),
            false,
            false,
            false,
            new FlatLayerInfo(52, Blocks.SANDSTONE),
            new FlatLayerInfo(3, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.the_void"),
            Blocks.BARRIER,
            Biomes.THE_VOID,
            Collections.emptyList(),
            false,
            true,
            false,
            new FlatLayerInfo(1, Blocks.AIR)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static class PresetInfo {
        public final Item icon;
        public final Component name;
        public final Function<Registry<Biome>, FlatLevelGeneratorSettings> settings;

        public PresetInfo(Item param0, Component param1, Function<Registry<Biome>, FlatLevelGeneratorSettings> param2) {
            this.icon = param0;
            this.name = param1;
            this.settings = param2;
        }

        public Component getName() {
            return this.name;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
        public PresetsList() {
            super(
                PresetFlatWorldScreen.this.minecraft,
                PresetFlatWorldScreen.this.width,
                PresetFlatWorldScreen.this.height,
                80,
                PresetFlatWorldScreen.this.height - 37,
                24
            );

            for(int param0 = 0; param0 < PresetFlatWorldScreen.PRESETS.size(); ++param0) {
                this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry());
            }

        }

        public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry param0) {
            super.setSelected(param0);
            if (param0 != null) {
                NarratorChatListener.INSTANCE
                    .sayNow(
                        new TranslatableComponent("narrator.select", PresetFlatWorldScreen.PRESETS.get(this.children().indexOf(param0)).getName()).getString()
                    );
            }

            PresetFlatWorldScreen.this.updateButtonValidity(param0 != null);
        }

        @Override
        protected boolean isFocused() {
            return PresetFlatWorldScreen.this.getFocused() == this;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (super.keyPressed(param0, param1, param2)) {
                return true;
            } else {
                if ((param0 == 257 || param0 == 335) && this.getSelected() != null) {
                    this.getSelected().select();
                }

                return false;
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                PresetFlatWorldScreen.PresetInfo var0 = PresetFlatWorldScreen.PRESETS.get(param1);
                this.blitSlot(param0, param3, param2, var0.icon);
                PresetFlatWorldScreen.this.font.draw(param0, var0.name, (float)(param3 + 18 + 5), (float)(param2 + 6), 16777215);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    this.select();
                }

                return false;
            }

            private void select() {
                PresetsList.this.setSelected(this);
                PresetFlatWorldScreen.PresetInfo var0 = PresetFlatWorldScreen.PRESETS.get(PresetsList.this.children().indexOf(this));
                Registry<Biome> var1 = PresetFlatWorldScreen.this.parent
                    .parent
                    .worldGenSettingsComponent
                    .registryHolder()
                    .registryOrThrow(Registry.BIOME_REGISTRY);
                PresetFlatWorldScreen.this.settings = var0.settings.apply(var1);
                PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(var1, PresetFlatWorldScreen.this.settings));
                PresetFlatWorldScreen.this.export.moveCursorToStart();
            }

            private void blitSlot(PoseStack param0, int param1, int param2, Item param3) {
                this.blitSlotBg(param0, param1 + 1, param2 + 1);
                RenderSystem.enableRescaleNormal();
                PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(new ItemStack(param3), param1 + 2, param2 + 2);
                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(PoseStack param0, int param1, int param2) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                PresetsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(param0, param1, param2, PresetFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }
        }
    }
}
