package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
    static final ResourceLocation ACCEPT_HIGHLIGHTED_SPRITE = new ResourceLocation("pending_invite/accept_highlighted");
    static final ResourceLocation ACCEPT_SPRITE = new ResourceLocation("pending_invite/accept");
    static final ResourceLocation REJECT_HIGHLIGHTED_SPRITE = new ResourceLocation("pending_invite/reject_highlighted");
    static final ResourceLocation REJECT_SPRITE = new ResourceLocation("pending_invite/reject");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
    static final Component ACCEPT_INVITE = Component.translatable("mco.invites.button.accept");
    static final Component REJECT_INVITE = Component.translatable("mco.invites.button.reject");
    private final Screen lastScreen;
    private final CompletableFuture<List<PendingInvite>> pendingInvites = CompletableFuture.supplyAsync(() -> {
        try {
            return RealmsClient.create().pendingInvites().pendingInvites;
        } catch (RealmsServiceException var1x) {
            LOGGER.error("Couldn't list invites", (Throwable)var1x);
            return List.of();
        }
    }, Util.ioPool());
    @Nullable
    Component toolTip;
    RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
    int selectedInvite = -1;
    private Button acceptButton;
    private Button rejectButton;

    public RealmsPendingInvitesScreen(Screen param0, Component param1) {
        super(param1);
        this.lastScreen = param0;
    }

    @Override
    public void init() {
        RealmsMainScreen.refreshPendingInvites();
        this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
        this.pendingInvites.thenAcceptAsync(param0 -> {
            List<RealmsPendingInvitesScreen.Entry> var0 = param0.stream().map(param0x -> new RealmsPendingInvitesScreen.Entry(param0x)).toList();
            this.pendingInvitationSelectionList.replaceEntries(var0);
            if (var0.isEmpty()) {
                this.minecraft.getNarrator().say(NO_PENDING_INVITES_TEXT);
            }

        }, this.screenExecutor);
        this.addRenderableWidget(this.pendingInvitationSelectionList);
        this.acceptButton = this.addRenderableWidget(Button.builder(ACCEPT_INVITE, param0 -> {
            this.handleInvitation(this.selectedInvite, true);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onClose()).bounds(this.width / 2 - 50, this.height - 32, 100, 20).build()
        );
        this.rejectButton = this.addRenderableWidget(Button.builder(REJECT_INVITE, param0 -> {
            this.handleInvitation(this.selectedInvite, false);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
        this.updateButtonStates();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    void handleInvitation(int param0, boolean param1) {
        if (param0 < this.pendingInvitationSelectionList.getItemCount()) {
            String var0 = this.pendingInvitationSelectionList.children().get(param0).pendingInvite.invitationId;
            CompletableFuture.<Boolean>supplyAsync(() -> {
                try {
                    RealmsClient var1x = RealmsClient.create();
                    if (param1) {
                        var1x.acceptInvitation(var0);
                    } else {
                        var1x.rejectInvitation(var0);
                    }

                    return true;
                } catch (RealmsServiceException var3x) {
                    LOGGER.error("Couldn't handle invite", (Throwable)var3x);
                    return false;
                }
            }, Util.ioPool()).thenAcceptAsync(param2 -> {
                if (param2) {
                    this.pendingInvitationSelectionList.removeAtIndex(param0);
                    RealmsDataFetcher var0x = this.minecraft.realmsDataFetcher();
                    if (param1) {
                        var0x.serverListUpdateTask.reset();
                    }

                    var0x.pendingInvitesTask.reset();
                }

            }, this.screenExecutor);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.toolTip = null;
        param0.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
        if (this.toolTip != null) {
            param0.renderTooltip(this.font, this.toolTip, param1, param2);
        }

        if (this.pendingInvites.isDone() && this.pendingInvitationSelectionList.getItemCount() == 0) {
            param0.drawCenteredString(this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, -1);
        }

    }

    void updateButtonStates() {
        this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
        this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
        private static final int TEXT_LEFT = 38;
        final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        Entry(PendingInvite param0) {
            this.pendingInvite = param0;
            this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderPendingInvitationItem(param0, this.pendingInvite, param3, param2, param6, param7);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, param2, param0, param1);
            return true;
        }

        private void renderPendingInvitationItem(GuiGraphics param0, PendingInvite param1, int param2, int param3, int param4, int param5) {
            param0.drawString(RealmsPendingInvitesScreen.this.font, param1.worldName, param2 + 38, param3 + 1, -1, false);
            param0.drawString(RealmsPendingInvitesScreen.this.font, param1.worldOwnerName, param2 + 38, param3 + 12, 7105644, false);
            param0.drawString(
                RealmsPendingInvitesScreen.this.font, RealmsUtil.convertToAgePresentationFromInstant(param1.date), param2 + 38, param3 + 24, 7105644, false
            );
            RowButton.drawButtonsInRow(param0, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, param2, param3, param4, param5);
            RealmsUtil.renderPlayerFace(param0, param2, param3, 32, param1.worldOwnerUuid);
        }

        @Override
        public Component getNarration() {
            Component var0 = CommonComponents.joinLines(
                Component.literal(this.pendingInvite.worldName),
                Component.literal(this.pendingInvite.worldOwnerName),
                RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date)
            );
            return Component.translatable("narrator.select", var0);
        }

        @OnlyIn(Dist.CLIENT)
        class AcceptRowButton extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(GuiGraphics param0, int param1, int param2, boolean param3) {
                param0.blitSprite(
                    param3 ? RealmsPendingInvitesScreen.ACCEPT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.ACCEPT_SPRITE, param1, param2, 18, 18
                );
                if (param3) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.ACCEPT_INVITE;
                }

            }

            @Override
            public void onClick(int param0) {
                RealmsPendingInvitesScreen.this.handleInvitation(param0, true);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RejectRowButton extends RowButton {
            RejectRowButton() {
                super(15, 15, 235, 5);
            }

            @Override
            protected void draw(GuiGraphics param0, int param1, int param2, boolean param3) {
                param0.blitSprite(
                    param3 ? RealmsPendingInvitesScreen.REJECT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.REJECT_SPRITE, param1, param2, 18, 18
                );
                if (param3) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.REJECT_INVITE;
                }

            }

            @Override
            public void onClick(int param0) {
                RealmsPendingInvitesScreen.this.handleInvitation(param0, false);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height - 72, 32, 36);
        }

        public void removeAtIndex(int param0) {
            this.remove(param0);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        public void selectItem(int param0) {
            super.selectItem(param0);
            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPendingInvitesScreen.this.selectedInvite = param0;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsPendingInvitesScreen.Entry param0) {
            super.setSelected(param0);
            RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(param0);
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }
}
