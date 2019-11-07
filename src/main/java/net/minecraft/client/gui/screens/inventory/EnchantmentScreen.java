package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private static final BookModel BOOK_MODEL = new BookModel();
    private final Random random = new Random();
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    public EnchantmentScreen(EnchantmentMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        this.font.draw(this.title.getColoredString(), 12.0F, 5.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickBook();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;

        for(int var2 = 0; var2 < 3; ++var2) {
            double var3 = param0 - (double)(var0 + 60);
            double var4 = param1 - (double)(var1 + 14 + 19 * var2);
            if (var3 >= 0.0 && var4 >= 0.0 && var3 < 108.0 && var4 < 19.0 && this.menu.clickMenuButton(this.minecraft.player, var2)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var2);
                return true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        int var2 = (int)this.minecraft.getWindow().getGuiScale();
        RenderSystem.viewport((this.width - 320) / 2 * var2, (this.height - 240) / 2 * var2, 320 * var2, 240 * var2);
        RenderSystem.translatef(-0.34F, 0.23F, 0.0F);
        RenderSystem.multMatrix(Matrix4f.perspective(90.0, 1.3333334F, 9.0F, 80.0F));
        RenderSystem.matrixMode(5888);
        PoseStack var3 = new PoseStack();
        var3.pushPose();
        PoseStack.Pose var4 = var3.last();
        var4.pose().setIdentity();
        var4.normal().setIdentity();
        var3.translate(0.0, 3.3F, 1984.0);
        float var5 = 5.0F;
        var3.scale(5.0F, 5.0F, 5.0F);
        var3.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        var3.mulPose(Vector3f.XP.rotationDegrees(20.0F));
        float var6 = Mth.lerp(param0, this.oOpen, this.open);
        var3.translate((double)((1.0F - var6) * 0.2F), (double)((1.0F - var6) * 0.1F), (double)((1.0F - var6) * 0.25F));
        float var7 = -(1.0F - var6) * 90.0F - 90.0F;
        var3.mulPose(Vector3f.YP.rotationDegrees(var7));
        var3.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        float var8 = Mth.lerp(param0, this.oFlip, this.flip) + 0.25F;
        float var9 = Mth.lerp(param0, this.oFlip, this.flip) + 0.75F;
        var8 = (var8 - (float)Mth.fastFloor((double)var8)) * 1.6F - 0.3F;
        var9 = (var9 - (float)Mth.fastFloor((double)var9)) * 1.6F - 0.3F;
        if (var8 < 0.0F) {
            var8 = 0.0F;
        }

        if (var9 < 0.0F) {
            var9 = 0.0F;
        }

        if (var8 > 1.0F) {
            var8 = 1.0F;
        }

        if (var9 > 1.0F) {
            var9 = 1.0F;
        }

        RenderSystem.enableRescaleNormal();
        BOOK_MODEL.setupAnim(0.0F, var8, var9, var6);
        MultiBufferSource.BufferSource var10 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer var11 = var10.getBuffer(BOOK_MODEL.renderType(ENCHANTING_BOOK_LOCATION));
        BOOK_MODEL.renderToBuffer(var3, var11, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        var10.endBatch();
        var3.popPose();
        RenderSystem.matrixMode(5889);
        RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
        int var12 = this.menu.getGoldCount();

        for(int var13 = 0; var13 < 3; ++var13) {
            int var14 = var0 + 60;
            int var15 = var14 + 20;
            this.setBlitOffset(0);
            this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
            int var16 = this.menu.costs[var13];
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (var16 == 0) {
                this.blit(var14, var1 + 14 + 19 * var13, 0, 185, 108, 19);
            } else {
                String var17 = "" + var16;
                int var18 = 86 - this.font.width(var17);
                String var19 = EnchantmentNames.getInstance().getRandomName(this.font, var18);
                Font var20 = this.minecraft.getFontManager().get(Minecraft.ALT_FONT);
                int var21 = 6839882;
                if ((var12 < var13 + 1 || this.minecraft.player.experienceLevel < var16) && !this.minecraft.player.abilities.instabuild) {
                    this.blit(var14, var1 + 14 + 19 * var13, 0, 185, 108, 19);
                    this.blit(var14 + 1, var1 + 15 + 19 * var13, 16 * var13, 239, 16, 16);
                    var20.drawWordWrap(var19, var15, var1 + 16 + 19 * var13, var18, (var21 & 16711422) >> 1);
                    var21 = 4226832;
                } else {
                    int var22 = param1 - (var0 + 60);
                    int var23 = param2 - (var1 + 14 + 19 * var13);
                    if (var22 >= 0 && var23 >= 0 && var22 < 108 && var23 < 19) {
                        this.blit(var14, var1 + 14 + 19 * var13, 0, 204, 108, 19);
                        var21 = 16777088;
                    } else {
                        this.blit(var14, var1 + 14 + 19 * var13, 0, 166, 108, 19);
                    }

                    this.blit(var14 + 1, var1 + 15 + 19 * var13, 16 * var13, 223, 16, 16);
                    var20.drawWordWrap(var19, var15, var1 + 16 + 19 * var13, var18, var21);
                    var21 = 8453920;
                }

                var20 = this.minecraft.font;
                var20.drawShadow(var17, (float)(var15 + 86 - var20.width(var17)), (float)(var1 + 16 + 19 * var13 + 7), var21);
            }
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        param2 = this.minecraft.getFrameTime();
        this.renderBackground();
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
        boolean var0 = this.minecraft.player.abilities.instabuild;
        int var1 = this.menu.getGoldCount();

        for(int var2 = 0; var2 < 3; ++var2) {
            int var3 = this.menu.costs[var2];
            Enchantment var4 = Enchantment.byId(this.menu.enchantClue[var2]);
            int var5 = this.menu.levelClue[var2];
            int var6 = var2 + 1;
            if (this.isHovering(60, 14 + 19 * var2, 108, 17, (double)param0, (double)param1) && var3 > 0 && var5 >= 0 && var4 != null) {
                List<String> var7 = Lists.newArrayList();
                var7.add("" + ChatFormatting.WHITE + ChatFormatting.ITALIC + I18n.get("container.enchant.clue", var4.getFullname(var5).getColoredString()));
                if (!var0) {
                    var7.add("");
                    if (this.minecraft.player.experienceLevel < var3) {
                        var7.add(ChatFormatting.RED + I18n.get("container.enchant.level.requirement", this.menu.costs[var2]));
                    } else {
                        String var8;
                        if (var6 == 1) {
                            var8 = I18n.get("container.enchant.lapis.one");
                        } else {
                            var8 = I18n.get("container.enchant.lapis.many", var6);
                        }

                        ChatFormatting var10 = var1 >= var6 ? ChatFormatting.GRAY : ChatFormatting.RED;
                        var7.add(var10 + "" + var8);
                        if (var6 == 1) {
                            var8 = I18n.get("container.enchant.level.one");
                        } else {
                            var8 = I18n.get("container.enchant.level.many", var6);
                        }

                        var7.add(ChatFormatting.GRAY + "" + var8);
                    }
                }

                this.renderTooltip(var7, param0, param1);
                break;
            }
        }

    }

    public void tickBook() {
        ItemStack var0 = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(var0, this.last)) {
            this.last = var0;

            do {
                this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while(this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }

        ++this.time;
        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean var1 = false;

        for(int var2 = 0; var2 < 3; ++var2) {
            if (this.menu.costs[var2] != 0) {
                var1 = true;
            }
        }

        if (var1) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float var3 = (this.flipT - this.flip) * 0.4F;
        float var4 = 0.2F;
        var3 = Mth.clamp(var3, -0.2F, 0.2F);
        this.flipA += (var3 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }
}
