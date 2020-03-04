package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation OP_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/op_icon.png");
    private static final ResourceLocation USER_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/user_icon.png");
    private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_player_icon.png");
    private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
    private String toolTip;
    private final RealmsConfigureWorldScreen lastScreen;
    private final RealmsServer serverData;
    private RealmsPlayerScreen.InvitedObjectSelectionList invitedObjectSelectionList;
    private int column1X;
    private int columnWidth;
    private int column2X;
    private Button removeButton;
    private Button opdeopButton;
    private int selectedInvitedIndex = -1;
    private String selectedInvited;
    private int player = -1;
    private boolean stateChanged;
    private RealmsLabel titleLabel;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        this.lastScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void init() {
        this.column1X = this.width / 2 - 160;
        this.columnWidth = 150;
        this.column2X = this.width / 2 + 12;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.invitedObjectSelectionList = new RealmsPlayerScreen.InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1X);
        this.addWidget(this.invitedObjectSelectionList);

        for(PlayerInfo var0 : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(var0);
        }

        this.addButton(
            new Button(
                this.column2X,
                row(1),
                this.columnWidth + 10,
                20,
                I18n.get("mco.configure.world.buttons.invite"),
                param0 -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
            )
        );
        this.removeButton = this.addButton(
            new Button(
                this.column2X, row(7), this.columnWidth + 10, 20, I18n.get("mco.configure.world.invites.remove.tooltip"), param0 -> this.uninvite(this.player)
            )
        );
        this.opdeopButton = this.addButton(
            new Button(this.column2X, row(9), this.columnWidth + 10, 20, I18n.get("mco.configure.world.invites.ops.tooltip"), param0 -> {
                if (this.serverData.players.get(this.player).isOperator()) {
                    this.deop(this.player);
                } else {
                    this.op(this.player);
                }
    
            })
        );
        this.addButton(
            new Button(
                this.column2X + this.columnWidth / 2 + 2, row(12), this.columnWidth / 2 + 10 - 2, 20, I18n.get("gui.back"), param0 -> this.backButtonClicked()
            )
        );
        this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.configure.world.players.title"), this.width / 2, 17, 16777215));
        this.narrateLabels();
        this.updateButtonStates();
    }

    private void updateButtonStates() {
        this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
        this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.player);
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.backButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        if (this.stateChanged) {
            this.minecraft.setScreen(this.lastScreen.getNewScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    private void op(int param0) {
        this.updateButtonStates();
        RealmsClient var0 = RealmsClient.create();
        String var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.op(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't op the user");
        }

    }

    private void deop(int param0) {
        this.updateButtonStates();
        RealmsClient var0 = RealmsClient.create();
        String var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.deop(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't deop the user");
        }

    }

    private void updateOps(Ops param0) {
        for(PlayerInfo var0 : this.serverData.players) {
            var0.setOperator(param0.ops.contains(var0.getName()));
        }

    }

    private void uninvite(int param0) {
        this.updateButtonStates();
        if (param0 >= 0 && param0 < this.serverData.players.size()) {
            PlayerInfo var0 = this.serverData.players.get(param0);
            this.selectedInvited = var0.getUuid();
            this.selectedInvitedIndex = param0;
            RealmsConfirmScreen var1 = new RealmsConfirmScreen(param0x -> {
                if (param0x) {
                    RealmsClient var0x = RealmsClient.create();

                    try {
                        var0x.uninvite(this.serverData.id, this.selectedInvited);
                    } catch (RealmsServiceException var4) {
                        LOGGER.error("Couldn't uninvite user");
                    }

                    this.deleteFromInvitedList(this.selectedInvitedIndex);
                    this.player = -1;
                    this.updateButtonStates();
                }

                this.stateChanged = true;
                this.minecraft.setScreen(this);
            }, "Question", I18n.get("mco.configure.world.uninvite.question") + " '" + var0.getName() + "' ?");
            this.minecraft.setScreen(var1);
        }

    }

    private void deleteFromInvitedList(int param0) {
        this.serverData.players.remove(param0);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(param0, param1, param2);
        }

        int var0 = row(12) + 20;
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        this.minecraft.getTextureManager().bind(OPTIONS_BACKGROUND);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var3 = 32.0F;
        var2.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var2.vertex(0.0, (double)this.height, 0.0).uv(0.0F, (float)(this.height - var0) / 32.0F + 0.0F).color(64, 64, 64, 255).endVertex();
        var2.vertex((double)this.width, (double)this.height, 0.0)
            .uv((float)this.width / 32.0F, (float)(this.height - var0) / 32.0F + 0.0F)
            .color(64, 64, 64, 255)
            .endVertex();
        var2.vertex((double)this.width, (double)var0, 0.0).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        var2.vertex(0.0, (double)var0, 0.0).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
        var1.end();
        this.titleLabel.render(this);
        if (this.serverData != null && this.serverData.players != null) {
            this.font
                .draw(I18n.get("mco.configure.world.invited") + " (" + this.serverData.players.size() + ")", (float)this.column1X, (float)row(0), 10526880);
        } else {
            this.font.draw(I18n.get("mco.configure.world.invited"), (float)this.column1X, (float)row(0), 10526880);
        }

        super.render(param0, param1, param2);
        if (this.serverData != null) {
            if (this.toolTip != null) {
                this.renderMousehoverTooltip(this.toolTip, param0, param1);
            }

        }
    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.font.width(param0);
            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, (float)var0, (float)var1, 16777215);
        }
    }

    private void drawRemoveIcon(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 9 && param3 < row(12) + 20 && param3 > row(1);
        this.minecraft.getTextureManager().bind(CROSS_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 7.0F : 0.0F;
        GuiComponent.blit(param0, param1, 0.0F, var1, 8, 7, 8, 14);
        if (var0) {
            this.toolTip = I18n.get("mco.configure.world.invites.remove.tooltip");
        }

    }

    private void drawOpped(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 9 && param3 < row(12) + 20 && param3 > row(1);
        this.minecraft.getTextureManager().bind(OP_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 8.0F : 0.0F;
        GuiComponent.blit(param0, param1, 0.0F, var1, 8, 8, 8, 16);
        if (var0) {
            this.toolTip = I18n.get("mco.configure.world.invites.ops.tooltip");
        }

    }

    private void drawNormal(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 9 && param3 < row(12) + 20 && param3 > row(1);
        this.minecraft.getTextureManager().bind(USER_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 8.0F : 0.0F;
        GuiComponent.blit(param0, param1, 0.0F, var1, 8, 8, 8, 16);
        if (var0) {
            this.toolTip = I18n.get("mco.configure.world.invites.normal.tooltip");
        }

    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
        private final PlayerInfo playerInfo;

        public Entry(PlayerInfo param0) {
            this.playerInfo = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderInvitedItem(this.playerInfo, param2, param1, param5, param6);
        }

        private void renderInvitedItem(PlayerInfo param0, int param1, int param2, int param3, int param4) {
            int var0;
            if (!param0.getAccepted()) {
                var0 = 10526880;
            } else if (param0.getOnline()) {
                var0 = 8388479;
            } else {
                var0 = 16777215;
            }

            RealmsPlayerScreen.this.font.draw(param0.getName(), (float)(RealmsPlayerScreen.this.column1X + 3 + 12), (float)(param2 + 1), var0);
            if (param0.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, param2 + 1, param3, param4);
            } else {
                RealmsPlayerScreen.this.drawNormal(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, param2 + 1, param3, param4);
            }

            RealmsPlayerScreen.this.drawRemoveIcon(RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 22, param2 + 2, param3, param4);
            RealmsPlayerScreen.this.font
                .draw(
                    I18n.get("mco.configure.world.activityfeed.disabled"), (float)RealmsPlayerScreen.this.column2X, (float)RealmsPlayerScreen.row(5), 10526880
                );
            RealmsTextureManager.withBoundFace(param0.getUuid(), () -> {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.blit(RealmsPlayerScreen.this.column1X + 2 + 2, param2 + 1, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
                GuiComponent.blit(RealmsPlayerScreen.this.column1X + 2 + 2, param2 + 1, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    class InvitedObjectSelectionList extends RealmsObjectSelectionList<RealmsPlayerScreen.Entry> {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
        }

        public void addEntry(PlayerInfo param0) {
            this.addEntry(RealmsPlayerScreen.this.new Entry(param0));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 1.0);
        }

        @Override
        public boolean isFocused() {
            return RealmsPlayerScreen.this.getFocused() == this;
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param0 < (double)this.getScrollbarPosition() && param1 >= (double)this.y0 && param1 <= (double)this.y1) {
                int var0 = RealmsPlayerScreen.this.column1X;
                int var1 = RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth;
                int var2 = (int)Math.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int var3 = var2 / this.itemHeight;
                if (param0 >= (double)var0 && param0 <= (double)var1 && var3 >= 0 && var2 >= 0 && var3 < this.getItemCount()) {
                    this.selectItem(var3);
                    this.itemClicked(var2, var3, param0, param1, this.width);
                }

                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            if (param1 >= 0 && param1 <= RealmsPlayerScreen.this.serverData.players.size() && RealmsPlayerScreen.this.toolTip != null) {
                if (!RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.ops.tooltip"))
                    && !RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.normal.tooltip"))) {
                    if (RealmsPlayerScreen.this.toolTip.equals(I18n.get("mco.configure.world.invites.remove.tooltip"))) {
                        RealmsPlayerScreen.this.uninvite(param1);
                    }
                } else if (RealmsPlayerScreen.this.serverData.players.get(param1).isOperator()) {
                    RealmsPlayerScreen.this.deop(param1);
                } else {
                    RealmsPlayerScreen.this.op(param1);
                }

            }
        }

        @Override
        public void selectItem(int param0) {
            this.setSelectedItem(param0);
            if (param0 != -1) {
                NarrationHelper.now(I18n.get("narrator.select", RealmsPlayerScreen.this.serverData.players.get(param0).getName()));
            }

            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPlayerScreen.this.player = param0;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsPlayerScreen.Entry param0) {
            super.setSelected(param0);
            RealmsPlayerScreen.this.player = this.children().indexOf(param0);
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void renderBackground() {
            RealmsPlayerScreen.this.renderBackground();
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1X + this.width - 5;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }
}
