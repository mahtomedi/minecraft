package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Screenshot {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private int rowHeight;
    private final DataOutputStream outputStream;
    private final byte[] bytes;
    private final int width;
    private final int height;
    private File file;

    public static void grab(File param0, RenderTarget param1, Consumer<Component> param2) {
        grab(param0, null, param1, param2);
    }

    public static void grab(File param0, @Nullable String param1, RenderTarget param2, Consumer<Component> param3) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _grab(param0, param1, param2, param3));
        } else {
            _grab(param0, param1, param2, param3);
        }

    }

    private static void _grab(File param0, @Nullable String param1, RenderTarget param2, Consumer<Component> param3) {
        NativeImage var0 = takeScreenshot(param2);
        File var1 = new File(param0, "screenshots");
        var1.mkdir();
        File var2;
        if (param1 == null) {
            var2 = getFile(var1);
        } else {
            var2 = new File(var1, param1);
        }

        Util.ioPool()
            .execute(
                () -> {
                    try {
                        var0.writeToFile(var2);
                        Component var0x = new TextComponent(var2.getName())
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var2.getAbsolutePath())));
                        param3.accept(new TranslatableComponent("screenshot.success", var0x));
                    } catch (Exception var7) {
                        LOGGER.warn("Couldn't save screenshot", (Throwable)var7);
                        param3.accept(new TranslatableComponent("screenshot.failure", var7.getMessage()));
                    } finally {
                        var0.close();
                    }
        
                }
            );
    }

    public static NativeImage takeScreenshot(RenderTarget param0) {
        int var0 = param0.width;
        int var1 = param0.height;
        NativeImage var2 = new NativeImage(var0, var1, false);
        RenderSystem.bindTexture(param0.getColorTextureId());
        var2.downloadTexture(0, true);
        var2.flipY();
        return var2;
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

    public Screenshot(File param0, int param1, int param2, int param3) throws IOException {
        this.width = param1;
        this.height = param2;
        this.rowHeight = param3;
        File var0 = new File(param0, "screenshots");
        var0.mkdir();
        String var1 = "huge_" + DATE_FORMAT.format(new Date());
        int var2 = 1;

        while((this.file = new File(var0, var1 + (var2 == 1 ? "" : "_" + var2) + ".tga")).exists()) {
            ++var2;
        }

        byte[] var3 = new byte[18];
        var3[2] = 2;
        var3[12] = (byte)(param1 % 256);
        var3[13] = (byte)(param1 / 256);
        var3[14] = (byte)(param2 % 256);
        var3[15] = (byte)(param2 / 256);
        var3[16] = 24;
        this.bytes = new byte[param1 * param3 * 3];
        this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
        this.outputStream.write(var3);
    }

    public void addRegion(ByteBuffer param0, int param1, int param2, int param3, int param4) {
        int var0 = param3;
        int var1 = param4;
        if (param3 > this.width - param1) {
            var0 = this.width - param1;
        }

        if (param4 > this.height - param2) {
            var1 = this.height - param2;
        }

        this.rowHeight = var1;

        for(int var2 = 0; var2 < var1; ++var2) {
            param0.position((param4 - var1) * param3 * 3 + var2 * param3 * 3);
            int var3 = (param1 + var2 * this.width) * 3;
            param0.get(this.bytes, var3, var0 * 3);
        }

    }

    public void saveRow() throws IOException {
        this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
    }

    public File close() throws IOException {
        this.outputStream.close();
        return this.file;
    }
}
