package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
    protected final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    private FlatLevelGeneratorSettings generator;
    private Component columnType;
    private Component columnHeight;
    private CreateFlatWorldScreen.DetailsList list;
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen param0, Consumer<FlatLevelGeneratorSettings> param1, FlatLevelGeneratorSettings param2) {
        super(new TranslatableComponent("createWorld.customize.flat.title"));
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
        this.columnType = new TranslatableComponent("createWorld.customize.flat.tile");
        this.columnHeight = new TranslatableComponent("createWorld.customize.flat.height");
        this.list = new CreateFlatWorldScreen.DetailsList();
        this.children.add(this.list);
        this.deleteLayerButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 52, 150, 20, new TranslatableComponent("createWorld.customize.flat.removeLayer"), param0 -> {
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
            })
        );
        this.addButton(new Button(this.width / 2 + 5, this.height - 52, 150, 20, new TranslatableComponent("createWorld.customize.presets"), param0 -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, param0 -> {
            this.applySettings.accept(this.generator);
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> {
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
        }));
        this.generator.updateLayers();
        this.updateButtonValidity();
    }

    private void updateButtonValidity() {
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.list.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        int var0 = this.width / 2 - 92 - 16;
        drawString(param0, this.font, this.columnType, var0, 32, 16777215);
        drawString(param0, this.font, this.columnHeight, var0 + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
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
            if (param0 != null) {
                FlatLayerInfo var0 = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - this.children().indexOf(param0) - 1);
                Item var1 = var0.getBlockState().getBlock().asItem();
                if (var1 != Items.AIR) {
                    NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", var1.getName(new ItemStack(var1))).getString());
                }
            }

            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        @Override
        protected boolean isFocused() {
            return CreateFlatWorldScreen.this.getFocused() == this;
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
            private Entry() {
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                FlatLayerInfo var0 = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - param1 - 1);
                BlockState var1 = var0.getBlockState();
                Item var2 = var1.getBlock().asItem();
                if (var2 == Items.AIR) {
                    if (var1.is(Blocks.WATER)) {
                        var2 = Items.WATER_BUCKET;
                    } else if (var1.is(Blocks.LAVA)) {
                        var2 = Items.LAVA_BUCKET;
                    }
                }

                ItemStack var3 = new ItemStack(var2);
                this.blitSlot(param0, param3, param2, var3);
                CreateFlatWorldScreen.this.font.draw(param0, var2.getName(var3), (float)(param3 + 18 + 5), (float)(param2 + 3), 16777215);
                String var4;
                if (param1 == 0) {
                    var4 = I18n.get("createWorld.customize.flat.layer.top", var0.getHeight());
                } else if (param1 == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
                    var4 = I18n.get("createWorld.customize.flat.layer.bottom", var0.getHeight());
                } else {
                    var4 = I18n.get("createWorld.customize.flat.layer", var0.getHeight());
                }

                CreateFlatWorldScreen.this.font
                    .draw(param0, var4, (float)(param3 + 2 + 213 - CreateFlatWorldScreen.this.font.width(var4)), (float)(param2 + 3), 16777215);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    DetailsList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            private void blitSlot(PoseStack param0, int param1, int param2, ItemStack param3) {
                this.blitSlotBg(param0, param1 + 1, param2 + 1);
                RenderSystem.enableRescaleNormal();
                if (!param3.isEmpty()) {
                    CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(param3, param1 + 2, param2 + 2);
                }

                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(PoseStack param0, int param1, int param2) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                DetailsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(param0, param1, param2, CreateFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }
        }
    }
}
