package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoRenderer {
    public static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    public static final ResourceLocation EASTER_EGG_LOGO = new ResourceLocation("textures/gui/title/minceraft.png");
    public static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    public static final int LOGO_WIDTH = 256;
    public static final int LOGO_HEIGHT = 44;
    private static final int LOGO_TEXTURE_WIDTH = 256;
    private static final int LOGO_TEXTURE_HEIGHT = 64;
    private static final int EDITION_WIDTH = 128;
    private static final int EDITION_HEIGHT = 14;
    private static final int EDITION_TEXTURE_WIDTH = 128;
    private static final int EDITION_TEXTURE_HEIGHT = 16;
    public static final int DEFAULT_HEIGHT_OFFSET = 30;
    private static final int EDITION_LOGO_OVERLAP = 7;
    private final boolean showEasterEgg = (double)RandomSource.create().nextFloat() < 1.0E-4;
    private final boolean keepLogoThroughFade;

    public LogoRenderer(boolean param0) {
        this.keepLogoThroughFade = param0;
    }

    public void renderLogo(GuiGraphics param0, int param1, float param2) {
        this.renderLogo(param0, param1, param2, 30);
    }

    public void renderLogo(GuiGraphics param0, int param1, float param2, int param3) {
        param0.setColor(1.0F, 1.0F, 1.0F, this.keepLogoThroughFade ? 1.0F : param2);
        int var0 = param1 / 2 - 128;
        param0.blit(this.showEasterEgg ? EASTER_EGG_LOGO : MINECRAFT_LOGO, var0, param3, 0.0F, 0.0F, 256, 44, 256, 64);
        int var1 = param1 / 2 - 64;
        int var2 = param3 + 44 - 7;
        param0.blit(MINECRAFT_EDITION, var1, var2, 0.0F, 0.0F, 128, 14, 128, 16);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
