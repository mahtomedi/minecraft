package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PresetFlatWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_TEX_SIZE = 128;
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    static final List<PresetFlatWorldScreen.PresetInfo> PRESETS = Lists.newArrayList();
    private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
    final CreateFlatWorldScreen parent;
    private Component shareText;
    private Component listText;
    private PresetFlatWorldScreen.PresetsList list;
    private Button selectButton;
    EditBox export;
    FlatLevelGeneratorSettings settings;

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
            } catch (NumberFormatException var10) {
                LOGGER.error("Error while parsing flat world string => {}", var10.getMessage());
                return null;
            }
        } else {
            var1 = 1;
        }

        int var4 = Math.min(param1 + var1, DimensionType.Y_SIZE);
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
            return new FlatLayerInfo(var5, var7);
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
            return FlatLevelGeneratorSettings.getDefault(param0);
        } else {
            List<FlatLayerInfo> var1 = getLayersInfoFromString(var0.next());
            if (var1.isEmpty()) {
                return FlatLevelGeneratorSettings.getDefault(param0);
            } else {
                FlatLevelGeneratorSettings var2 = param2.withLayers(var1, param2.structureOverrides());
                ResourceKey<Biome> var3 = DEFAULT_BIOME;
                if (var0.hasNext()) {
                    try {
                        ResourceLocation var4 = new ResourceLocation(var0.next());
                        var3 = ResourceKey.create(Registry.BIOME_REGISTRY, var4);
                        param0.getOptional(var3).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + var4));
                    } catch (Exception var8) {
                        LOGGER.error("Error while parsing flat world string => {}", var8.getMessage());
                        var3 = DEFAULT_BIOME;
                    }
                }

                var2.setBiome(param0.getOrCreateHolder(var3));
                return var2;
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.shareText = new TranslatableComponent("createWorld.customize.presets.share");
        this.listText = new TranslatableComponent("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        Registry<Biome> var0 = this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
        this.export.setValue(save(this.parent.settings()));
        this.settings = this.parent.settings();
        this.addWidget(this.export);
        this.list = new PresetFlatWorldScreen.PresetsList();
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(
            new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("createWorld.customize.presets.select"), param1 -> {
                FlatLevelGeneratorSettings var0x = fromString(var0, this.export.getValue(), this.settings);
                this.parent.setConfig(var0x);
                this.minecraft.setScreen(this.parent);
            })
        );
        this.addRenderableWidget(
            new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent))
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
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        param0.pushPose();
        param0.translate(0.0, 0.0, 400.0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        drawString(param0, this.font, this.shareText, 50, 30, 10526880);
        drawString(param0, this.font, this.listText, 50, 70, 10526880);
        param0.popPose();
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
        Set<ResourceKey<StructureSet>> param3,
        boolean param4,
        boolean param5,
        FlatLayerInfo... param6
    ) {
        PRESETS.add(
            new PresetFlatWorldScreen.PresetInfo(
                param1.asItem(),
                param0,
                param5x -> {
                    Registry<Biome> var0x = param5x.registryOrThrow(Registry.BIOME_REGISTRY);
                    Registry<StructureSet> var1x = param5x.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
                    HolderSet.Direct<StructureSet> var2x = HolderSet.direct(
                        param3.stream().flatMap(param1x -> var1x.getHolder(param1x).stream()).collect(Collectors.toList())
                    );
                    FlatLevelGeneratorSettings var3x = new FlatLevelGeneratorSettings(Optional.of(var2x), var0x);
                    if (param4) {
                        var3x.setDecoration();
                    }
        
                    if (param5) {
                        var3x.setAddLakes();
                    }
        
                    for(int var4x = param6.length - 1; var4x >= 0; --var4x) {
                        var3x.getLayersInfo().add(param6[var4x]);
                    }
        
                    var3x.setBiome(var0x.getOrCreateHolder(param2));
                    var3x.updateLayers();
                    return var3x;
                }
            )
        );
    }

    static {
        preset(
            new TranslatableComponent("createWorld.customize.preset.classic_flat"),
            Blocks.GRASS_BLOCK,
            Biomes.PLAINS,
            Set.of(BuiltinStructureSets.VILLAGES),
            false,
            false,
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(2, Blocks.DIRT),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.tunnelers_dream"),
            Blocks.STONE,
            Biomes.WINDSWEPT_HILLS,
            Set.of(BuiltinStructureSets.MINESHAFTS, BuiltinStructureSets.STRONGHOLDS),
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
            Set.of(BuiltinStructureSets.OCEAN_RUINS, BuiltinStructureSets.SHIPWRECKS, BuiltinStructureSets.OCEAN_MONUMENTS),
            false,
            false,
            new FlatLayerInfo(90, Blocks.WATER),
            new FlatLayerInfo(5, Blocks.GRAVEL),
            new FlatLayerInfo(5, Blocks.DIRT),
            new FlatLayerInfo(5, Blocks.STONE),
            new FlatLayerInfo(64, Blocks.DEEPSLATE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.overworld"),
            Blocks.GRASS,
            Biomes.PLAINS,
            Set.of(
                BuiltinStructureSets.VILLAGES,
                BuiltinStructureSets.MINESHAFTS,
                BuiltinStructureSets.PILLAGER_OUTPOSTS,
                BuiltinStructureSets.RUINED_PORTALS,
                BuiltinStructureSets.STRONGHOLDS
            ),
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
            Biomes.SNOWY_PLAINS,
            Set.of(BuiltinStructureSets.VILLAGES, BuiltinStructureSets.IGLOOS),
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
            Set.of(BuiltinStructureSets.VILLAGES),
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
            Set.of(BuiltinStructureSets.VILLAGES, BuiltinStructureSets.DESERT_PYRAMIDS, BuiltinStructureSets.MINESHAFTS, BuiltinStructureSets.STRONGHOLDS),
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
            Set.of(),
            false,
            false,
            new FlatLayerInfo(116, Blocks.SANDSTONE),
            new FlatLayerInfo(3, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            new TranslatableComponent("createWorld.customize.preset.the_void"),
            Blocks.BARRIER,
            Biomes.THE_VOID,
            Set.of(),
            true,
            false,
            new FlatLayerInfo(1, Blocks.AIR)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static class PresetInfo {
        public final Item icon;
        public final Component name;
        public final Function<RegistryAccess, FlatLevelGeneratorSettings> settings;

        public PresetInfo(Item param0, Component param1, Function<RegistryAccess, FlatLevelGeneratorSettings> param2) {
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

            for(PresetFlatWorldScreen.PresetInfo param0 : PresetFlatWorldScreen.PRESETS) {
                this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(param0));
            }

        }

        public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry param0) {
            super.setSelected(param0);
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
            private final PresetFlatWorldScreen.PresetInfo preset;

            public Entry(PresetFlatWorldScreen.PresetInfo param1) {
                this.preset = param1;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                this.blitSlot(param0, param3, param2, this.preset.icon);
                PresetFlatWorldScreen.this.font.draw(param0, this.preset.name, (float)(param3 + 18 + 5), (float)(param2 + 6), 16777215);
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
                PresetFlatWorldScreen.this.settings = this.preset
                    .settings
                    .apply(PresetFlatWorldScreen.this.parent.parent.worldGenSettingsComponent.registryHolder());
                PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
                PresetFlatWorldScreen.this.export.moveCursorToStart();
            }

            private void blitSlot(PoseStack param0, int param1, int param2, Item param3) {
                this.blitSlotBg(param0, param1 + 1, param2 + 1);
                PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(new ItemStack(param3), param1 + 2, param2 + 2);
            }

            private void blitSlotBg(PoseStack param0, int param1, int param2) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(param0, param1, param2, PresetFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }

            @Override
            public Component getNarration() {
                return new TranslatableComponent("narrator.select", this.preset.getName());
            }
        }
    }
}
