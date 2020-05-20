package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Objects;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
    private final Screen parent;
    private final Consumer<Biome> applySettings;
    private CreateBuffetWorldScreen.BiomeList list;
    private Biome biome;
    private Button doneButton;

    public CreateBuffetWorldScreen(Screen param0, Consumer<Biome> param1, Biome param2) {
        super(new TranslatableComponent("createWorld.customize.buffet.title"));
        this.parent = param0;
        this.applySettings = param1;
        this.biome = param2;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.list = new CreateBuffetWorldScreen.BiomeList();
        this.children.add(this.list);
        this.list.setSelected(this.list.children().stream().filter(param0 -> Objects.equals(param0.biome, this.biome)).findFirst().orElse(null));
        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, param0 -> {
            this.applySettings.accept(this.biome);
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)));
        this.updateButtonValidity();
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.list.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        this.drawCenteredString(param0, this.font, I18n.get("createWorld.customize.buffet.biome"), this.width / 2, 28, 10526880);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
        private BiomeList() {
            super(
                CreateBuffetWorldScreen.this.minecraft,
                CreateBuffetWorldScreen.this.width,
                CreateBuffetWorldScreen.this.height,
                40,
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
                CreateBuffetWorldScreen.this.biome = param0.biome;
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
