package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private static final Component DISABLED_ACTIVITY_FEED_LABEL = new TranslatableComponent("mco.configure.world.activityfeed.disabled");
    private static final Component NORMAL_USER_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.normal.tooltip");
    private static final Component OP_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.ops.tooltip");
    private static final Component REMOVE_ENTRY_TOOLTIP = new TranslatableComponent("mco.configure.world.invites.remove.tooltip");
    private static final Component INVITED_LABEL = new TranslatableComponent("mco.configure.world.invited");
    private Component toolTip;
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
    private RealmsPlayerScreen.UserAction hoveredUserAction = RealmsPlayerScreen.UserAction.NONE;

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
                new TranslatableComponent("mco.configure.world.buttons.invite"),
                param0 -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
            )
        );
        this.removeButton = this.addButton(
            new Button(
                this.column2X,
                row(7),
                this.columnWidth + 10,
                20,
                new TranslatableComponent("mco.configure.world.invites.remove.tooltip"),
                param0 -> this.uninvite(this.player)
            )
        );
        this.opdeopButton = this.addButton(
            new Button(this.column2X, row(9), this.columnWidth + 10, 20, new TranslatableComponent("mco.configure.world.invites.ops.tooltip"), param0 -> {
                if (this.serverData.players.get(this.player).isOperator()) {
                    this.deop(this.player);
                } else {
                    this.op(this.player);
                }
    
            })
        );
        this.addButton(
            new Button(
                this.column2X + this.columnWidth / 2 + 2,
                row(12),
                this.columnWidth / 2 + 10 - 2,
                20,
                CommonComponents.GUI_BACK,
                param0 -> this.backButtonClicked()
            )
        );
        this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.configure.world.players.title"), this.width / 2, 17, 16777215));
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
            RealmsConfirmScreen var1 = new RealmsConfirmScreen(
                param0x -> {
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
                },
                new TextComponent("Question"),
                new TranslatableComponent("mco.configure.world.uninvite.question").append(" '").append(var0.getName()).append("' ?")
            );
            this.minecraft.setScreen(var1);
        }

    }

    private void deleteFromInvitedList(int param0) {
        this.serverData.players.remove(param0);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.hoveredUserAction = RealmsPlayerScreen.UserAction.NONE;
        this.renderBackground(param0);
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(param0, param1, param2, param3);
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
        this.titleLabel.render(this, param0);
        if (this.serverData != null && this.serverData.players != null) {
            this.font
                .draw(
                    param0,
                    new TextComponent("").append(INVITED_LABEL).append(" (").append(Integer.toString(this.serverData.players.size())).append(")"),
                    (float)this.column1X,
                    (float)row(0),
                    10526880
                );
        } else {
            this.font.draw(param0, INVITED_LABEL, (float)this.column1X, (float)row(0), 10526880);
        }

        super.render(param0, param1, param2, param3);
        if (this.serverData != null) {
            this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
        }
    }

    protected void renderMousehoverTooltip(PoseStack param0, @Nullable Component param1, int param2, int param3) {
        if (param1 != null) {
            int var0 = param2 + 12;
            int var1 = param3 - 12;
            int var2 = this.font.width(param1);
            this.fillGradient(param0, var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, param1, (float)var0, (float)var1, 16777215);
        }
    }

    private void drawRemoveIcon(PoseStack param0, int param1, int param2, int param3, int param4) {
        boolean var0 = param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 9 && param4 < row(12) + 20 && param4 > row(1);
        this.minecraft.getTextureManager().bind(CROSS_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 7.0F : 0.0F;
        GuiComponent.blit(param0, param1, param2, 0.0F, var1, 8, 7, 8, 14);
        if (var0) {
            this.toolTip = REMOVE_ENTRY_TOOLTIP;
            this.hoveredUserAction = RealmsPlayerScreen.UserAction.REMOVE;
        }

    }

    private void drawOpped(PoseStack param0, int param1, int param2, int param3, int param4) {
        boolean var0 = param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 9 && param4 < row(12) + 20 && param4 > row(1);
        this.minecraft.getTextureManager().bind(OP_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 8.0F : 0.0F;
        GuiComponent.blit(param0, param1, param2, 0.0F, var1, 8, 8, 8, 16);
        if (var0) {
            this.toolTip = OP_TOOLTIP;
            this.hoveredUserAction = RealmsPlayerScreen.UserAction.TOGGLE_OP;
        }

    }

    private void drawNormal(PoseStack param0, int param1, int param2, int param3, int param4) {
        boolean var0 = param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 9 && param4 < row(12) + 20 && param4 > row(1);
        this.minecraft.getTextureManager().bind(USER_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 8.0F : 0.0F;
        GuiComponent.blit(param0, param1, param2, 0.0F, var1, 8, 8, 8, 16);
        if (var0) {
            this.toolTip = NORMAL_USER_TOOLTIP;
            this.hoveredUserAction = RealmsPlayerScreen.UserAction.TOGGLE_OP;
        }

    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
        private final PlayerInfo playerInfo;

        public Entry(PlayerInfo param0) {
            this.playerInfo = param0;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderInvitedItem(param0, this.playerInfo, param3, param2, param6, param7);
        }

        private void renderInvitedItem(PoseStack param0, PlayerInfo param1, int param2, int param3, int param4, int param5) {
            int var0;
            if (!param1.getAccepted()) {
                var0 = 10526880;
            } else if (param1.getOnline()) {
                var0 = 8388479;
            } else {
                var0 = 16777215;
            }

            RealmsPlayerScreen.this.font.draw(param0, param1.getName(), (float)(RealmsPlayerScreen.this.column1X + 3 + 12), (float)(param3 + 1), var0);
            if (param1.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(
                    param0, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, param3 + 1, param4, param5
                );
            } else {
                RealmsPlayerScreen.this.drawNormal(
                    param0, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 10, param3 + 1, param4, param5
                );
            }

            RealmsPlayerScreen.this.drawRemoveIcon(
                param0, RealmsPlayerScreen.this.column1X + RealmsPlayerScreen.this.columnWidth - 22, param3 + 2, param4, param5
            );
            RealmsPlayerScreen.this.font
                .draw(
                    param0,
                    RealmsPlayerScreen.DISABLED_ACTIVITY_FEED_LABEL,
                    (float)RealmsPlayerScreen.this.column2X,
                    (float)RealmsPlayerScreen.row(5),
                    10526880
                );
            RealmsTextureManager.withBoundFace(param1.getUuid(), () -> {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.blit(param0, RealmsPlayerScreen.this.column1X + 2 + 2, param3 + 1, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
                GuiComponent.blit(param0, RealmsPlayerScreen.this.column1X + 2 + 2, param3 + 1, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
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
            if (param1 >= 0
                && param1 <= RealmsPlayerScreen.this.serverData.players.size()
                && RealmsPlayerScreen.this.hoveredUserAction != RealmsPlayerScreen.UserAction.NONE) {
                if (RealmsPlayerScreen.this.hoveredUserAction == RealmsPlayerScreen.UserAction.TOGGLE_OP) {
                    if (RealmsPlayerScreen.this.serverData.players.get(param1).isOperator()) {
                        RealmsPlayerScreen.this.deop(param1);
                    } else {
                        RealmsPlayerScreen.this.op(param1);
                    }
                } else if (RealmsPlayerScreen.this.hoveredUserAction == RealmsPlayerScreen.UserAction.REMOVE) {
                    RealmsPlayerScreen.this.uninvite(param1);
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
        public void renderBackground(PoseStack param0) {
            RealmsPlayerScreen.this.renderBackground(param0);
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

    @OnlyIn(Dist.CLIENT)
    static enum UserAction {
        TOGGLE_OP,
        REMOVE,
        NONE;
    }
}
