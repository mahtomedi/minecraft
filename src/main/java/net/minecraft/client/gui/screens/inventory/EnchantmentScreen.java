package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    protected void renderLabels(PoseStack param0, int param1, int param2) {
        this.font.draw(param0, this.title, 12.0F, 5.0F, 4210752);
        this.font.draw(param0, this.inventory.getDisplayName(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
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
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        Lighting.setupForFlatItems();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        int var2 = (int)this.minecraft.getWindow().getGuiScale();
        RenderSystem.viewport((this.width - 320) / 2 * var2, (this.height - 240) / 2 * var2, 320 * var2, 240 * var2);
        RenderSystem.translatef(-0.34F, 0.23F, 0.0F);
        RenderSystem.multMatrix(Matrix4f.perspective(90.0, 1.3333334F, 9.0F, 80.0F));
        RenderSystem.matrixMode(5888);
        param0.pushPose();
        PoseStack.Pose var3 = param0.last();
        var3.pose().setIdentity();
        var3.normal().setIdentity();
        param0.translate(0.0, 3.3F, 1984.0);
        float var4 = 5.0F;
        param0.scale(5.0F, 5.0F, 5.0F);
        param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        param0.mulPose(Vector3f.XP.rotationDegrees(20.0F));
        float var5 = Mth.lerp(param1, this.oOpen, this.open);
        param0.translate((double)((1.0F - var5) * 0.2F), (double)((1.0F - var5) * 0.1F), (double)((1.0F - var5) * 0.25F));
        float var6 = -(1.0F - var5) * 90.0F - 90.0F;
        param0.mulPose(Vector3f.YP.rotationDegrees(var6));
        param0.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        float var7 = Mth.lerp(param1, this.oFlip, this.flip) + 0.25F;
        float var8 = Mth.lerp(param1, this.oFlip, this.flip) + 0.75F;
        var7 = (var7 - (float)Mth.fastFloor((double)var7)) * 1.6F - 0.3F;
        var8 = (var8 - (float)Mth.fastFloor((double)var8)) * 1.6F - 0.3F;
        if (var7 < 0.0F) {
            var7 = 0.0F;
        }

        if (var8 < 0.0F) {
            var8 = 0.0F;
        }

        if (var7 > 1.0F) {
            var7 = 1.0F;
        }

        if (var8 > 1.0F) {
            var8 = 1.0F;
        }

        RenderSystem.enableRescaleNormal();
        BOOK_MODEL.setupAnim(0.0F, var7, var8, var5);
        MultiBufferSource.BufferSource var9 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer var10 = var9.getBuffer(BOOK_MODEL.renderType(ENCHANTING_BOOK_LOCATION));
        BOOK_MODEL.renderToBuffer(param0, var10, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        var9.endBatch();
        param0.popPose();
        RenderSystem.matrixMode(5889);
        RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        Lighting.setupFor3DItems();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
        int var11 = this.menu.getGoldCount();

        for(int var12 = 0; var12 < 3; ++var12) {
            int var13 = var0 + 60;
            int var14 = var13 + 20;
            this.setBlitOffset(0);
            this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
            int var15 = this.menu.costs[var12];
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (var15 == 0) {
                this.blit(param0, var13, var1 + 14 + 19 * var12, 0, 185, 108, 19);
            } else {
                String var16 = "" + var15;
                int var17 = 86 - this.font.width(var16);
                Component var18 = EnchantmentNames.getInstance().getRandomName(this.font, var17);
                int var19 = 6839882;
                if ((var11 < var12 + 1 || this.minecraft.player.experienceLevel < var15) && !this.minecraft.player.abilities.instabuild) {
                    this.blit(param0, var13, var1 + 14 + 19 * var12, 0, 185, 108, 19);
                    this.blit(param0, var13 + 1, var1 + 15 + 19 * var12, 16 * var12, 239, 16, 16);
                    this.font.drawWordWrap(var18, var14, var1 + 16 + 19 * var12, var17, (var19 & 16711422) >> 1);
                    var19 = 4226832;
                } else {
                    int var20 = param2 - (var0 + 60);
                    int var21 = param3 - (var1 + 14 + 19 * var12);
                    if (var20 >= 0 && var21 >= 0 && var20 < 108 && var21 < 19) {
                        this.blit(param0, var13, var1 + 14 + 19 * var12, 0, 204, 108, 19);
                        var19 = 16777088;
                    } else {
                        this.blit(param0, var13, var1 + 14 + 19 * var12, 0, 166, 108, 19);
                    }

                    this.blit(param0, var13 + 1, var1 + 15 + 19 * var12, 16 * var12, 223, 16, 16);
                    this.font.drawWordWrap(var18, var14, var1 + 16 + 19 * var12, var17, var19);
                    var19 = 8453920;
                }

                this.font.drawShadow(param0, var16, (float)(var14 + 86 - this.font.width(var16)), (float)(var1 + 16 + 19 * var12 + 7), var19);
            }
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        param3 = this.minecraft.getFrameTime();
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
        boolean var0 = this.minecraft.player.abilities.instabuild;
        int var1 = this.menu.getGoldCount();

        for(int var2 = 0; var2 < 3; ++var2) {
            int var3 = this.menu.costs[var2];
            Enchantment var4 = Enchantment.byId(this.menu.enchantClue[var2]);
            int var5 = this.menu.levelClue[var2];
            int var6 = var2 + 1;
            if (this.isHovering(60, 14 + 19 * var2, 108, 17, (double)param1, (double)param2) && var3 > 0 && var5 >= 0 && var4 != null) {
                List<Component> var7 = Lists.newArrayList();
                var7.add(
                    new TranslatableComponent("container.enchant.clue", var4.getFullname(var5))
                        .withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.ITALIC})
                );
                if (!var0) {
                    var7.add(TextComponent.EMPTY);
                    if (this.minecraft.player.experienceLevel < var3) {
                        var7.add(new TranslatableComponent("container.enchant.level.requirement", this.menu.costs[var2]).withStyle(ChatFormatting.RED));
                    } else {
                        MutableComponent var8;
                        if (var6 == 1) {
                            var8 = new TranslatableComponent("container.enchant.lapis.one");
                        } else {
                            var8 = new TranslatableComponent("container.enchant.lapis.many", var6);
                        }

                        var7.add(var8.withStyle(var1 >= var6 ? ChatFormatting.GRAY : ChatFormatting.RED));
                        MutableComponent var10;
                        if (var6 == 1) {
                            var10 = new TranslatableComponent("container.enchant.level.one");
                        } else {
                            var10 = new TranslatableComponent("container.enchant.level.many", var6);
                        }

                        var7.add(var10.withStyle(ChatFormatting.GRAY));
                    }
                }

                this.renderTooltip(param0, var7, param1, param2);
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
