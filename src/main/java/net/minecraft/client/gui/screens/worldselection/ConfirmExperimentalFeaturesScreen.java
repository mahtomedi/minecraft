package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmExperimentalFeaturesScreen extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
    private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
    private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
    private static final int COLUMN_SPACING = 10;
    private static final int DETAILS_BUTTON_WIDTH = 100;
    private final BooleanConsumer callback;
    final Collection<Pack> enabledPacks;
    private final GridLayout layout = new GridLayout().columnSpacing(10).rowSpacing(20);

    public ConfirmExperimentalFeaturesScreen(Collection<Pack> param0, BooleanConsumer param1) {
        super(TITLE);
        this.enabledPacks = param0;
        this.callback = param1;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout.RowHelper var0 = this.layout.createRowHelper(2);
        LayoutSettings var1 = var0.newCellSettings().alignHorizontallyCenter();
        var0.addChild(new StringWidget(this.title, this.font), 2, var1);
        MultiLineTextWidget var2 = var0.addChild(new MultiLineTextWidget(MESSAGE, this.font).setCentered(true), 2, var1);
        var2.setMaxWidth(310);
        var0.addChild(
            Button.builder(DETAILS_BUTTON, param0 -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen.DetailsScreen())).width(100).build(),
            2,
            var1
        );
        var0.addChild(Button.builder(CommonComponents.GUI_PROCEED, param0 -> this.callback.accept(true)).build());
        var0.addChild(Button.builder(CommonComponents.GUI_BACK, param0 -> this.callback.accept(false)).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.layout.arrangeElements();
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        FrameLayout.alignInRectangle(this.layout, 0, 0, this.width, this.height, 0.5F, 0.5F);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @OnlyIn(Dist.CLIENT)
    class DetailsScreen extends Screen {
        private ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList packList;

        DetailsScreen() {
            super(Component.translatable("selectWorld.experimental.details.title"));
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(ConfirmExperimentalFeaturesScreen.this);
        }

        @Override
        protected void init() {
            super.init();
            this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).bounds(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build()
            );
            this.packList = this.addRenderableWidget(
                new ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList(this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks)
            );
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, float param3) {
            super.render(param0, param1, param2, param3);
            param0.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
        }

        @OnlyIn(Dist.CLIENT)
        class PackList extends ObjectSelectionList<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
            public PackList(Minecraft param0, Collection<Pack> param1) {
                super(param0, DetailsScreen.this.width, DetailsScreen.this.height - 96, 32, (9 + 2) * 3);

                for(Pack param2 : param1) {
                    String var0 = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_SET, param2.getRequestedFeatures());
                    if (!var0.isEmpty()) {
                        Component var1 = ComponentUtils.mergeStyles(param2.getTitle().copy(), Style.EMPTY.withBold(true));
                        Component var2 = Component.translatable("selectWorld.experimental.details.entry", var0);
                        this.addEntry(
                            DetailsScreen.this.new PackListEntry(var1, var2, MultiLineLabel.create(DetailsScreen.this.font, var2, this.getRowWidth()))
                        );
                    }
                }

            }

            @Override
            public int getRowWidth() {
                return this.width * 3 / 4;
            }
        }

        @OnlyIn(Dist.CLIENT)
        class PackListEntry extends ObjectSelectionList.Entry<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
            private final Component packId;
            private final Component message;
            private final MultiLineLabel splitMessage;

            PackListEntry(Component param0, Component param1, MultiLineLabel param2) {
                this.packId = param0;
                this.message = param1;
                this.splitMessage = param2;
            }

            @Override
            public void render(
                GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                param0.drawString(DetailsScreen.this.minecraft.font, this.packId, param3, param2, 16777215);
                this.splitMessage.renderLeftAligned(param0, param3, param2 + 12, 9, 16777215);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
            }
        }
    }
}
