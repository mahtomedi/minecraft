package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateFlatWorldScreen extends Screen {
    static final ResourceLocation SLOT_SPRITE = new ResourceLocation("container/slot");
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    protected final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    FlatLevelGeneratorSettings generator;
    private Component columnType;
    private Component columnHeight;
    private CreateFlatWorldScreen.DetailsList list;
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen param0, Consumer<FlatLevelGeneratorSettings> param1, FlatLevelGeneratorSettings param2) {
        super(Component.translatable("createWorld.customize.flat.title"));
        this.parent = param0;
        this.applySettings = param1;
        this.generator = param2;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.generator;
    }

    public void setConfig(FlatLevelGeneratorSettings param0) {
        this.generator = param0;
    }

    @Override
    protected void init() {
        this.columnType = Component.translatable("createWorld.customize.flat.tile");
        this.columnHeight = Component.translatable("createWorld.customize.flat.height");
        this.list = new CreateFlatWorldScreen.DetailsList();
        this.addWidget(this.list);
        this.deleteLayerButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), param0 -> {
            if (this.hasValidSelection()) {
                List<FlatLayerInfo> var0 = this.generator.getLayersInfo();
                int var1 = this.list.children().indexOf(this.list.getSelected());
                int var2 = var0.size() - var1 - 1;
                var0.remove(var2);
                this.list.setSelected(var0.isEmpty() ? null : this.list.children().get(Math.min(var1, var0.size() - 1)));
                this.generator.updateLayers();
                this.list.resetRows();
                this.updateButtonValidity();
            }
        }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets"), param0 -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, param0 -> {
            this.applySettings.accept(this.generator);
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> {
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.generator.updateLayers();
        this.updateButtonValidity();
    }

    void updateButtonValidity() {
        this.deleteLayerButton.active = this.hasValidSelection();
    }

    private boolean hasValidSelection() {
        return this.list.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.list.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        int var0 = this.width / 2 - 92 - 16;
        param0.drawString(this.font, this.columnType, var0, 32, 16777215);
        param0.drawString(this.font, this.columnHeight, var0 + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
    }

    @OnlyIn(Dist.CLIENT)
    class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
        private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");

        public DetailsList() {
            super(
                CreateFlatWorldScreen.this.minecraft,
                CreateFlatWorldScreen.this.width,
                CreateFlatWorldScreen.this.height,
                43,
                CreateFlatWorldScreen.this.height - 60,
                24
            );

            for(int param0 = 0; param0 < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++param0) {
                this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
            }

        }

        public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry param0) {
            super.setSelected(param0);
            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 70;
        }

        public void resetRows() {
            int var0 = this.children().indexOf(this.getSelected());
            this.clearEntries();

            for(int var1 = 0; var1 < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++var1) {
                this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
            }

            List<CreateFlatWorldScreen.DetailsList.Entry> var2 = this.children();
            if (var0 >= 0 && var0 < var2.size()) {
                this.setSelected(var2.get(var0));
            }

        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
            @Override
            public void render(
                GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                FlatLayerInfo var0 = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - param1 - 1);
                BlockState var1 = var0.getBlockState();
                ItemStack var2 = this.getDisplayItem(var1);
                this.blitSlot(param0, param3, param2, var2);
                param0.drawString(CreateFlatWorldScreen.this.font, var2.getHoverName(), param3 + 18 + 5, param2 + 3, 16777215, false);
                Component var3;
                if (param1 == 0) {
                    var3 = Component.translatable("createWorld.customize.flat.layer.top", var0.getHeight());
                } else if (param1 == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
                    var3 = Component.translatable("createWorld.customize.flat.layer.bottom", var0.getHeight());
                } else {
                    var3 = Component.translatable("createWorld.customize.flat.layer", var0.getHeight());
                }

                param0.drawString(
                    CreateFlatWorldScreen.this.font, var3, param3 + 2 + 213 - CreateFlatWorldScreen.this.font.width(var3), param2 + 3, 16777215, false
                );
            }

            private ItemStack getDisplayItem(BlockState param0) {
                Item var0 = param0.getBlock().asItem();
                if (var0 == Items.AIR) {
                    if (param0.is(Blocks.WATER)) {
                        var0 = Items.WATER_BUCKET;
                    } else if (param0.is(Blocks.LAVA)) {
                        var0 = Items.LAVA_BUCKET;
                    }
                }

                return new ItemStack(var0);
            }

            @Override
            public Component getNarration() {
                FlatLayerInfo var0 = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
                ItemStack var1 = this.getDisplayItem(var0.getBlockState());
                return (Component)(!var1.isEmpty() ? Component.translatable("narrator.select", var1.getHoverName()) : CommonComponents.EMPTY);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                DetailsList.this.setSelected(this);
                return true;
            }

            private void blitSlot(GuiGraphics param0, int param1, int param2, ItemStack param3) {
                this.blitSlotBg(param0, param1 + 1, param2 + 1);
                if (!param3.isEmpty()) {
                    param0.renderFakeItem(param3, param1 + 2, param2 + 2);
                }

            }

            private void blitSlotBg(GuiGraphics param0, int param1, int param2) {
                param0.blitSprite(CreateFlatWorldScreen.SLOT_SPRITE, param1, param2, 0, 18, 18);
            }
        }
    }
}
