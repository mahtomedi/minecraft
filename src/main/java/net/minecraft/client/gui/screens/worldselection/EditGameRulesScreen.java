package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditGameRulesScreen extends Screen {
    private final Consumer<Optional<GameRules>> exitCallback;
    private EditGameRulesScreen.RuleList rules;
    private final Set<EditGameRulesScreen.RuleEntry> invalidEntries = Sets.newHashSet();
    private Button doneButton;
    @Nullable
    private List<FormattedCharSequence> tooltip;
    private final GameRules gameRules;

    public EditGameRulesScreen(GameRules param0, Consumer<Optional<GameRules>> param1) {
        super(Component.translatable("editGamerule.title"));
        this.gameRules = param0;
        this.exitCallback = param1;
    }

    @Override
    protected void init() {
        this.rules = this.addRenderableWidget(new EditGameRulesScreen.RuleList(this.gameRules));
        GridLayout.RowHelper var0 = new GridLayout().columnSpacing(10).createRowHelper(2);
        this.doneButton = var0.addChild(Button.builder(CommonComponents.GUI_DONE, param0 -> this.exitCallback.accept(Optional.of(this.gameRules))).build());
        var0.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.exitCallback.accept(Optional.empty())).build());
        var0.getGrid().visitWidgets(param1 -> {
        });
        var0.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
        var0.getGrid().arrangeElements();
    }

    @Override
    public void onClose() {
        this.exitCallback.accept(Optional.empty());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.tooltip = null;
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
    }

    private void updateDoneButton() {
        this.doneButton.active = this.invalidEntries.isEmpty();
    }

    void markInvalid(EditGameRulesScreen.RuleEntry param0) {
        this.invalidEntries.add(param0);
        this.updateDoneButton();
    }

    void clearInvalid(EditGameRulesScreen.RuleEntry param0) {
        this.invalidEntries.remove(param0);
        this.updateDoneButton();
    }

    @OnlyIn(Dist.CLIENT)
    public class BooleanRuleEntry extends EditGameRulesScreen.GameRuleEntry {
        private final CycleButton<Boolean> checkbox;

        public BooleanRuleEntry(Component param1, List<FormattedCharSequence> param2, String param3, GameRules.BooleanValue param4) {
            super(param2, param1);
            this.checkbox = CycleButton.onOffBuilder(param4.get())
                .displayOnlyValue()
                .withCustomNarration(param1x -> param1x.createDefaultNarrationMessage().append("\n").append(param3))
                .create(10, 5, 44, 20, param1, (param1x, param2x) -> param4.set(param2x, null));
            this.children.add(this.checkbox);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderLabel(param0, param2, param3);
            this.checkbox.setX(param3 + param4 - 45);
            this.checkbox.setY(param2);
            this.checkbox.render(param0, param6, param7, param9);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryRuleEntry extends EditGameRulesScreen.RuleEntry {
        final Component label;

        public CategoryRuleEntry(Component param1) {
            super(null);
            this.label = param1;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.drawCenteredString(EditGameRulesScreen.this.minecraft.font, this.label, param3 + param4 / 2, param2 + 5, 16777215);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput param0) {
                    param0.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
                }
            });
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface EntryFactory<T extends GameRules.Value<T>> {
        EditGameRulesScreen.RuleEntry create(Component var1, List<FormattedCharSequence> var2, String var3, T var4);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract class GameRuleEntry extends EditGameRulesScreen.RuleEntry {
        private final List<FormattedCharSequence> label;
        protected final List<AbstractWidget> children = Lists.newArrayList();

        public GameRuleEntry(List<FormattedCharSequence> param1, Component param2) {
            super(param1);
            this.label = EditGameRulesScreen.this.minecraft.font.split(param2, 175);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        protected void renderLabel(GuiGraphics param0, int param1, int param2) {
            if (this.label.size() == 1) {
                param0.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), param2, param1 + 5, 16777215, false);
            } else if (this.label.size() >= 2) {
                param0.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), param2, param1, 16777215, false);
                param0.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(1), param2, param1 + 10, 16777215, false);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public class IntegerRuleEntry extends EditGameRulesScreen.GameRuleEntry {
        private final EditBox input;

        public IntegerRuleEntry(Component param1, List<FormattedCharSequence> param2, String param3, GameRules.IntegerValue param4) {
            super(param2, param1);
            this.input = new EditBox(EditGameRulesScreen.this.minecraft.font, 10, 5, 44, 20, param1.copy().append("\n").append(param3).append("\n"));
            this.input.setValue(Integer.toString(param4.get()));
            this.input.setResponder(param1x -> {
                if (param4.tryDeserialize(param1x)) {
                    this.input.setTextColor(14737632);
                    EditGameRulesScreen.this.clearInvalid(this);
                } else {
                    this.input.setTextColor(16711680);
                    EditGameRulesScreen.this.markInvalid(this);
                }

            });
            this.children.add(this.input);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderLabel(param0, param2, param3);
            this.input.setX(param3 + param4 - 45);
            this.input.setY(param2);
            this.input.render(param0, param6, param7, param9);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class RuleEntry extends ContainerObjectSelectionList.Entry<EditGameRulesScreen.RuleEntry> {
        @Nullable
        final List<FormattedCharSequence> tooltip;

        public RuleEntry(@Nullable List<FormattedCharSequence> param0) {
            this.tooltip = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class RuleList extends ContainerObjectSelectionList<EditGameRulesScreen.RuleEntry> {
        public RuleList(final GameRules param1) {
            super(EditGameRulesScreen.this.minecraft, EditGameRulesScreen.this.width, EditGameRulesScreen.this.height - 75, 43, 24);
            final Map<GameRules.Category, Map<GameRules.Key<?>, EditGameRulesScreen.RuleEntry>> var0 = Maps.newHashMap();
            GameRules.visitGameRuleTypes(
                new GameRules.GameRuleTypeVisitor() {
                    @Override
                    public void visitBoolean(GameRules.Key<GameRules.BooleanValue> param0, GameRules.Type<GameRules.BooleanValue> param1x) {
                        this.addEntry(
                            param0, (param0x, param1xxx, param2, param3) -> EditGameRulesScreen.this.new BooleanRuleEntry(param0x, param1xxx, param2, param3)
                        );
                    }
    
                    @Override
                    public void visitInteger(GameRules.Key<GameRules.IntegerValue> param0, GameRules.Type<GameRules.IntegerValue> param1x) {
                        this.addEntry(
                            param0, (param0x, param1xxx, param2, param3) -> EditGameRulesScreen.this.new IntegerRuleEntry(param0x, param1xxx, param2, param3)
                        );
                    }
    
                    private <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> param0, EditGameRulesScreen.EntryFactory<T> param1x) {
                        Component var0 = Component.translatable(param0.getDescriptionId());
                        Component var1 = Component.literal(param0.getId()).withStyle(ChatFormatting.YELLOW);
                        T var2 = param1.getRule(param0);
                        String var3 = var2.serialize();
                        Component var4 = Component.translatable("editGamerule.default", Component.literal(var3)).withStyle(ChatFormatting.GRAY);
                        String var5 = param0.getDescriptionId() + ".description";
                        List<FormattedCharSequence> var8;
                        String var9;
                        if (I18n.exists(var5)) {
                            Builder<FormattedCharSequence> var6 = ImmutableList.<FormattedCharSequence>builder().add(var1.getVisualOrderText());
                            Component var7 = Component.translatable(var5);
                            EditGameRulesScreen.this.font.split(var7, 150).forEach(var6::add);
                            var8 = var6.add(var4.getVisualOrderText()).build();
                            var9 = var7.getString() + "\n" + var4.getString();
                        } else {
                            var8 = ImmutableList.of(var1.getVisualOrderText(), var4.getVisualOrderText());
                            var9 = var4.getString();
                        }
    
                        var0.computeIfAbsent(param0.getCategory(), param0x -> Maps.newHashMap()).put(param0, param1.create(var0, var8, var9, var2));
                    }
                }
            );
            var0.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(
                    param0x -> {
                        this.addEntry(
                            EditGameRulesScreen.this.new CategoryRuleEntry(
                                Component.translatable(param0x.getKey().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)
                            )
                        );
                        param0x.getValue()
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRules.Key::getId)))
                            .forEach(param0xx -> this.addEntry(param0xx.getValue()));
                    }
                );
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            super.renderWidget(param0, param1, param2, param3);
            EditGameRulesScreen.RuleEntry var0 = this.getHovered();
            if (var0 != null && var0.tooltip != null) {
                EditGameRulesScreen.this.setTooltipForNextRenderPass(var0.tooltip);
            }

        }
    }
}
