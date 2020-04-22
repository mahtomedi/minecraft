package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Screenshot {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    public static void grab(File param0, int param1, int param2, RenderTarget param3, Consumer<Component> param4) {
        grab(param0, null, param1, param2, param3, param4);
    }

    public static void grab(File param0, @Nullable String param1, int param2, int param3, RenderTarget param4, Consumer<Component> param5) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _grab(param0, param1, param2, param3, param4, param5));
        } else {
            _grab(param0, param1, param2, param3, param4, param5);
        }

    }

    private static void _grab(File param0, @Nullable String param1, int param2, int param3, RenderTarget param4, Consumer<Component> param5) {
        NativeImage var0 = takeScreenshot(param2, param3, param4);
        File var1 = new File(param0, "screenshots");
        var1.mkdir();
        File var2;
        if (param1 == null) {
            var2 = getFile(var1);
        } else {
            var2 = new File(var1, param1);
        }

        SimpleResource.IO_EXECUTOR
            .execute(
                () -> {
                    try {
                        var0.writeToFile(var2);
                        Component var0x = new TextComponent(var2.getName())
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var2.getAbsolutePath())));
                        param5.accept(new TranslatableComponent("screenshot.success", var0x));
                    } catch (Exception var7x) {
                        LOGGER.warn("Couldn't save screenshot", (Throwable)var7x);
                        param5.accept(new TranslatableComponent("screenshot.failure", var7x.getMessage()));
                    } finally {
                        var0.close();
                    }
        
                }
            );
    }

    public static NativeImage takeScreenshot(int param0, int param1, RenderTarget param2) {
        param0 = param2.width;
        param1 = param2.height;
        NativeImage var0 = new NativeImage(param0, param1, false);
        RenderSystem.bindTexture(param2.colorTextureId);
        var0.downloadTexture(0, true);
        var0.flipY();
        return var0;
    }

    private static File getFile(File param0) {
        String var0 = DATE_FORMAT.format(new Date());
        int var1 = 1;

        while(true) {
            File var2 = new File(param0, var0 + (var1 == 1 ? "" : "_" + var1) + ".png");
            if (!var2.exists()) {
                return var2;
            }

            ++var1;
        }
    }
}
