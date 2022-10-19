package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmExperimentalFeaturesScreen extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
    private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
    private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
    private static final int MARGIN = 20;
    private final BooleanConsumer callback;
    final Collection<Pack> enabledPacks;
    private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;

    public ConfirmExperimentalFeaturesScreen(Collection<Pack> param0, BooleanConsumer param1) {
        super(TITLE);
        this.enabledPacks = param0;
        this.callback = param1;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    private int messageHeight() {
        return this.multilineMessage.getLineCount() * 9;
    }

    private int titleTop() {
        int var0 = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(var0 - 20 - 9, 10, 80);
    }

    @Override
    protected void init() {
        super.init();
        this.multilineMessage = MultiLineLabel.create(this.font, MESSAGE, this.width - 50);
        int var0 = Mth.clamp(this.titleTop() + 20 + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
        this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, var0, 100, 20, CommonComponents.GUI_PROCEED, param0 -> this.callback.accept(true)));
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 50, var0, 100, 20, DETAILS_BUTTON, param0 -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen.DetailsScreen())
            )
        );
        this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, var0, 100, 20, CommonComponents.GUI_BACK, param0 -> this.callback.accept(false)));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, this.titleTop(), 16777215);
        this.multilineMessage.renderCentered(param0, this.width / 2, this.titleTop() + 20);
        super.render(param0, param1, param2, param3);
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
            this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, param0 -> this.onClose()));
            this.packList = new ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList(this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks);
            this.addWidget(this.packList);
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, float param3) {
            this.renderBackground(param0);
            this.packList.render(param0, param1, param2, param3);
            drawCenteredString(param0, this.font, this.title, this.width / 2, 10, 16777215);
            super.render(param0, param1, param2, param3);
        }

        @OnlyIn(Dist.CLIENT)
        class PackList extends ObjectSelectionList<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
            public PackList(Minecraft param0, Collection<Pack> param1) {
                super(param0, DetailsScreen.this.width, DetailsScreen.this.height, 32, DetailsScreen.this.height - 64, (9 + 2) * 3);

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

            @Override
            public boolean isFocused() {
                return DetailsScreen.this.getFocused() == this;
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
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                GuiComponent.drawString(param0, DetailsScreen.this.minecraft.font, this.packId, param3, param2, 16777215);
                this.splitMessage.renderLeftAligned(param0, param3, param2 + 12, 9, 16777215);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
            }
        }
    }
}
