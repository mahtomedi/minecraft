package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateFlatWorldScreen extends Screen {
    private final CreateWorldScreen parent;
    private FlatLevelGeneratorSettings generator = FlatLevelGeneratorSettings.getDefault();
    private String columnType;
    private String columnHeight;
    private CreateFlatWorldScreen.DetailsList list;
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen param0, CompoundTag param1) {
        super(new TranslatableComponent("createWorld.customize.flat.title"));
        this.parent = param0;
        this.loadLayers(param1);
    }

    public String saveLayerString() {
        return this.generator.toString();
    }

    public CompoundTag saveLayers() {
        return (CompoundTag)this.generator.toObject(NbtOps.INSTANCE).getValue();
    }

    public void loadLayers(String param0) {
        this.generator = FlatLevelGeneratorSettings.fromString(param0);
    }

    public void loadLayers(CompoundTag param0) {
        this.generator = FlatLevelGeneratorSettings.fromObject(new Dynamic<>(NbtOps.INSTANCE, param0));
    }

    @Override
    protected void init() {
        this.columnType = I18n.get("createWorld.customize.flat.tile");
        this.columnHeight = I18n.get("createWorld.customize.flat.height");
        this.list = new CreateFlatWorldScreen.DetailsList();
        this.children.add(this.list);
        this.deleteLayerButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 52, 150, 20, I18n.get("createWorld.customize.flat.removeLayer"), param0 -> {
                if (this.hasValidSelection()) {
                    List<FlatLayerInfo> var0 = this.generator.getLayersInfo();
                    int var1 = this.list.children().indexOf(this.list.getSelected());
                    int var2 = var0.size() - var1 - 1;
                    var0.remove(var2);
                    this.list.setSelected(var0.isEmpty() ? null : this.list.children().get(Math.min(var1, var0.size() - 1)));
                    this.generator.updateLayers();
                    this.updateButtonValidity();
                }
            })
        );
        this.addButton(new Button(this.width / 2 + 5, this.height - 52, 150, 20, I18n.get("createWorld.customize.presets"), param0 -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done"), param0 -> {
            this.parent.levelTypeOptions = this.saveLayers();
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), param0 -> {
            this.minecraft.setScreen(this.parent);
            this.generator.updateLayers();
            this.updateButtonValidity();
        }));
        this.generator.updateLayers();
        this.updateButtonValidity();
    }

    public void updateButtonValidity() {
        this.deleteLayerButton.active = this.hasValidSelection();
        this.list.resetRows();
    }

    private boolean hasValidSelection() {
        return this.list.getSelected() != null;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.list.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
        int var0 = this.width / 2 - 92 - 16;
        this.drawString(this.font, this.columnType, var0, 32, 16777215);
        this.drawString(this.font, this.columnHeight, var0 + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
        super.render(param0, param1, param2);
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

        }

        @Override
        protected void moveSelection(int param0) {
            super.moveSelection(param0);
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
            public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
                FlatLayerInfo var0 = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - param0 - 1);
                BlockState var1 = var0.getBlockState();
                Block var2 = var1.getBlock();
                Item var3 = var2.asItem();
                if (var3 == Items.AIR) {
                    if (var2 == Blocks.WATER) {
                        var3 = Items.WATER_BUCKET;
                    } else if (var2 == Blocks.LAVA) {
                        var3 = Items.LAVA_BUCKET;
                    }
                }

                ItemStack var4 = new ItemStack(var3);
                String var5 = var3.getName(var4).getColoredString();
                this.blitSlot(param2, param1, var4);
                CreateFlatWorldScreen.this.font.draw(var5, (float)(param2 + 18 + 5), (float)(param1 + 3), 16777215);
                String var6;
                if (param0 == 0) {
                    var6 = I18n.get("createWorld.customize.flat.layer.top", var0.getHeight());
                } else if (param0 == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
                    var6 = I18n.get("createWorld.customize.flat.layer.bottom", var0.getHeight());
                } else {
                    var6 = I18n.get("createWorld.customize.flat.layer", var0.getHeight());
                }

                CreateFlatWorldScreen.this.font
                    .draw(var6, (float)(param2 + 2 + 213 - CreateFlatWorldScreen.this.font.width(var6)), (float)(param1 + 3), 16777215);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    DetailsList.this.setSelected(this);
                    CreateFlatWorldScreen.this.updateButtonValidity();
                    return true;
                } else {
                    return false;
                }
            }

            private void blitSlot(int param0, int param1, ItemStack param2) {
                this.blitSlotBg(param0 + 1, param1 + 1);
                RenderSystem.enableRescaleNormal();
                if (!param2.isEmpty()) {
                    CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(param2, param0 + 2, param1 + 2);
                }

                RenderSystem.disableRescaleNormal();
            }

            private void blitSlotBg(int param0, int param1) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                DetailsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(param0, param1, CreateFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
            }
        }
    }
}
