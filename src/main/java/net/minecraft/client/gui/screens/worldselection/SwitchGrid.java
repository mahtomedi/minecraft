package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class SwitchGrid {
    private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
    private final List<SwitchGrid.LabeledSwitch> switches;

    SwitchGrid(List<SwitchGrid.LabeledSwitch> param0) {
        this.switches = param0;
    }

    public void refreshStates() {
        this.switches.forEach(SwitchGrid.LabeledSwitch::refreshState);
    }

    public static SwitchGrid.Builder builder(int param0) {
        return new SwitchGrid.Builder(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        final int width;
        private final List<SwitchGrid.SwitchBuilder> switchBuilders = new ArrayList<>();
        int paddingLeft;
        int rowSpacing = 4;
        int rowCount;
        Optional<SwitchGrid.InfoUnderneathSettings> infoUnderneath = Optional.empty();

        public Builder(int param0) {
            this.width = param0;
        }

        void increaseRow() {
            ++this.rowCount;
        }

        public SwitchGrid.SwitchBuilder addSwitch(Component param0, BooleanSupplier param1, Consumer<Boolean> param2) {
            SwitchGrid.SwitchBuilder var0 = new SwitchGrid.SwitchBuilder(param0, param1, param2, 44);
            this.switchBuilders.add(var0);
            return var0;
        }

        public SwitchGrid.Builder withPaddingLeft(int param0) {
            this.paddingLeft = param0;
            return this;
        }

        public SwitchGrid.Builder withRowSpacing(int param0) {
            this.rowSpacing = param0;
            return this;
        }

        public SwitchGrid build(Consumer<LayoutElement> param0) {
            GridLayout var0 = new GridLayout().rowSpacing(this.rowSpacing);
            var0.addChild(SpacerElement.width(this.width - 44), 0, 0);
            var0.addChild(SpacerElement.width(44), 0, 1);
            List<SwitchGrid.LabeledSwitch> var1 = new ArrayList<>();
            this.rowCount = 0;

            for(SwitchGrid.SwitchBuilder var2 : this.switchBuilders) {
                var1.add(var2.build(this, var0, 0));
            }

            var0.arrangeElements();
            param0.accept(var0);
            SwitchGrid var3 = new SwitchGrid(var1);
            var3.refreshStates();
            return var3;
        }

        public SwitchGrid.Builder withInfoUnderneath(int param0, boolean param1) {
            this.infoUnderneath = Optional.of(new SwitchGrid.InfoUnderneathSettings(param0, param1));
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record InfoUnderneathSettings(int maxInfoRows, boolean alwaysMaxHeight) {
    }

    @OnlyIn(Dist.CLIENT)
    static record LabeledSwitch(CycleButton<Boolean> button, BooleanSupplier stateSupplier, @Nullable BooleanSupplier isActiveCondition) {
        public void refreshState() {
            this.button.setValue(this.stateSupplier.getAsBoolean());
            if (this.isActiveCondition != null) {
                this.button.active = this.isActiveCondition.getAsBoolean();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SwitchBuilder {
        private final Component label;
        private final BooleanSupplier stateSupplier;
        private final Consumer<Boolean> onClicked;
        @Nullable
        private Component info;
        @Nullable
        private BooleanSupplier isActiveCondition;
        private final int buttonWidth;

        SwitchBuilder(Component param0, BooleanSupplier param1, Consumer<Boolean> param2, int param3) {
            this.label = param0;
            this.stateSupplier = param1;
            this.onClicked = param2;
            this.buttonWidth = param3;
        }

        public SwitchGrid.SwitchBuilder withIsActiveCondition(BooleanSupplier param0) {
            this.isActiveCondition = param0;
            return this;
        }

        public SwitchGrid.SwitchBuilder withInfo(Component param0) {
            this.info = param0;
            return this;
        }

        SwitchGrid.LabeledSwitch build(SwitchGrid.Builder param0, GridLayout param1, int param2) {
            param0.increaseRow();
            StringWidget var0 = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
            param1.addChild(var0, param0.rowCount, param2, param1.newCellSettings().align(0.0F, 0.5F).paddingLeft(param0.paddingLeft));
            Optional<SwitchGrid.InfoUnderneathSettings> var1 = param0.infoUnderneath;
            CycleButton.Builder<Boolean> var2 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
            var2.displayOnlyValue();
            boolean var3 = this.info != null && var1.isEmpty();
            if (var3) {
                Tooltip var4 = Tooltip.create(this.info);
                var2.withTooltip(param1x -> var4);
            }

            if (this.info != null && !var3) {
                var2.withCustomNarration(param0x -> CommonComponents.joinForNarration(this.label, param0x.createDefaultNarrationMessage(), this.info));
            } else {
                var2.withCustomNarration(param0x -> CommonComponents.joinForNarration(this.label, param0x.createDefaultNarrationMessage()));
            }

            CycleButton<Boolean> var5 = var2.create(0, 0, this.buttonWidth, 20, Component.empty(), (param0x, param1x) -> this.onClicked.accept(param1x));
            if (this.isActiveCondition != null) {
                var5.active = this.isActiveCondition.getAsBoolean();
            }

            param1.addChild(var5, param0.rowCount, param2 + 1, param1.newCellSettings().alignHorizontallyRight());
            if (this.info != null) {
                var1.ifPresent(param3 -> {
                    Component var0x = this.info.copy().withStyle(ChatFormatting.GRAY);
                    Font var1x = Minecraft.getInstance().font;
                    MultiLineTextWidget var2x = new MultiLineTextWidget(var0x, var1x);
                    var2x.setMaxWidth(param0.width - param0.paddingLeft - this.buttonWidth);
                    var2x.setMaxRows(param3.maxInfoRows());
                    param0.increaseRow();
                    int var3x = param3.alwaysMaxHeight ? 9 * param3.maxInfoRows - var2x.getHeight() : 0;
                    param1.addChild(var2x, param0.rowCount, param2, param1.newCellSettings().paddingTop(-param0.rowSpacing).paddingBottom(var3x));
                });
            }

            return new SwitchGrid.LabeledSwitch(var5, this.stateSupplier, this.isActiveCondition);
        }
    }
}
