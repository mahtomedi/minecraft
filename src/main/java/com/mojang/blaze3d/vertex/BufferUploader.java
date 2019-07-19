package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
    public void end(BufferBuilder param0) {
        if (param0.getVertexCount() > 0) {
            VertexFormat var0 = param0.getVertexFormat();
            int var1 = var0.getVertexSize();
            ByteBuffer var2 = param0.getBuffer();
            List<VertexFormatElement> var3 = var0.getElements();

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                VertexFormatElement var5 = var3.get(var4);
                VertexFormatElement.Usage var6 = var5.getUsage();
                int var7 = var5.getType().getGlType();
                int var8 = var5.getIndex();
                ((Buffer)var2).position(var0.getOffset(var4));
                switch(var6) {
                    case POSITION:
                        GlStateManager.vertexPointer(var5.getCount(), var7, var1, var2);
                        GlStateManager.enableClientState(32884);
                        break;
                    case UV:
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0 + var8);
                        GlStateManager.texCoordPointer(var5.getCount(), var7, var1, var2);
                        GlStateManager.enableClientState(32888);
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
                        break;
                    case COLOR:
                        GlStateManager.colorPointer(var5.getCount(), var7, var1, var2);
                        GlStateManager.enableClientState(32886);
                        break;
                    case NORMAL:
                        GlStateManager.normalPointer(var7, var1, var2);
                        GlStateManager.enableClientState(32885);
                }
            }

            GlStateManager.drawArrays(param0.getDrawMode(), 0, param0.getVertexCount());
            int var9 = 0;

            for(int var10 = var3.size(); var9 < var10; ++var9) {
                VertexFormatElement var11 = var3.get(var9);
                VertexFormatElement.Usage var12 = var11.getUsage();
                int var13 = var11.getIndex();
                switch(var12) {
                    case POSITION:
                        GlStateManager.disableClientState(32884);
                        break;
                    case UV:
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0 + var13);
                        GlStateManager.disableClientState(32888);
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
                        break;
                    case COLOR:
                        GlStateManager.disableClientState(32886);
                        GlStateManager.clearCurrentColor();
                        break;
                    case NORMAL:
                        GlStateManager.disableClientState(32885);
                }
            }
        }

        param0.clear();
    }
}
