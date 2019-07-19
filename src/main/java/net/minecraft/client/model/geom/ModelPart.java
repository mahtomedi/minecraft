package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelPart {
    public float xTexSize = 64.0F;
    public float yTexSize = 32.0F;
    private int xTexOffs;
    private int yTexOffs;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    private boolean compiled;
    private int list;
    public boolean mirror;
    public boolean visible = true;
    public boolean neverRender;
    public final List<Cube> cubes = Lists.newArrayList();
    public List<ModelPart> children;
    public final String id;
    public float translateX;
    public float translateY;
    public float translateZ;

    public ModelPart(Model param0, String param1) {
        param0.cubes.add(this);
        this.id = param1;
        this.setTexSize(param0.texWidth, param0.texHeight);
    }

    public ModelPart(Model param0) {
        this(param0, null);
    }

    public ModelPart(Model param0, int param1, int param2) {
        this(param0);
        this.texOffs(param1, param2);
    }

    public void copyFrom(ModelPart param0) {
        this.xRot = param0.xRot;
        this.yRot = param0.yRot;
        this.zRot = param0.zRot;
        this.x = param0.x;
        this.y = param0.y;
        this.z = param0.z;
    }

    public void addChild(ModelPart param0) {
        if (this.children == null) {
            this.children = Lists.newArrayList();
        }

        this.children.add(param0);
    }

    public void removeChild(ModelPart param0) {
        if (this.children != null) {
            this.children.remove(param0);
        }

    }

    public ModelPart texOffs(int param0, int param1) {
        this.xTexOffs = param0;
        this.yTexOffs = param1;
        return this;
    }

    public ModelPart addBox(String param0, float param1, float param2, float param3, int param4, int param5, int param6, float param7, int param8, int param9) {
        param0 = this.id + "." + param0;
        this.texOffs(param8, param9);
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, param1, param2, param3, param4, param5, param6, param7).setId(param0));
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, int param3, int param4, int param5) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F));
        return this;
    }

    public ModelPart addBox(float param0, float param1, float param2, int param3, int param4, int param5, boolean param6) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, 0.0F, param6));
        return this;
    }

    public void addBox(float param0, float param1, float param2, int param3, int param4, int param5, float param6) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6));
    }

    public void addBox(float param0, float param1, float param2, int param3, int param4, int param5, float param6, boolean param7) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, param0, param1, param2, param3, param4, param5, param6, param7));
    }

    public void setPos(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public void render(float param0) {
        if (!this.neverRender) {
            if (this.visible) {
                if (!this.compiled) {
                    this.compile(param0);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translatef(this.translateX, this.translateY, this.translateZ);
                if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(this.x * param0, this.y * param0, this.z * param0);
                    if (this.zRot != 0.0F) {
                        GlStateManager.rotatef(this.zRot * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.yRot != 0.0F) {
                        GlStateManager.rotatef(this.yRot * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.xRot != 0.0F) {
                        GlStateManager.rotatef(this.xRot * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }

                    GlStateManager.callList(this.list);
                    if (this.children != null) {
                        for(int var0 = 0; var0 < this.children.size(); ++var0) {
                            this.children.get(var0).render(param0);
                        }
                    }

                    GlStateManager.popMatrix();
                } else if (this.x == 0.0F && this.y == 0.0F && this.z == 0.0F) {
                    GlStateManager.callList(this.list);
                    if (this.children != null) {
                        for(int var2 = 0; var2 < this.children.size(); ++var2) {
                            this.children.get(var2).render(param0);
                        }
                    }
                } else {
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(this.x * param0, this.y * param0, this.z * param0);
                    GlStateManager.callList(this.list);
                    if (this.children != null) {
                        for(int var1 = 0; var1 < this.children.size(); ++var1) {
                            this.children.get(var1).render(param0);
                        }
                    }

                    GlStateManager.popMatrix();
                }

                GlStateManager.popMatrix();
            }
        }
    }

    public void renderRollable(float param0) {
        if (!this.neverRender) {
            if (this.visible) {
                if (!this.compiled) {
                    this.compile(param0);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translatef(this.x * param0, this.y * param0, this.z * param0);
                if (this.yRot != 0.0F) {
                    GlStateManager.rotatef(this.yRot * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.xRot != 0.0F) {
                    GlStateManager.rotatef(this.xRot * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }

                if (this.zRot != 0.0F) {
                    GlStateManager.rotatef(this.zRot * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                GlStateManager.callList(this.list);
                GlStateManager.popMatrix();
            }
        }
    }

    public void translateTo(float param0) {
        if (!this.neverRender) {
            if (this.visible) {
                if (!this.compiled) {
                    this.compile(param0);
                }

                if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
                    GlStateManager.translatef(this.x * param0, this.y * param0, this.z * param0);
                    if (this.zRot != 0.0F) {
                        GlStateManager.rotatef(this.zRot * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.yRot != 0.0F) {
                        GlStateManager.rotatef(this.yRot * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.xRot != 0.0F) {
                        GlStateManager.rotatef(this.xRot * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }
                } else if (this.x != 0.0F || this.y != 0.0F || this.z != 0.0F) {
                    GlStateManager.translatef(this.x * param0, this.y * param0, this.z * param0);
                }

            }
        }
    }

    private void compile(float param0) {
        this.list = MemoryTracker.genLists(1);
        GlStateManager.newList(this.list, 4864);
        BufferBuilder var0 = Tesselator.getInstance().getBuilder();

        for(int var1 = 0; var1 < this.cubes.size(); ++var1) {
            this.cubes.get(var1).compile(var0, param0);
        }

        GlStateManager.endList();
        this.compiled = true;
    }

    public ModelPart setTexSize(int param0, int param1) {
        this.xTexSize = (float)param0;
        this.yTexSize = (float)param1;
        return this;
    }
}
