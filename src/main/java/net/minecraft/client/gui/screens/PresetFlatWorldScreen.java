package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PresetFlatWorldScreen extends Screen {
    private static final List<PresetFlatWorldScreen.PresetInfo> PRESETS = Lists.newArrayList();
    private final CreateFlatWorldScreen parent;
    private String shareText;
    private String listText;
    private PresetFlatWorldScreen.PresetsList list;
    private Button selectButton;
    private EditBox export;

    public PresetFlatWorldScreen(CreateFlatWorldScreen param0) {
        super(new TranslatableComponent("createWorld.customize.presets.title"));
        this.parent = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.shareText = I18n.get("createWorld.customize.presets.share");
        this.listText = I18n.get("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        this.export.setValue(this.parent.saveLayerString());
        this.children.add(this.export);
        this.list = new PresetFlatWorldScreen.PresetsList();
        this.children.add(this.list);
        this.selectButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("createWorld.customize.presets.select"), param0 -> {
                this.parent.loadLayers(this.export.getValue());
                this.minecraft.setScreen(this.parent);
            })
        );
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(this.parent)));
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
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.list.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
        this.drawString(this.font, this.shareText, 50, 30, 10526880);
        this.drawString(this.font, this.listText, 50, 70, 10526880);
        this.export.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }

    @Override
    public void tick() {
        this.export.tick();
        super.tick();
    }

    public void updateButtonValidity(boolean param0) {
        this.selectButton.active = param0 || this.export.getValue().length() > 1;
    }

    private static void preset(String param0, ItemLike param1, Biome param2, List<String> param3, FlatLayerInfo... param4) {
        FlatLevelGeneratorSettings var0 = ChunkGeneratorType.FLAT.createSettings();

        for(int var1 = param4.length - 1; var1 >= 0; --var1) {
            var0.getLayersInfo().add(param4[var1]);
        }

        var0.setBiome(param2);
        var0.updateLayers();

        for(String var2 : param3) {
            var0.getStructuresOptions().put(var2, Maps.newHashMap());
        }

        PRESETS.add(new PresetFlatWorldScreen.PresetInfo(param1.asItem(), param0, var0.toString()));
    }

    static {
        preset(
            I18n.get("createWorld.customize.preset.classic_flat"),
            Blocks.GRASS_BLOCK,
            Biomes.PLAINS,
            Arrays.asList("village"),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(2, Blocks.DIRT),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.tunnelers_dream"),
            Blocks.STONE,
            Biomes.MOUNTAINS,
            Arrays.asList("biome_1", "dungeon", "decoration", "stronghold", "mineshaft"),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(5, Blocks.DIRT),
            new FlatLayerInfo(230, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.water_world"),
            Items.WATER_BUCKET,
            Biomes.DEEP_OCEAN,
            Arrays.asList("biome_1", "oceanmonument"),
            new FlatLayerInfo(90, Blocks.WATER),
            new FlatLayerInfo(5, Blocks.SAND),
            new FlatLayerInfo(5, Blocks.DIRT),
            new FlatLayerInfo(5, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.overworld"),
            Blocks.GRASS,
            Biomes.PLAINS,
            Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake", "pillager_outpost"),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(59, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.snowy_kingdom"),
            Blocks.SNOW,
            Biomes.SNOWY_TUNDRA,
            Arrays.asList("village", "biome_1"),
            new FlatLayerInfo(1, Blocks.SNOW),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(59, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.bottomless_pit"),
            Items.FEATHER,
            Biomes.PLAINS,
            Arrays.asList("village", "biome_1"),
            new FlatLayerInfo(1, Blocks.GRASS_BLOCK),
            new FlatLayerInfo(3, Blocks.DIRT),
            new FlatLayerInfo(2, Blocks.COBBLESTONE)
        );
        preset(
            I18n.get("createWorld.customize.preset.desert"),
            Blocks.SAND,
            Biomes.DESERT,
            Arrays.asList("village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"),
            new FlatLayerInfo(8, Blocks.SAND),
            new FlatLayerInfo(52, Blocks.SANDSTONE),
            new FlatLayerInfo(3, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.redstone_ready"),
            Items.REDSTONE,
            Biomes.DESERT,
            Collections.emptyList(),
            new FlatLayerInfo(52, Blocks.SANDSTONE),
            new FlatLayerInfo(3, Blocks.STONE),
            new FlatLayerInfo(1, Blocks.BEDROCK)
        );
        preset(
            I18n.get("createWorld.customize.preset.the_void"), Blocks.BARRIER, Biomes.THE_VOID, Arrays.asList("decoration"), new FlatLayerInfo(1, Blocks.AIR)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static class PresetInfo {
        public final Item icon;
        public final String name;
        public final String value;

        public PresetInfo(Item param0, String param1, String param2) {
            this.icon = param0;
            this.name = param1;
            this.value = param2;
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
                    .sayNow(new TranslatableComponent("narrator.select", PresetFlatWorldScreen.PRESETS.get(this.children().indexOf(param0)).name).getString());
            }

        }

        @Override
        protected void moveSelection(int param0) {
            super.moveSelection(param0);
            PresetFlatWorldScreen.this.updateButtonValidity(true);
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
            public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
                PresetFlatWorldScreen.PresetInfo var0 = PresetFlatWorldScreen.PRESETS.get(param0);
                this.blitSlot(param2, param1, var0.icon);
                PresetFlatWorldScreen.this.font.draw(var0.name, (float)(param2 + 18 + 5), (float)(param1 + 6), 16777215);
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
                PresetFlatWorldScreen.this.updateButtonValidity(true);
                PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.PRESETS.get(PresetsList.this.children().indexOf(this)).value);
                PresetFlatWorldScreen.this.export.moveCursorToStart();
            }

            private void blitSlot(int param0, int param1, Item param2) {
                this.blitSlotBg(param0 + 1, param1 + 1);
                RenderSystem.enableRescaleNormal();
                Lighting.turnOnGui();
                PresetFlatWorldScreen.this.itemRenderer.renderGuiItem(new ItemStack(param2), param0 + 2, param1 + 2);
                Lighting.turnOff();
                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(int param0, int param1) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                PresetsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(param0, param1, PresetFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }
        }
    }
}
