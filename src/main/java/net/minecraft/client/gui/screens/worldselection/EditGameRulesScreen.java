package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private List<Component> tooltip;
    private final GameRules gameRules;

    public EditGameRulesScreen(GameRules param0, Consumer<Optional<GameRules>> param1) {
        super(new TranslatableComponent("editGamerule.title"));
        this.gameRules = param0;
        this.exitCallback = param1;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        super.init();
        this.rules = new EditGameRulesScreen.RuleList(this.gameRules);
        this.children.add(this.rules);
        this.addButton(
            new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.exitCallback.accept(Optional.empty()))
        );
        this.doneButton = this.addButton(
            new Button(
                this.width / 2 - 155, this.height - 29, 150, 20, CommonComponents.GUI_DONE, param0 -> this.exitCallback.accept(Optional.of(this.gameRules))
            )
        );
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void onClose() {
        this.exitCallback.accept(Optional.empty());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.tooltip = null;
        this.rules.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
        if (this.tooltip != null) {
            this.renderTooltip(param0, this.tooltip, param1, param2);
        }

    }

    private void setTooltip(@Nullable List<Component> param0) {
        this.tooltip = param0;
    }

    private void updateDoneButton() {
        this.doneButton.active = this.invalidEntries.isEmpty();
    }

    private void markInvalid(EditGameRulesScreen.RuleEntry param0) {
        this.invalidEntries.add(param0);
        this.updateDoneButton();
    }

    private void clearInvalid(EditGameRulesScreen.RuleEntry param0) {
        this.invalidEntries.remove(param0);
        this.updateDoneButton();
    }

    @OnlyIn(Dist.CLIENT)
    public class BooleanRuleEntry extends EditGameRulesScreen.RuleEntry {
        private final Button checkbox;
        private final List<? extends GuiEventListener> children;

        public BooleanRuleEntry(Component param1, List<Component> param2, final String param3, GameRules.BooleanValue param4) {
            super(param2);
            this.checkbox = new Button(10, 5, 220, 20, this.getMessage(param1, param4.get()), param2x -> {
                boolean var0 = !param4.get();
                param4.set(var0, null);
                param2x.setMessage(this.getMessage(param1, var0));
            }) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return this.getMessage().mutableCopy().append("\n").append(param3);
                }
            };
            this.children = ImmutableList.of(this.checkbox);
        }

        private Component getMessage(Component param0, boolean param1) {
            return param0.mutableCopy().append(": ").append(CommonComponents.optionStatus(param1));
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.checkbox.x = param3;
            this.checkbox.y = param2;
            this.checkbox.render(param0, param6, param7, param9);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryRuleEntry extends EditGameRulesScreen.RuleEntry {
        private final Component label;

        public CategoryRuleEntry(Component param1) {
            super(null);
            this.label = param1;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            EditGameRulesScreen.this.drawCenteredString(param0, EditGameRulesScreen.this.minecraft.font, this.label, param3 + param4 / 2, param2 + 5, 16777215);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface EntryFactory<T extends GameRules.Value<T>> {
        EditGameRulesScreen.RuleEntry create(Component var1, List<Component> var2, String var3, T var4);
    }

    @OnlyIn(Dist.CLIENT)
    public class IntegerRuleEntry extends EditGameRulesScreen.RuleEntry {
        private final Component label;
        private final EditBox input;
        private final List<? extends GuiEventListener> children;

        public IntegerRuleEntry(Component param1, List<Component> param2, String param3, GameRules.IntegerValue param4) {
            super(param2);
            this.label = param1;
            this.input = new EditBox(EditGameRulesScreen.this.minecraft.font, 10, 5, 42, 20, param1.mutableCopy().append("\n").append(param3).append("\n"));
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
            this.children = ImmutableList.of(this.input);
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            EditGameRulesScreen.this.minecraft.font.draw(param0, this.label, (float)param3, (float)(param2 + 5), 16777215);
            this.input.x = param3 + param4 - 44;
            this.input.y = param2;
            this.input.render(param0, param6, param7, param9);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract class RuleEntry extends ContainerObjectSelectionList.Entry<EditGameRulesScreen.RuleEntry> {
        @Nullable
        private final List<Component> tooltip;

        public RuleEntry(@Nullable List<Component> param1) {
            this.tooltip = param1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class RuleList extends ContainerObjectSelectionList<EditGameRulesScreen.RuleEntry> {
        public RuleList(final GameRules param1) {
            super(
                EditGameRulesScreen.this.minecraft,
                EditGameRulesScreen.this.width,
                EditGameRulesScreen.this.height,
                43,
                EditGameRulesScreen.this.height - 32,
                24
            );
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
                        Component var0 = new TranslatableComponent(param0.getDescriptionId());
                        Component var1 = new TextComponent(param0.getId()).withStyle(ChatFormatting.YELLOW);
                        T var2 = param1.getRule(param0);
                        String var3 = var2.serialize();
                        Component var4 = new TranslatableComponent("editGamerule.default", new TextComponent(var3)).withStyle(ChatFormatting.GRAY);
                        String var5 = param0.getDescriptionId() + ".description";
                        List<Component> var8;
                        String var9;
                        if (I18n.exists(var5)) {
                            Builder<Component> var6 = ImmutableList.<Component>builder().add(var1);
                            Component var7 = new TranslatableComponent(var5);
                            EditGameRulesScreen.this.font.split(var7, 150).forEach(var6::add);
                            var8 = var6.add(var4).build();
                            var9 = var7.getString() + "\n" + var4.getString();
                        } else {
                            var8 = ImmutableList.of(var1, var4);
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
                                new TranslatableComponent(param0x.getKey().getDescriptionId())
                                    .withStyle(new ChatFormatting[]{ChatFormatting.BOLD, ChatFormatting.YELLOW})
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
        public void render(PoseStack param0, int param1, int param2, float param3) {
            super.render(param0, param1, param2, param3);
            if (this.isMouseOver((double)param1, (double)param2)) {
                EditGameRulesScreen.RuleEntry var0 = this.getEntryAtPosition((double)param1, (double)param2);
                if (var0 != null) {
                    EditGameRulesScreen.this.setTooltip(var0.tooltip);
                }
            }

        }
    }
}
