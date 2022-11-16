package net.minecraft.client.gui.screens;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.Lists;

@OnlyIn(Dist.CLIENT)
public class OnlineOptionsScreen extends SimpleOptionsSubScreen {
    @Nullable
    private final OptionInstance<Unit> difficultyDisplay;

    public static OnlineOptionsScreen createOnlineOptionsScreen(Minecraft param0, Screen param1, Options param2) {
        List<OptionInstance<?>> var0 = Lists.newArrayList();
        var0.add(param2.realmsNotifications());
        var0.add(param2.allowServerListing());
        OptionInstance<Unit> var1 = Util.mapNullable(
            param0.level,
            param0x -> {
                Difficulty var0x = param0x.getDifficulty();
                return new OptionInstance<>(
                    "options.difficulty.online",
                    OptionInstance.noTooltip(),
                    (param1x, param2x) -> var0x.getDisplayName(),
                    new OptionInstance.Enum<>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()),
                    Unit.INSTANCE,
                    param0xx -> {
                    }
                );
            }
        );
        if (var1 != null) {
            var0.add(var1);
        }

        return new OnlineOptionsScreen(param1, param2, var0.toArray(new OptionInstance[0]), var1);
    }

    private OnlineOptionsScreen(Screen param0, Options param1, OptionInstance<?>[] param2, @Nullable OptionInstance<Unit> param3) {
        super(param0, param1, Component.translatable("options.online.title"), param2);
        this.difficultyDisplay = param3;
    }

    @Override
    protected void init() {
        super.init();
        if (this.difficultyDisplay != null) {
            AbstractWidget var0 = this.list.findOption(this.difficultyDisplay);
            if (var0 != null) {
                var0.active = false;
            }
        }

        AbstractWidget var1 = this.list.findOption(this.options.telemetryOptInExtra());
        if (var1 != null) {
            var1.active = this.minecraft.extraTelemetryAvailable();
        }

    }
}
