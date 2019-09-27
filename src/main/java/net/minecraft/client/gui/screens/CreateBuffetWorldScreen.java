package net.minecraft.client.gui.screens;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
    private static final List<ResourceLocation> GENERATORS = Registry.CHUNK_GENERATOR_TYPE
        .keySet()
        .stream()
        .filter(param0 -> Registry.CHUNK_GENERATOR_TYPE.get(param0).isPublic())
        .collect(Collectors.toList());
    private final CreateWorldScreen parent;
    private final CompoundTag optionsTag;
    private CreateBuffetWorldScreen.BiomeList list;
    private int generatorIndex;
    private Button doneButton;

    public CreateBuffetWorldScreen(CreateWorldScreen param0, CompoundTag param1) {
        super(new TranslatableComponent("createWorld.customize.buffet.title"));
        this.parent = param0;
        this.optionsTag = param1;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(
            new Button(
                (this.width - 200) / 2,
                40,
                200,
                20,
                I18n.get("createWorld.customize.buffet.generatortype")
                    + " "
                    + I18n.get(Util.makeDescriptionId("generator", GENERATORS.get(this.generatorIndex))),
                param0 -> {
                    ++this.generatorIndex;
                    if (this.generatorIndex >= GENERATORS.size()) {
                        this.generatorIndex = 0;
                    }
        
                    param0.setMessage(
                        I18n.get("createWorld.customize.buffet.generatortype")
                            + " "
                            + I18n.get(Util.makeDescriptionId("generator", GENERATORS.get(this.generatorIndex)))
                    );
                }
            )
        );
        this.list = new CreateBuffetWorldScreen.BiomeList();
        this.children.add(this.list);
        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done"), param0 -> {
            this.parent.levelTypeOptions = this.saveOptions();
            this.minecraft.setScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(this.parent)));
        this.loadOptions();
        this.updateButtonValidity();
    }

    private void loadOptions() {
        if (this.optionsTag.contains("chunk_generator", 10) && this.optionsTag.getCompound("chunk_generator").contains("type", 8)) {
            ResourceLocation var0 = new ResourceLocation(this.optionsTag.getCompound("chunk_generator").getString("type"));

            for(int var1 = 0; var1 < GENERATORS.size(); ++var1) {
                if (GENERATORS.get(var1).equals(var0)) {
                    this.generatorIndex = var1;
                    break;
                }
            }
        }

        if (this.optionsTag.contains("biome_source", 10) && this.optionsTag.getCompound("biome_source").contains("biomes", 9)) {
            ListTag var2 = this.optionsTag.getCompound("biome_source").getList("biomes", 8);

            for(int var3 = 0; var3 < var2.size(); ++var3) {
                ResourceLocation var4 = new ResourceLocation(var2.getString(var3));
                this.list.setSelected(this.list.children().stream().filter(param1 -> Objects.equals(param1.key, var4)).findFirst().orElse(null));
            }
        }

        this.optionsTag.remove("chunk_generator");
        this.optionsTag.remove("biome_source");
    }

    private CompoundTag saveOptions() {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();
        var1.putString("type", Registry.BIOME_SOURCE_TYPE.getKey(BiomeSourceType.FIXED).toString());
        CompoundTag var2 = new CompoundTag();
        ListTag var3 = new ListTag();
        var3.add(StringTag.valueOf(this.list.getSelected().key.toString()));
        var2.put("biomes", var3);
        var1.put("options", var2);
        CompoundTag var4 = new CompoundTag();
        CompoundTag var5 = new CompoundTag();
        var4.putString("type", GENERATORS.get(this.generatorIndex).toString());
        var5.putString("default_block", "minecraft:stone");
        var5.putString("default_fluid", "minecraft:water");
        var4.put("options", var5);
        var0.put("biome_source", var1);
        var0.put("chunk_generator", var4);
        return var0;
    }

    public void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderDirtBackground(0);
        this.list.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
        this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.generator"), this.width / 2, 30, 10526880);
        this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.biome"), this.width / 2, 68, 10526880);
        super.render(param0, param1, param2);
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
                .keySet()
                .stream()
                .sorted(Comparator.comparing(param0 -> Registry.BIOME.get(param0).getName().getString()))
                .forEach(param0 -> this.addEntry(new CreateBuffetWorldScreen.BiomeList.Entry(param0)));
        }

        @Override
        protected boolean isFocused() {
            return CreateBuffetWorldScreen.this.getFocused() == this;
        }

        public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry param0) {
            super.setSelected(param0);
            if (param0 != null) {
                NarratorChatListener.INSTANCE
                    .sayNow(new TranslatableComponent("narrator.select", Registry.BIOME.get(param0.key).getName().getString()).getString());
            }

        }

        @Override
        protected void moveSelection(int param0) {
            super.moveSelection(param0);
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
            private final ResourceLocation key;

            public Entry(ResourceLocation param0) {
                this.key = param0;
            }

            @Override
            public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
                BiomeList.this.drawString(
                    CreateBuffetWorldScreen.this.font, Registry.BIOME.get(this.key).getName().getString(), param2 + 5, param1 + 2, 16777215
                );
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
