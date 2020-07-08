package net.minecraft.client;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FullscreenResolutionProgressOption extends ProgressOption {
    public FullscreenResolutionProgressOption(Window param0) {
        this(param0, param0.findBestMonitor());
    }

    private FullscreenResolutionProgressOption(Window param0, @Nullable Monitor param1) {
        super(
            "options.fullscreen.resolution",
            -1.0,
            param1 != null ? (double)(param1.getModeCount() - 1) : -1.0,
            1.0F,
            param2 -> {
                if (param1 == null) {
                    return -1.0;
                } else {
                    Optional<VideoMode> var0 = param0.getPreferredFullscreenVideoMode();
                    return var0.<Double>map(param1x -> (double)param1.getVideoModeIndex(param1x)).orElse(-1.0);
                }
            },
            (param2, param3) -> {
                if (param1 != null) {
                    if (param3 == -1.0) {
                        param0.setPreferredFullscreenVideoMode(Optional.empty());
                    } else {
                        param0.setPreferredFullscreenVideoMode(Optional.of(param1.getMode(param3.intValue())));
                    }
    
                }
            },
            (param1x, param2) -> {
                if (param1 == null) {
                    return new TranslatableComponent("options.fullscreen.unavailable");
                } else {
                    double var0 = param2.get(param1x);
                    return var0 == -1.0
                        ? param2.genericValueLabel(new TranslatableComponent("options.fullscreen.current"))
                        : param2.genericValueLabel(new TextComponent(param1.getMode((int)var0).toString()));
                }
            }
        );
    }
}
