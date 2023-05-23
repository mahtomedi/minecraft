package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
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
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void containerTick() {
        super.containerTick();
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
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(ENCHANTING_TABLE_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        this.renderBook(param0, var0, var1, param1);
        EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
        int var2 = this.menu.getGoldCount();

        for(int var3 = 0; var3 < 3; ++var3) {
            int var4 = var0 + 60;
            int var5 = var4 + 20;
            int var6 = this.menu.costs[var3];
            if (var6 == 0) {
                param0.blit(ENCHANTING_TABLE_LOCATION, var4, var1 + 14 + 19 * var3, 0, 185, 108, 19);
            } else {
                String var7 = var6 + "";
                int var8 = 86 - this.font.width(var7);
                FormattedText var9 = EnchantmentNames.getInstance().getRandomName(this.font, var8);
                int var10 = 6839882;
                if ((var2 < var3 + 1 || this.minecraft.player.experienceLevel < var6) && !this.minecraft.player.getAbilities().instabuild) {
                    param0.blit(ENCHANTING_TABLE_LOCATION, var4, var1 + 14 + 19 * var3, 0, 185, 108, 19);
                    param0.blit(ENCHANTING_TABLE_LOCATION, var4 + 1, var1 + 15 + 19 * var3, 16 * var3, 239, 16, 16);
                    param0.drawWordWrap(this.font, var9, var5, var1 + 16 + 19 * var3, var8, (var10 & 16711422) >> 1);
                    var10 = 4226832;
                } else {
                    int var11 = param2 - (var0 + 60);
                    int var12 = param3 - (var1 + 14 + 19 * var3);
                    if (var11 >= 0 && var12 >= 0 && var11 < 108 && var12 < 19) {
                        param0.blit(ENCHANTING_TABLE_LOCATION, var4, var1 + 14 + 19 * var3, 0, 204, 108, 19);
                        var10 = 16777088;
                    } else {
                        param0.blit(ENCHANTING_TABLE_LOCATION, var4, var1 + 14 + 19 * var3, 0, 166, 108, 19);
                    }

                    param0.blit(ENCHANTING_TABLE_LOCATION, var4 + 1, var1 + 15 + 19 * var3, 16 * var3, 223, 16, 16);
                    param0.drawWordWrap(this.font, var9, var5, var1 + 16 + 19 * var3, var8, var10);
                    var10 = 8453920;
                }

                param0.drawString(this.font, var7, var5 + 86 - this.font.width(var7), var1 + 16 + 19 * var3 + 7, var10);
            }
        }

    }

    private void renderBook(GuiGraphics param0, int param1, int param2, float param3) {
        float var0 = Mth.lerp(param3, this.oOpen, this.open);
        float var1 = Mth.lerp(param3, this.oFlip, this.flip);
        Lighting.setupForEntityInInventory();
        param0.pose().pushPose();
        param0.pose().translate((float)param1 + 33.0F, (float)param2 + 31.0F, 100.0F);
        float var2 = 40.0F;
        param0.pose().scale(-40.0F, 40.0F, 40.0F);
        param0.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
        param0.pose().translate((1.0F - var0) * 0.2F, (1.0F - var0) * 0.1F, (1.0F - var0) * 0.25F);
        float var3 = -(1.0F - var0) * 90.0F - 90.0F;
        param0.pose().mulPose(Axis.YP.rotationDegrees(var3));
        param0.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
        float var4 = Mth.clamp(Mth.frac(var1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float var5 = Mth.clamp(Mth.frac(var1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.bookModel.setupAnim(0.0F, var4, var5, var0);
        VertexConsumer var6 = param0.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
        this.bookModel.renderToBuffer(param0.pose(), var6, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        param0.flush();
        param0.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        param3 = this.minecraft.getFrameTime();
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
        boolean var0 = this.minecraft.player.getAbilities().instabuild;
        int var1 = this.menu.getGoldCount();

        for(int var2 = 0; var2 < 3; ++var2) {
            int var3 = this.menu.costs[var2];
            Enchantment var4 = Enchantment.byId(this.menu.enchantClue[var2]);
            int var5 = this.menu.levelClue[var2];
            int var6 = var2 + 1;
            if (this.isHovering(60, 14 + 19 * var2, 108, 17, (double)param1, (double)param2) && var3 > 0 && var5 >= 0 && var4 != null) {
                List<Component> var7 = Lists.newArrayList();
                var7.add(Component.translatable("container.enchant.clue", var4.getFullname(var5)).withStyle(ChatFormatting.WHITE));
                if (!var0) {
                    var7.add(CommonComponents.EMPTY);
                    if (this.minecraft.player.experienceLevel < var3) {
                        var7.add(Component.translatable("container.enchant.level.requirement", this.menu.costs[var2]).withStyle(ChatFormatting.RED));
                    } else {
                        MutableComponent var8;
                        if (var6 == 1) {
                            var8 = Component.translatable("container.enchant.lapis.one");
                        } else {
                            var8 = Component.translatable("container.enchant.lapis.many", var6);
                        }

                        var7.add(var8.withStyle(var1 >= var6 ? ChatFormatting.GRAY : ChatFormatting.RED));
                        MutableComponent var10;
                        if (var6 == 1) {
                            var10 = Component.translatable("container.enchant.level.one");
                        } else {
                            var10 = Component.translatable("container.enchant.level.many", var6);
                        }

                        var7.add(var10.withStyle(ChatFormatting.GRAY));
                    }
                }

                param0.renderComponentTooltip(this.font, var7, param1, param2);
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
