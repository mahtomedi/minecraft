package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockEditScreen extends Screen {
    private final StructureBlockEntity structure;
    private Mirror initialMirror = Mirror.NONE;
    private Rotation initialRotation = Rotation.NONE;
    private StructureMode initialMode = StructureMode.DATA;
    private boolean initialEntityIgnoring;
    private boolean initialShowAir;
    private boolean initialShowBoundingBox;
    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private EditBox integrityEdit;
    private EditBox seedEdit;
    private EditBox dataEdit;
    private Button doneButton;
    private Button cancelButton;
    private Button saveButton;
    private Button loadButton;
    private Button rot0Button;
    private Button rot90Button;
    private Button rot180Button;
    private Button rot270Button;
    private Button modeButton;
    private Button detectButton;
    private Button entitiesButton;
    private Button mirrorButton;
    private Button toggleAirButton;
    private Button toggleBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public StructureBlockEditScreen(StructureBlockEntity param0) {
        super(new TranslatableComponent(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.structure = param0;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.posXEdit.tick();
        this.posYEdit.tick();
        this.posZEdit.tick();
        this.sizeXEdit.tick();
        this.sizeYEdit.tick();
        this.sizeZEdit.tick();
        this.integrityEdit.tick();
        this.seedEdit.tick();
        this.dataEdit.tick();
    }

    private void onDone() {
        if (this.sendToServer(StructureBlockEntity.UpdateType.UPDATE_DATA)) {
            this.minecraft.setScreen(null);
        }

    }

    private void onCancel() {
        this.structure.setMirror(this.initialMirror);
        this.structure.setRotation(this.initialRotation);
        this.structure.setMode(this.initialMode);
        this.structure.setIgnoreEntities(this.initialEntityIgnoring);
        this.structure.setShowAir(this.initialShowAir);
        this.structure.setShowBoundingBox(this.initialShowBoundingBox);
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, CommonComponents.GUI_DONE, param0 -> this.onDone()));
        this.cancelButton = this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.onCancel()));
        this.saveButton = this.addButton(
            new Button(this.width / 2 + 4 + 100, 185, 50, 20, new TranslatableComponent("structure_block.button.save"), param0 -> {
                if (this.structure.getMode() == StructureMode.SAVE) {
                    this.sendToServer(StructureBlockEntity.UpdateType.SAVE_AREA);
                    this.minecraft.setScreen(null);
                }
    
            })
        );
        this.loadButton = this.addButton(
            new Button(this.width / 2 + 4 + 100, 185, 50, 20, new TranslatableComponent("structure_block.button.load"), param0 -> {
                if (this.structure.getMode() == StructureMode.LOAD) {
                    this.sendToServer(StructureBlockEntity.UpdateType.LOAD_AREA);
                    this.minecraft.setScreen(null);
                }
    
            })
        );
        this.modeButton = this.addButton(new Button(this.width / 2 - 4 - 150, 185, 50, 20, new TextComponent("MODE"), param0 -> {
            this.structure.nextMode();
            this.updateMode();
        }));
        this.detectButton = this.addButton(
            new Button(this.width / 2 + 4 + 100, 120, 50, 20, new TranslatableComponent("structure_block.button.detect_size"), param0 -> {
                if (this.structure.getMode() == StructureMode.SAVE) {
                    this.sendToServer(StructureBlockEntity.UpdateType.SCAN_AREA);
                    this.minecraft.setScreen(null);
                }
    
            })
        );
        this.entitiesButton = this.addButton(new Button(this.width / 2 + 4 + 100, 160, 50, 20, new TextComponent("ENTITIES"), param0 -> {
            this.structure.setIgnoreEntities(!this.structure.isIgnoreEntities());
            this.updateEntitiesButton();
        }));
        this.mirrorButton = this.addButton(new Button(this.width / 2 - 20, 185, 40, 20, new TextComponent("MIRROR"), param0 -> {
            switch(this.structure.getMirror()) {
                case NONE:
                    this.structure.setMirror(Mirror.LEFT_RIGHT);
                    break;
                case LEFT_RIGHT:
                    this.structure.setMirror(Mirror.FRONT_BACK);
                    break;
                case FRONT_BACK:
                    this.structure.setMirror(Mirror.NONE);
            }

            this.updateMirrorButton();
        }));
        this.toggleAirButton = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, new TextComponent("SHOWAIR"), param0 -> {
            this.structure.setShowAir(!this.structure.getShowAir());
            this.updateToggleAirButton();
        }));
        this.toggleBoundingBox = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, new TextComponent("SHOWBB"), param0 -> {
            this.structure.setShowBoundingBox(!this.structure.getShowBoundingBox());
            this.updateToggleBoundingBox();
        }));
        this.rot0Button = this.addButton(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20, new TextComponent("0"), param0 -> {
            this.structure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }));
        this.rot90Button = this.addButton(new Button(this.width / 2 - 1 - 40 - 20, 185, 40, 20, new TextComponent("90"), param0 -> {
            this.structure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.rot180Button = this.addButton(new Button(this.width / 2 + 1 + 20, 185, 40, 20, new TextComponent("180"), param0 -> {
            this.structure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }));
        this.rot270Button = this.addButton(new Button(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20, new TextComponent("270"), param0 -> {
            this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, new TranslatableComponent("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char param0, int param1) {
                return !StructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), param0, this.getCursorPosition())
                    ? false
                    : super.charTyped(param0, param1);
            }
        };
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.structure.getStructureName());
        this.children.add(this.nameEdit);
        BlockPos var0 = this.structure.getStructurePos();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, new TranslatableComponent("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(var0.getX()));
        this.children.add(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, new TranslatableComponent("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(var0.getY()));
        this.children.add(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, new TranslatableComponent("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(var0.getZ()));
        this.children.add(this.posZEdit);
        BlockPos var1 = this.structure.getStructureSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, new TranslatableComponent("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(var1.getX()));
        this.children.add(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, new TranslatableComponent("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(var1.getY()));
        this.children.add(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, new TranslatableComponent("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(var1.getZ()));
        this.children.add(this.sizeZEdit);
        this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, new TranslatableComponent("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format((double)this.structure.getIntegrity()));
        this.children.add(this.integrityEdit);
        this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, new TranslatableComponent("structure_block.integrity.seed"));
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
        this.children.add(this.seedEdit);
        this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, new TranslatableComponent("structure_block.custom_data"));
        this.dataEdit.setMaxLength(128);
        this.dataEdit.setValue(this.structure.getMetaData());
        this.children.add(this.dataEdit);
        this.initialMirror = this.structure.getMirror();
        this.updateMirrorButton();
        this.initialRotation = this.structure.getRotation();
        this.updateDirectionButtons();
        this.initialMode = this.structure.getMode();
        this.updateMode();
        this.initialEntityIgnoring = this.structure.isIgnoreEntities();
        this.updateEntitiesButton();
        this.initialShowAir = this.structure.getShowAir();
        this.updateToggleAirButton();
        this.initialShowBoundingBox = this.structure.getShowBoundingBox();
        this.updateToggleBoundingBox();
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.nameEdit.getValue();
        String var1 = this.posXEdit.getValue();
        String var2 = this.posYEdit.getValue();
        String var3 = this.posZEdit.getValue();
        String var4 = this.sizeXEdit.getValue();
        String var5 = this.sizeYEdit.getValue();
        String var6 = this.sizeZEdit.getValue();
        String var7 = this.integrityEdit.getValue();
        String var8 = this.seedEdit.getValue();
        String var9 = this.dataEdit.getValue();
        this.init(param0, param1, param2);
        this.nameEdit.setValue(var0);
        this.posXEdit.setValue(var1);
        this.posYEdit.setValue(var2);
        this.posZEdit.setValue(var3);
        this.sizeXEdit.setValue(var4);
        this.sizeYEdit.setValue(var5);
        this.sizeZEdit.setValue(var6);
        this.integrityEdit.setValue(var7);
        this.seedEdit.setValue(var8);
        this.dataEdit.setValue(var9);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void updateEntitiesButton() {
        this.entitiesButton.setMessage(CommonComponents.optionStatus(!this.structure.isIgnoreEntities()));
    }

    private void updateToggleAirButton() {
        this.toggleAirButton.setMessage(CommonComponents.optionStatus(this.structure.getShowAir()));
    }

    private void updateToggleBoundingBox() {
        this.toggleBoundingBox.setMessage(CommonComponents.optionStatus(this.structure.getShowBoundingBox()));
    }

    private void updateMirrorButton() {
        Mirror var0 = this.structure.getMirror();
        switch(var0) {
            case NONE:
                this.mirrorButton.setMessage(new TextComponent("|"));
                break;
            case LEFT_RIGHT:
                this.mirrorButton.setMessage(new TextComponent("< >"));
                break;
            case FRONT_BACK:
                this.mirrorButton.setMessage(new TextComponent("^ v"));
        }

    }

    private void updateDirectionButtons() {
        this.rot0Button.active = true;
        this.rot90Button.active = true;
        this.rot180Button.active = true;
        this.rot270Button.active = true;
        switch(this.structure.getRotation()) {
            case NONE:
                this.rot0Button.active = false;
                break;
            case CLOCKWISE_180:
                this.rot180Button.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                this.rot270Button.active = false;
                break;
            case CLOCKWISE_90:
                this.rot90Button.active = false;
        }

    }

    private void updateMode() {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.dataEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectButton.visible = false;
        this.entitiesButton.visible = false;
        this.mirrorButton.visible = false;
        this.rot0Button.visible = false;
        this.rot90Button.visible = false;
        this.rot180Button.visible = false;
        this.rot270Button.visible = false;
        this.toggleAirButton.visible = false;
        this.toggleBoundingBox.visible = false;
        switch(this.structure.getMode()) {
            case SAVE:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectButton.visible = true;
                this.entitiesButton.visible = true;
                this.toggleAirButton.visible = true;
                break;
            case LOAD:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.entitiesButton.visible = true;
                this.mirrorButton.visible = true;
                this.rot0Button.visible = true;
                this.rot90Button.visible = true;
                this.rot180Button.visible = true;
                this.rot270Button.visible = true;
                this.toggleBoundingBox.visible = true;
                this.updateDirectionButtons();
                break;
            case CORNER:
                this.nameEdit.setVisible(true);
                break;
            case DATA:
                this.dataEdit.setVisible(true);
        }

        this.modeButton.setMessage(new TranslatableComponent("structure_block.mode." + this.structure.getMode().getSerializedName()));
    }

    private boolean sendToServer(StructureBlockEntity.UpdateType param0) {
        BlockPos var0 = new BlockPos(
            this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue())
        );
        BlockPos var1 = new BlockPos(
            this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue())
        );
        float var2 = this.parseIntegrity(this.integrityEdit.getValue());
        long var3 = this.parseSeed(this.seedEdit.getValue());
        this.minecraft
            .getConnection()
            .send(
                new ServerboundSetStructureBlockPacket(
                    this.structure.getBlockPos(),
                    param0,
                    this.structure.getMode(),
                    this.nameEdit.getValue(),
                    var0,
                    var1,
                    this.structure.getMirror(),
                    this.structure.getRotation(),
                    this.dataEdit.getValue(),
                    this.structure.isIgnoreEntities(),
                    this.structure.getShowAir(),
                    this.structure.getShowBoundingBox(),
                    var2,
                    var3
                )
            );
        return true;
    }

    private long parseSeed(String param0) {
        try {
            return Long.valueOf(param0);
        } catch (NumberFormatException var3) {
            return 0L;
        }
    }

    private float parseIntegrity(String param0) {
        try {
            return Float.valueOf(param0);
        } catch (NumberFormatException var3) {
            return 1.0F;
        }
    }

    private int parseCoordinate(String param0) {
        try {
            return Integer.parseInt(param0);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.onDone();
            return true;
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        StructureMode var0 = this.structure.getMode();
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 10, 16777215);
        if (var0 != StructureMode.DATA) {
            this.drawString(param0, this.font, I18n.get("structure_block.structure_name"), this.width / 2 - 153, 30, 10526880);
            this.nameEdit.render(param0, param1, param2, param3);
        }

        if (var0 == StructureMode.LOAD || var0 == StructureMode.SAVE) {
            this.drawString(param0, this.font, I18n.get("structure_block.position"), this.width / 2 - 153, 70, 10526880);
            this.posXEdit.render(param0, param1, param2, param3);
            this.posYEdit.render(param0, param1, param2, param3);
            this.posZEdit.render(param0, param1, param2, param3);
            String var1 = I18n.get("structure_block.include_entities");
            int var2 = this.font.width(var1);
            this.drawString(param0, this.font, var1, this.width / 2 + 154 - var2, 150, 10526880);
        }

        if (var0 == StructureMode.SAVE) {
            this.drawString(param0, this.font, I18n.get("structure_block.size"), this.width / 2 - 153, 110, 10526880);
            this.sizeXEdit.render(param0, param1, param2, param3);
            this.sizeYEdit.render(param0, param1, param2, param3);
            this.sizeZEdit.render(param0, param1, param2, param3);
            String var3 = I18n.get("structure_block.detect_size");
            int var4 = this.font.width(var3);
            this.drawString(param0, this.font, var3, this.width / 2 + 154 - var4, 110, 10526880);
            String var5 = I18n.get("structure_block.show_air");
            int var6 = this.font.width(var5);
            this.drawString(param0, this.font, var5, this.width / 2 + 154 - var6, 70, 10526880);
        }

        if (var0 == StructureMode.LOAD) {
            this.drawString(param0, this.font, I18n.get("structure_block.integrity"), this.width / 2 - 153, 110, 10526880);
            this.integrityEdit.render(param0, param1, param2, param3);
            this.seedEdit.render(param0, param1, param2, param3);
            String var7 = I18n.get("structure_block.show_boundingbox");
            int var8 = this.font.width(var7);
            this.drawString(param0, this.font, var7, this.width / 2 + 154 - var8, 70, 10526880);
        }

        if (var0 == StructureMode.DATA) {
            this.drawString(param0, this.font, I18n.get("structure_block.custom_data"), this.width / 2 - 153, 110, 10526880);
            this.dataEdit.render(param0, param1, param2, param3);
        }

        String var9 = "structure_block.mode_info." + var0.getSerializedName();
        this.drawString(param0, this.font, I18n.get(var9), this.width / 2 - 153, 174, 10526880);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
