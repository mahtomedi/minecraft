package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SystemToast implements Toast {
    private static final long DISPLAY_TIME = 5000L;
    private static final int MAX_LINE_SIZE = 200;
    private final SystemToast.SystemToastIds id;
    private Component title;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;

    public SystemToast(SystemToast.SystemToastIds param0, Component param1, @Nullable Component param2) {
        this(
            param0,
            param1,
            nullToEmpty(param2),
            Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(param1), param2 == null ? 0 : Minecraft.getInstance().font.width(param2)))
        );
    }

    public static SystemToast multiline(Minecraft param0, SystemToast.SystemToastIds param1, Component param2, Component param3) {
        Font var0 = param0.font;
        List<FormattedCharSequence> var1 = var0.split(param3, 200);
        int var2 = Math.max(200, var1.stream().mapToInt(var0::width).max().orElse(200));
        return new SystemToast(param1, param2, var1, var2 + 30);
    }

    private SystemToast(SystemToast.SystemToastIds param0, Component param1, List<FormattedCharSequence> param2, int param3) {
        this.id = param0;
        this.title = param1;
        this.messageLines = param2;
        this.width = param3;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component param0) {
        return param0 == null ? ImmutableList.of() : ImmutableList.of(param0.getVisualOrderText());
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public Toast.Visibility render(PoseStack param0, ToastComponent param1, long param2) {
        if (this.changed) {
            this.lastChanged = param2;
            this.changed = false;
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int var0 = this.width();
        int var1 = 12;
        if (var0 == 160 && this.messageLines.size() <= 1) {
            param1.blit(param0, 0, 0, 0, 64, var0, this.height());
        } else {
            int var2 = this.height() + Math.max(0, this.messageLines.size() - 1) * 12;
            int var3 = 28;
            int var4 = Math.min(4, var2 - 28);
            this.renderBackgroundRow(param0, param1, var0, 0, 0, 28);

            for(int var5 = 28; var5 < var2 - var4; var5 += 10) {
                this.renderBackgroundRow(param0, param1, var0, 16, var5, Math.min(16, var2 - var5 - var4));
            }

            this.renderBackgroundRow(param0, param1, var0, 32 - var4, var2 - var4, var4);
        }

        if (this.messageLines == null) {
            param1.getMinecraft().font.draw(param0, this.title, 18.0F, 12.0F, -256);
        } else {
            param1.getMinecraft().font.draw(param0, this.title, 18.0F, 7.0F, -256);

            for(int var6 = 0; var6 < this.messageLines.size(); ++var6) {
                param1.getMinecraft().font.draw(param0, this.messageLines.get(var6), 18.0F, (float)(18 + var6 * 12), -1);
            }
        }

        return param2 - this.lastChanged < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void renderBackgroundRow(PoseStack param0, ToastComponent param1, int param2, int param3, int param4, int param5) {
        int var0 = param3 == 0 ? 20 : 5;
        int var1 = Math.min(60, param2 - var0);
        param1.blit(param0, 0, param4, 0, 64 + param3, var0, param5);

        for(int var2 = var0; var2 < param2 - var1; var2 += 64) {
            param1.blit(param0, var2, param4, 32, 64 + param3, Math.min(64, param2 - var2 - var1), param5);
        }

        param1.blit(param0, param2 - var1, param4, 160 - var1, 64 + param3, var1, param5);
    }

    public void reset(Component param0, @Nullable Component param1) {
        this.title = param0;
        this.messageLines = nullToEmpty(param1);
        this.changed = true;
    }

    public SystemToast.SystemToastIds getToken() {
        return this.id;
    }

    public static void add(ToastComponent param0, SystemToast.SystemToastIds param1, Component param2, @Nullable Component param3) {
        param0.addToast(new SystemToast(param1, param2, param3));
    }

    public static void addOrUpdate(ToastComponent param0, SystemToast.SystemToastIds param1, Component param2, @Nullable Component param3) {
        SystemToast var0 = param0.getToast(SystemToast.class, param1);
        if (var0 == null) {
            add(param0, param1, param2, param3);
        } else {
            var0.reset(param2, param3);
        }

    }

    public static void onWorldAccessFailure(Minecraft param0, String param1) {
        add(
            param0.getToasts(),
            SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
            Component.translatable("selectWorld.access_failure"),
            Component.literal(param1)
        );
    }

    public static void onWorldDeleteFailure(Minecraft param0, String param1) {
        add(
            param0.getToasts(),
            SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
            Component.translatable("selectWorld.delete_failure"),
            Component.literal(param1)
        );
    }

    public static void onPackCopyFailure(Minecraft param0, String param1) {
        add(param0.getToasts(), SystemToast.SystemToastIds.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(param1));
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SystemToastIds {
        TUTORIAL_HINT,
        NARRATOR_TOGGLE,
        WORLD_BACKUP,
        WORLD_GEN_SETTINGS_TRANSFER,
        PACK_LOAD_FAILURE,
        WORLD_ACCESS_FAILURE,
        PACK_COPY_FAILURE,
        PERIODIC_NOTIFICATION;
    }
}
