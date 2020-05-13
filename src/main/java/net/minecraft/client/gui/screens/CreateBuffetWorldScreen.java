package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
    private static final WorldGenSettings.BuffetGeneratorType[] TYPES = WorldGenSettings.BuffetGeneratorType.values();
    private final Screen parent;
    private final Consumer<Pair<WorldGenSettings.BuffetGeneratorType, Set<Biome>>> applySettings;
    private CreateBuffetWorldScreen.BiomeList list;
    private int generatorIndex;
    private Button doneButton;

    public CreateBuffetWorldScreen(
        Screen param0, Consumer<Pair<WorldGenSettings.BuffetGeneratorType, Set<Biome>>> param1, Pair<WorldGenSettings.BuffetGeneratorType, Set<Biome>> param2
    ) {
        super(new TranslatableComponent("createWorld.customize.buffet.title"));
        this.parent = param0;
        this.applySettings = param1;

        for(int var0 = 0; var0 < TYPES.length; ++var0) {
            if (TYPES[var0].equals(param2.getFirst())) {
                this.generatorIndex = var0;
                break;
            }
        }

        for(Biome var1 : param2.getSecond()) {
            this.list.setSelected(this.list.children().stream().filter(param1x -> Objects.equals(param1x.biome, var1)).findFirst().orElse(null));
        }

    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button((this.width - 200) / 2, 40, 200, 20, TYPES[this.generatorIndex].createGeneratorString(), param0 -> {
            ++this.generatorIndex;
            if (this.generatorIndex >= TYPES.length) {
                this.generatorIndex = 0;
            }

            param0.setMessage(TYPES[this.generatorIndex].createGeneratorString());
        }));
        this.list = new CreateBuffetWorldScreen.BiomeList();
        this.children.add(this.list);
        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, param0 -> {
            this.applySettings.accept(Pair.of(TYPES[this.generatorIndex], ImmutableSet.of(this.list.getSelected().biome)));
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)));
        this.updateButtonValidity();
    }

    public void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.list.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        this.drawCenteredString(param0, this.font, I18n.get("createWorld.customize.buffet.generator"), this.width / 2, 30, 10526880);
        this.drawCenteredString(param0, this.font, I18n.get("createWorld.customize.buffet.biome"), this.width / 2, 68, 10526880);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
        private BiomeList() {
            super(
                CreateBuffetWorldScreen.this.minecraft,
                CreateBuffetWorldScreen.this.width,
                CreateBuffetWorldScreen.this.height,
                80,
                CreateBuffetWorldScreen.this.height - 37,
                16
            );
            Registry.BIOME
                .stream()
                .sorted(Comparator.comparing(param0 -> param0.getName().getString()))
                .forEach(param0 -> this.addEntry(new CreateBuffetWorldScreen.BiomeList.Entry(param0)));
        }

        @Override
        protected boolean isFocused() {
            return CreateBuffetWorldScreen.this.getFocused() == this;
        }

        public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry param0) {
            super.setSelected(param0);
            if (param0 != null) {
                NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", param0.biome.getName().getString()).getString());
            }

        }

        @Override
        protected void moveSelection(int param0) {
            super.moveSelection(param0);
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
            private final Biome biome;

            public Entry(Biome param0) {
                this.biome = param0;
            }

            @Override
            public void render(
                PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
            ) {
                BiomeList.this.drawString(param0, CreateBuffetWorldScreen.this.font, this.biome.getName().getString(), param3 + 5, param2 + 2, 16777215);
            }

            @Override
            public boolean mouseClicked(double param0, double param1, int param2) {
                if (param2 == 0) {
                    BiomeList.this.setSelected(this);
                    CreateBuffetWorldScreen.this.updateButtonValidity();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
