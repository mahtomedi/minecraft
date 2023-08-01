package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperimentsScreen extends Screen {
    private static final int MAIN_CONTENT_WIDTH = 310;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final PackRepository packRepository;
    private final Consumer<PackRepository> output;
    private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<>();

    protected ExperimentsScreen(Screen param0, PackRepository param1, Consumer<PackRepository> param2) {
        super(Component.translatable("experiments_screen.title"));
        this.parent = param0;
        this.packRepository = param1;
        this.output = param2;

        for(Pack var0 : param1.getAvailablePacks()) {
            if (var0.getPackSource() == PackSource.FEATURE) {
                this.packs.put(var0, param1.getSelectedPacks().contains(var0));
            }
        }

    }

    @Override
    protected void init() {
        this.layout.addToHeader(new StringWidget(Component.translatable("selectWorld.experiments"), this.font));
        LinearLayout var0 = this.layout.addToContents(LinearLayout.vertical());
        var0.addChild(
            new MultiLineTextWidget(Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED), this.font).setMaxWidth(310),
            param0 -> param0.paddingBottom(15)
        );
        SwitchGrid.Builder var1 = SwitchGrid.builder(310).withInfoUnderneath(2, true).withRowSpacing(4);
        this.packs
            .forEach(
                (param1, param2) -> var1.addSwitch(
                            getHumanReadableTitle(param1), () -> this.packs.getBoolean(param1), param1x -> this.packs.put(param1, param1x.booleanValue())
                        )
                        .withInfo(param1.getDescription())
            );
        var1.build(var0::addChild);
        GridLayout.RowHelper var2 = this.layout.addToFooter(new GridLayout().columnSpacing(10)).createRowHelper(2);
        var2.addChild(Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).build());
        var2.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onClose()).build());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    private static Component getHumanReadableTitle(Pack param0) {
        String var0 = "dataPack." + param0.getId() + ".name";
        return (Component)(I18n.exists(var0) ? Component.translatable(var0) : param0.getTitle());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onDone() {
        List<Pack> var0 = new ArrayList<>(this.packRepository.getSelectedPacks());
        List<Pack> var1 = new ArrayList<>();
        this.packs.forEach((param2, param3) -> {
            var0.remove(param2);
            if (param3) {
                var1.add(param2);
            }

        });
        var0.addAll(Lists.reverse(var1));
        this.packRepository.setSelected(var0.stream().map(Pack::getId).toList());
        this.output.accept(this.packRepository);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        super.renderBackground(param0, param1, param2, param3);
        param0.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        int var0 = 32;
        param0.blit(
            BACKGROUND_LOCATION,
            0,
            this.layout.getHeaderHeight(),
            0.0F,
            0.0F,
            this.width,
            this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight(),
            32,
            32
        );
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
