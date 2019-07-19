package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import oshi.SystemInfo;
import oshi.hardware.Processor;

@OnlyIn(Dist.CLIENT)
public class GLX {
    private static final Logger LOGGER = LogManager.getLogger();
    public static boolean isNvidia;
    public static boolean isAmd;
    public static int GL_FRAMEBUFFER;
    public static int GL_RENDERBUFFER;
    public static int GL_COLOR_ATTACHMENT0;
    public static int GL_DEPTH_ATTACHMENT;
    public static int GL_FRAMEBUFFER_COMPLETE;
    public static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
    public static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
    public static int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
    public static int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
    private static GLX.FboMode fboMode;
    public static final boolean useFbo = true;
    private static boolean hasShaders;
    private static boolean useShaderArb;
    public static int GL_LINK_STATUS;
    public static int GL_COMPILE_STATUS;
    public static int GL_VERTEX_SHADER;
    public static int GL_FRAGMENT_SHADER;
    private static boolean useMultitextureArb;
    public static int GL_TEXTURE0;
    public static int GL_TEXTURE1;
    public static int GL_TEXTURE2;
    private static boolean useTexEnvCombineArb;
    public static int GL_COMBINE;
    public static int GL_INTERPOLATE;
    public static int GL_PRIMARY_COLOR;
    public static int GL_CONSTANT;
    public static int GL_PREVIOUS;
    public static int GL_COMBINE_RGB;
    public static int GL_SOURCE0_RGB;
    public static int GL_SOURCE1_RGB;
    public static int GL_SOURCE2_RGB;
    public static int GL_OPERAND0_RGB;
    public static int GL_OPERAND1_RGB;
    public static int GL_OPERAND2_RGB;
    public static int GL_COMBINE_ALPHA;
    public static int GL_SOURCE0_ALPHA;
    public static int GL_SOURCE1_ALPHA;
    public static int GL_SOURCE2_ALPHA;
    public static int GL_OPERAND0_ALPHA;
    public static int GL_OPERAND1_ALPHA;
    public static int GL_OPERAND2_ALPHA;
    private static boolean separateBlend;
    public static boolean useSeparateBlendExt;
    public static boolean isOpenGl21;
    public static boolean usePostProcess;
    private static String capsString = "";
    private static String cpuInfo;
    public static final boolean useVbo = true;
    public static boolean needVbo;
    private static boolean useVboArb;
    public static int GL_ARRAY_BUFFER;
    public static int GL_STATIC_DRAW;
    private static final Map<Integer, String> LOOKUP_MAP = make(Maps.newHashMap(), param0 -> {
        param0.put(0, "No error");
        param0.put(1280, "Enum parameter is invalid for this function");
        param0.put(1281, "Parameter is invalid for this function");
        param0.put(1282, "Current state is invalid for this function");
        param0.put(1283, "Stack overflow");
        param0.put(1284, "Stack underflow");
        param0.put(1285, "Out of memory");
        param0.put(1286, "Operation on incomplete framebuffer");
        param0.put(1286, "Operation on incomplete framebuffer");
    });

    public static void populateSnooperWithOpenGL(SnooperAccess param0) {
        param0.setFixedData("opengl_version", GlStateManager.getString(7938));
        param0.setFixedData("opengl_vendor", GlStateManager.getString(7936));
        GLCapabilities var0 = GL.getCapabilities();
        param0.setFixedData("gl_caps[ARB_arrays_of_arrays]", var0.GL_ARB_arrays_of_arrays);
        param0.setFixedData("gl_caps[ARB_base_instance]", var0.GL_ARB_base_instance);
        param0.setFixedData("gl_caps[ARB_blend_func_extended]", var0.GL_ARB_blend_func_extended);
        param0.setFixedData("gl_caps[ARB_clear_buffer_object]", var0.GL_ARB_clear_buffer_object);
        param0.setFixedData("gl_caps[ARB_color_buffer_float]", var0.GL_ARB_color_buffer_float);
        param0.setFixedData("gl_caps[ARB_compatibility]", var0.GL_ARB_compatibility);
        param0.setFixedData("gl_caps[ARB_compressed_texture_pixel_storage]", var0.GL_ARB_compressed_texture_pixel_storage);
        param0.setFixedData("gl_caps[ARB_compute_shader]", var0.GL_ARB_compute_shader);
        param0.setFixedData("gl_caps[ARB_copy_buffer]", var0.GL_ARB_copy_buffer);
        param0.setFixedData("gl_caps[ARB_copy_image]", var0.GL_ARB_copy_image);
        param0.setFixedData("gl_caps[ARB_depth_buffer_float]", var0.GL_ARB_depth_buffer_float);
        param0.setFixedData("gl_caps[ARB_compute_shader]", var0.GL_ARB_compute_shader);
        param0.setFixedData("gl_caps[ARB_copy_buffer]", var0.GL_ARB_copy_buffer);
        param0.setFixedData("gl_caps[ARB_copy_image]", var0.GL_ARB_copy_image);
        param0.setFixedData("gl_caps[ARB_depth_buffer_float]", var0.GL_ARB_depth_buffer_float);
        param0.setFixedData("gl_caps[ARB_depth_clamp]", var0.GL_ARB_depth_clamp);
        param0.setFixedData("gl_caps[ARB_depth_texture]", var0.GL_ARB_depth_texture);
        param0.setFixedData("gl_caps[ARB_draw_buffers]", var0.GL_ARB_draw_buffers);
        param0.setFixedData("gl_caps[ARB_draw_buffers_blend]", var0.GL_ARB_draw_buffers_blend);
        param0.setFixedData("gl_caps[ARB_draw_elements_base_vertex]", var0.GL_ARB_draw_elements_base_vertex);
        param0.setFixedData("gl_caps[ARB_draw_indirect]", var0.GL_ARB_draw_indirect);
        param0.setFixedData("gl_caps[ARB_draw_instanced]", var0.GL_ARB_draw_instanced);
        param0.setFixedData("gl_caps[ARB_explicit_attrib_location]", var0.GL_ARB_explicit_attrib_location);
        param0.setFixedData("gl_caps[ARB_explicit_uniform_location]", var0.GL_ARB_explicit_uniform_location);
        param0.setFixedData("gl_caps[ARB_fragment_layer_viewport]", var0.GL_ARB_fragment_layer_viewport);
        param0.setFixedData("gl_caps[ARB_fragment_program]", var0.GL_ARB_fragment_program);
        param0.setFixedData("gl_caps[ARB_fragment_shader]", var0.GL_ARB_fragment_shader);
        param0.setFixedData("gl_caps[ARB_fragment_program_shadow]", var0.GL_ARB_fragment_program_shadow);
        param0.setFixedData("gl_caps[ARB_framebuffer_object]", var0.GL_ARB_framebuffer_object);
        param0.setFixedData("gl_caps[ARB_framebuffer_sRGB]", var0.GL_ARB_framebuffer_sRGB);
        param0.setFixedData("gl_caps[ARB_geometry_shader4]", var0.GL_ARB_geometry_shader4);
        param0.setFixedData("gl_caps[ARB_gpu_shader5]", var0.GL_ARB_gpu_shader5);
        param0.setFixedData("gl_caps[ARB_half_float_pixel]", var0.GL_ARB_half_float_pixel);
        param0.setFixedData("gl_caps[ARB_half_float_vertex]", var0.GL_ARB_half_float_vertex);
        param0.setFixedData("gl_caps[ARB_instanced_arrays]", var0.GL_ARB_instanced_arrays);
        param0.setFixedData("gl_caps[ARB_map_buffer_alignment]", var0.GL_ARB_map_buffer_alignment);
        param0.setFixedData("gl_caps[ARB_map_buffer_range]", var0.GL_ARB_map_buffer_range);
        param0.setFixedData("gl_caps[ARB_multisample]", var0.GL_ARB_multisample);
        param0.setFixedData("gl_caps[ARB_multitexture]", var0.GL_ARB_multitexture);
        param0.setFixedData("gl_caps[ARB_occlusion_query2]", var0.GL_ARB_occlusion_query2);
        param0.setFixedData("gl_caps[ARB_pixel_buffer_object]", var0.GL_ARB_pixel_buffer_object);
        param0.setFixedData("gl_caps[ARB_seamless_cube_map]", var0.GL_ARB_seamless_cube_map);
        param0.setFixedData("gl_caps[ARB_shader_objects]", var0.GL_ARB_shader_objects);
        param0.setFixedData("gl_caps[ARB_shader_stencil_export]", var0.GL_ARB_shader_stencil_export);
        param0.setFixedData("gl_caps[ARB_shader_texture_lod]", var0.GL_ARB_shader_texture_lod);
        param0.setFixedData("gl_caps[ARB_shadow]", var0.GL_ARB_shadow);
        param0.setFixedData("gl_caps[ARB_shadow_ambient]", var0.GL_ARB_shadow_ambient);
        param0.setFixedData("gl_caps[ARB_stencil_texturing]", var0.GL_ARB_stencil_texturing);
        param0.setFixedData("gl_caps[ARB_sync]", var0.GL_ARB_sync);
        param0.setFixedData("gl_caps[ARB_tessellation_shader]", var0.GL_ARB_tessellation_shader);
        param0.setFixedData("gl_caps[ARB_texture_border_clamp]", var0.GL_ARB_texture_border_clamp);
        param0.setFixedData("gl_caps[ARB_texture_buffer_object]", var0.GL_ARB_texture_buffer_object);
        param0.setFixedData("gl_caps[ARB_texture_cube_map]", var0.GL_ARB_texture_cube_map);
        param0.setFixedData("gl_caps[ARB_texture_cube_map_array]", var0.GL_ARB_texture_cube_map_array);
        param0.setFixedData("gl_caps[ARB_texture_non_power_of_two]", var0.GL_ARB_texture_non_power_of_two);
        param0.setFixedData("gl_caps[ARB_uniform_buffer_object]", var0.GL_ARB_uniform_buffer_object);
        param0.setFixedData("gl_caps[ARB_vertex_blend]", var0.GL_ARB_vertex_blend);
        param0.setFixedData("gl_caps[ARB_vertex_buffer_object]", var0.GL_ARB_vertex_buffer_object);
        param0.setFixedData("gl_caps[ARB_vertex_program]", var0.GL_ARB_vertex_program);
        param0.setFixedData("gl_caps[ARB_vertex_shader]", var0.GL_ARB_vertex_shader);
        param0.setFixedData("gl_caps[EXT_bindable_uniform]", var0.GL_EXT_bindable_uniform);
        param0.setFixedData("gl_caps[EXT_blend_equation_separate]", var0.GL_EXT_blend_equation_separate);
        param0.setFixedData("gl_caps[EXT_blend_func_separate]", var0.GL_EXT_blend_func_separate);
        param0.setFixedData("gl_caps[EXT_blend_minmax]", var0.GL_EXT_blend_minmax);
        param0.setFixedData("gl_caps[EXT_blend_subtract]", var0.GL_EXT_blend_subtract);
        param0.setFixedData("gl_caps[EXT_draw_instanced]", var0.GL_EXT_draw_instanced);
        param0.setFixedData("gl_caps[EXT_framebuffer_multisample]", var0.GL_EXT_framebuffer_multisample);
        param0.setFixedData("gl_caps[EXT_framebuffer_object]", var0.GL_EXT_framebuffer_object);
        param0.setFixedData("gl_caps[EXT_framebuffer_sRGB]", var0.GL_EXT_framebuffer_sRGB);
        param0.setFixedData("gl_caps[EXT_geometry_shader4]", var0.GL_EXT_geometry_shader4);
        param0.setFixedData("gl_caps[EXT_gpu_program_parameters]", var0.GL_EXT_gpu_program_parameters);
        param0.setFixedData("gl_caps[EXT_gpu_shader4]", var0.GL_EXT_gpu_shader4);
        param0.setFixedData("gl_caps[EXT_packed_depth_stencil]", var0.GL_EXT_packed_depth_stencil);
        param0.setFixedData("gl_caps[EXT_separate_shader_objects]", var0.GL_EXT_separate_shader_objects);
        param0.setFixedData("gl_caps[EXT_shader_image_load_store]", var0.GL_EXT_shader_image_load_store);
        param0.setFixedData("gl_caps[EXT_shadow_funcs]", var0.GL_EXT_shadow_funcs);
        param0.setFixedData("gl_caps[EXT_shared_texture_palette]", var0.GL_EXT_shared_texture_palette);
        param0.setFixedData("gl_caps[EXT_stencil_clear_tag]", var0.GL_EXT_stencil_clear_tag);
        param0.setFixedData("gl_caps[EXT_stencil_two_side]", var0.GL_EXT_stencil_two_side);
        param0.setFixedData("gl_caps[EXT_stencil_wrap]", var0.GL_EXT_stencil_wrap);
        param0.setFixedData("gl_caps[EXT_texture_array]", var0.GL_EXT_texture_array);
        param0.setFixedData("gl_caps[EXT_texture_buffer_object]", var0.GL_EXT_texture_buffer_object);
        param0.setFixedData("gl_caps[EXT_texture_integer]", var0.GL_EXT_texture_integer);
        param0.setFixedData("gl_caps[EXT_texture_sRGB]", var0.GL_EXT_texture_sRGB);
        param0.setFixedData("gl_caps[ARB_vertex_shader]", var0.GL_ARB_vertex_shader);
        param0.setFixedData("gl_caps[gl_max_vertex_uniforms]", GlStateManager.getInteger(35658));
        GlStateManager.getError();
        param0.setFixedData("gl_caps[gl_max_fragment_uniforms]", GlStateManager.getInteger(35657));
        GlStateManager.getError();
        param0.setFixedData("gl_caps[gl_max_vertex_attribs]", GlStateManager.getInteger(34921));
        GlStateManager.getError();
        param0.setFixedData("gl_caps[gl_max_vertex_texture_image_units]", GlStateManager.getInteger(35660));
        GlStateManager.getError();
        param0.setFixedData("gl_caps[gl_max_texture_image_units]", GlStateManager.getInteger(34930));
        GlStateManager.getError();
        param0.setFixedData("gl_caps[gl_max_array_texture_layers]", GlStateManager.getInteger(35071));
        GlStateManager.getError();
    }

    public static String getOpenGLVersionString() {
        return GLFW.glfwGetCurrentContext() == 0L
            ? "NO CONTEXT"
            : GlStateManager.getString(7937) + " GL version " + GlStateManager.getString(7938) + ", " + GlStateManager.getString(7936);
    }

    public static int getRefreshRate(Window param0) {
        long var0 = GLFW.glfwGetWindowMonitor(param0.getWindow());
        if (var0 == 0L) {
            var0 = GLFW.glfwGetPrimaryMonitor();
        }

        GLFWVidMode var1 = var0 == 0L ? null : GLFW.glfwGetVideoMode(var0);
        return var1 == null ? 0 : var1.refreshRate();
    }

    public static String getLWJGLVersion() {
        return Version.getVersion();
    }

    public static LongSupplier initGlfw() {
        Window.checkGlfwError((param0, param1) -> {
            throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", param0, param1));
        });
        List<String> var0 = Lists.newArrayList();
        GLFWErrorCallback var1 = GLFW.glfwSetErrorCallback((param1, param2) -> var0.add(String.format("GLFW error during init: [0x%X]%s", param1, param2)));
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(var0));
        } else {
            LongSupplier var2 = () -> (long)(GLFW.glfwGetTime() * 1.0E9);

            for(String var3 : var0) {
                LOGGER.error("GLFW error collected during initialization: {}", var3);
            }

            setGlfwErrorCallback(var1);
            return var2;
        }
    }

    public static void setGlfwErrorCallback(GLFWErrorCallbackI param0) {
        GLFW.glfwSetErrorCallback(param0).free();
    }

    public static boolean shouldClose(Window param0) {
        return GLFW.glfwWindowShouldClose(param0.getWindow());
    }

    public static void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public static String getOpenGLVersion() {
        return GlStateManager.getString(7938);
    }

    public static String getRenderer() {
        return GlStateManager.getString(7937);
    }

    public static String getVendor() {
        return GlStateManager.getString(7936);
    }

    public static void setupNvFogDistance() {
        if (GL.getCapabilities().GL_NV_fog_distance) {
            GlStateManager.fogi(34138, 34139);
        }

    }

    public static boolean supportsOpenGL2() {
        return GL.getCapabilities().OpenGL20;
    }

    public static void withTextureRestore(Runnable param0) {
        GL11.glPushAttrib(270336);

        try {
            param0.run();
        } finally {
            GL11.glPopAttrib();
        }

    }

    public static ByteBuffer allocateMemory(int param0) {
        return MemoryUtil.memAlloc(param0);
    }

    public static void freeMemory(Buffer param0) {
        MemoryUtil.memFree(param0);
    }

    public static void init() {
        GLCapabilities var0 = GL.getCapabilities();
        useMultitextureArb = var0.GL_ARB_multitexture && !var0.OpenGL13;
        useTexEnvCombineArb = var0.GL_ARB_texture_env_combine && !var0.OpenGL13;
        if (useMultitextureArb) {
            capsString = capsString + "Using ARB_multitexture.\n";
            GL_TEXTURE0 = 33984;
            GL_TEXTURE1 = 33985;
            GL_TEXTURE2 = 33986;
        } else {
            capsString = capsString + "Using GL 1.3 multitexturing.\n";
            GL_TEXTURE0 = 33984;
            GL_TEXTURE1 = 33985;
            GL_TEXTURE2 = 33986;
        }

        if (useTexEnvCombineArb) {
            capsString = capsString + "Using ARB_texture_env_combine.\n";
            GL_COMBINE = 34160;
            GL_INTERPOLATE = 34165;
            GL_PRIMARY_COLOR = 34167;
            GL_CONSTANT = 34166;
            GL_PREVIOUS = 34168;
            GL_COMBINE_RGB = 34161;
            GL_SOURCE0_RGB = 34176;
            GL_SOURCE1_RGB = 34177;
            GL_SOURCE2_RGB = 34178;
            GL_OPERAND0_RGB = 34192;
            GL_OPERAND1_RGB = 34193;
            GL_OPERAND2_RGB = 34194;
            GL_COMBINE_ALPHA = 34162;
            GL_SOURCE0_ALPHA = 34184;
            GL_SOURCE1_ALPHA = 34185;
            GL_SOURCE2_ALPHA = 34186;
            GL_OPERAND0_ALPHA = 34200;
            GL_OPERAND1_ALPHA = 34201;
            GL_OPERAND2_ALPHA = 34202;
        } else {
            capsString = capsString + "Using GL 1.3 texture combiners.\n";
            GL_COMBINE = 34160;
            GL_INTERPOLATE = 34165;
            GL_PRIMARY_COLOR = 34167;
            GL_CONSTANT = 34166;
            GL_PREVIOUS = 34168;
            GL_COMBINE_RGB = 34161;
            GL_SOURCE0_RGB = 34176;
            GL_SOURCE1_RGB = 34177;
            GL_SOURCE2_RGB = 34178;
            GL_OPERAND0_RGB = 34192;
            GL_OPERAND1_RGB = 34193;
            GL_OPERAND2_RGB = 34194;
            GL_COMBINE_ALPHA = 34162;
            GL_SOURCE0_ALPHA = 34184;
            GL_SOURCE1_ALPHA = 34185;
            GL_SOURCE2_ALPHA = 34186;
            GL_OPERAND0_ALPHA = 34200;
            GL_OPERAND1_ALPHA = 34201;
            GL_OPERAND2_ALPHA = 34202;
        }

        useSeparateBlendExt = var0.GL_EXT_blend_func_separate && !var0.OpenGL14;
        separateBlend = var0.OpenGL14 || var0.GL_EXT_blend_func_separate;
        capsString = capsString + "Using framebuffer objects because ";
        if (var0.OpenGL30) {
            capsString = capsString + "OpenGL 3.0 is supported and separate blending is supported.\n";
            fboMode = GLX.FboMode.BASE;
            GL_FRAMEBUFFER = 36160;
            GL_RENDERBUFFER = 36161;
            GL_COLOR_ATTACHMENT0 = 36064;
            GL_DEPTH_ATTACHMENT = 36096;
            GL_FRAMEBUFFER_COMPLETE = 36053;
            GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
        } else if (var0.GL_ARB_framebuffer_object) {
            capsString = capsString + "ARB_framebuffer_object is supported and separate blending is supported.\n";
            fboMode = GLX.FboMode.ARB;
            GL_FRAMEBUFFER = 36160;
            GL_RENDERBUFFER = 36161;
            GL_COLOR_ATTACHMENT0 = 36064;
            GL_DEPTH_ATTACHMENT = 36096;
            GL_FRAMEBUFFER_COMPLETE = 36053;
            GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
        } else {
            if (!var0.GL_EXT_framebuffer_object) {
                throw new IllegalStateException("The driver does not appear to support framebuffer objects");
            }

            capsString = capsString + "EXT_framebuffer_object is supported.\n";
            fboMode = GLX.FboMode.EXT;
            GL_FRAMEBUFFER = 36160;
            GL_RENDERBUFFER = 36161;
            GL_COLOR_ATTACHMENT0 = 36064;
            GL_DEPTH_ATTACHMENT = 36096;
            GL_FRAMEBUFFER_COMPLETE = 36053;
            GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
        }

        isOpenGl21 = var0.OpenGL21;
        hasShaders = isOpenGl21 || var0.GL_ARB_vertex_shader && var0.GL_ARB_fragment_shader && var0.GL_ARB_shader_objects;
        capsString = capsString + "Shaders are " + (hasShaders ? "" : "not ") + "available because ";
        if (hasShaders) {
            if (var0.OpenGL21) {
                capsString = capsString + "OpenGL 2.1 is supported.\n";
                useShaderArb = false;
                GL_LINK_STATUS = 35714;
                GL_COMPILE_STATUS = 35713;
                GL_VERTEX_SHADER = 35633;
                GL_FRAGMENT_SHADER = 35632;
            } else {
                capsString = capsString + "ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported.\n";
                useShaderArb = true;
                GL_LINK_STATUS = 35714;
                GL_COMPILE_STATUS = 35713;
                GL_VERTEX_SHADER = 35633;
                GL_FRAGMENT_SHADER = 35632;
            }
        } else {
            capsString = capsString + "OpenGL 2.1 is " + (var0.OpenGL21 ? "" : "not ") + "supported, ";
            capsString = capsString + "ARB_shader_objects is " + (var0.GL_ARB_shader_objects ? "" : "not ") + "supported, ";
            capsString = capsString + "ARB_vertex_shader is " + (var0.GL_ARB_vertex_shader ? "" : "not ") + "supported, and ";
            capsString = capsString + "ARB_fragment_shader is " + (var0.GL_ARB_fragment_shader ? "" : "not ") + "supported.\n";
        }

        usePostProcess = hasShaders;
        String var1 = GL11.glGetString(7936).toLowerCase(Locale.ROOT);
        isNvidia = var1.contains("nvidia");
        useVboArb = !var0.OpenGL15 && var0.GL_ARB_vertex_buffer_object;
        capsString = capsString + "VBOs are available because ";
        if (useVboArb) {
            capsString = capsString + "ARB_vertex_buffer_object is supported.\n";
            GL_STATIC_DRAW = 35044;
            GL_ARRAY_BUFFER = 34962;
        } else {
            capsString = capsString + "OpenGL 1.5 is supported.\n";
            GL_STATIC_DRAW = 35044;
            GL_ARRAY_BUFFER = 34962;
        }

        isAmd = var1.contains("ati");
        if (isAmd) {
            needVbo = true;
        }

        try {
            Processor[] var2 = new SystemInfo().getHardware().getProcessors();
            cpuInfo = String.format("%dx %s", var2.length, var2[0]).replaceAll("\\s+", " ");
        } catch (Throwable var3) {
        }

    }

    public static boolean isNextGen() {
        return usePostProcess;
    }

    public static String getCapsString() {
        return capsString;
    }

    public static int glGetProgrami(int param0, int param1) {
        return useShaderArb ? ARBShaderObjects.glGetObjectParameteriARB(param0, param1) : GL20.glGetProgrami(param0, param1);
    }

    public static void glAttachShader(int param0, int param1) {
        if (useShaderArb) {
            ARBShaderObjects.glAttachObjectARB(param0, param1);
        } else {
            GL20.glAttachShader(param0, param1);
        }

    }

    public static void glDeleteShader(int param0) {
        if (useShaderArb) {
            ARBShaderObjects.glDeleteObjectARB(param0);
        } else {
            GL20.glDeleteShader(param0);
        }

    }

    public static int glCreateShader(int param0) {
        return useShaderArb ? ARBShaderObjects.glCreateShaderObjectARB(param0) : GL20.glCreateShader(param0);
    }

    public static void glShaderSource(int param0, CharSequence param1) {
        if (useShaderArb) {
            ARBShaderObjects.glShaderSourceARB(param0, param1);
        } else {
            GL20.glShaderSource(param0, param1);
        }

    }

    public static void glCompileShader(int param0) {
        if (useShaderArb) {
            ARBShaderObjects.glCompileShaderARB(param0);
        } else {
            GL20.glCompileShader(param0);
        }

    }

    public static int glGetShaderi(int param0, int param1) {
        return useShaderArb ? ARBShaderObjects.glGetObjectParameteriARB(param0, param1) : GL20.glGetShaderi(param0, param1);
    }

    public static String glGetShaderInfoLog(int param0, int param1) {
        return useShaderArb ? ARBShaderObjects.glGetInfoLogARB(param0, param1) : GL20.glGetShaderInfoLog(param0, param1);
    }

    public static String glGetProgramInfoLog(int param0, int param1) {
        return useShaderArb ? ARBShaderObjects.glGetInfoLogARB(param0, param1) : GL20.glGetProgramInfoLog(param0, param1);
    }

    public static void glUseProgram(int param0) {
        if (useShaderArb) {
            ARBShaderObjects.glUseProgramObjectARB(param0);
        } else {
            GL20.glUseProgram(param0);
        }

    }

    public static int glCreateProgram() {
        return useShaderArb ? ARBShaderObjects.glCreateProgramObjectARB() : GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int param0) {
        if (useShaderArb) {
            ARBShaderObjects.glDeleteObjectARB(param0);
        } else {
            GL20.glDeleteProgram(param0);
        }

    }

    public static void glLinkProgram(int param0) {
        if (useShaderArb) {
            ARBShaderObjects.glLinkProgramARB(param0);
        } else {
            GL20.glLinkProgram(param0);
        }

    }

    public static int glGetUniformLocation(int param0, CharSequence param1) {
        return useShaderArb ? ARBShaderObjects.glGetUniformLocationARB(param0, param1) : GL20.glGetUniformLocation(param0, param1);
    }

    public static void glUniform1(int param0, IntBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform1ivARB(param0, param1);
        } else {
            GL20.glUniform1iv(param0, param1);
        }

    }

    public static void glUniform1i(int param0, int param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform1iARB(param0, param1);
        } else {
            GL20.glUniform1i(param0, param1);
        }

    }

    public static void glUniform1(int param0, FloatBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform1fvARB(param0, param1);
        } else {
            GL20.glUniform1fv(param0, param1);
        }

    }

    public static void glUniform2(int param0, IntBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform2ivARB(param0, param1);
        } else {
            GL20.glUniform2iv(param0, param1);
        }

    }

    public static void glUniform2(int param0, FloatBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform2fvARB(param0, param1);
        } else {
            GL20.glUniform2fv(param0, param1);
        }

    }

    public static void glUniform3(int param0, IntBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform3ivARB(param0, param1);
        } else {
            GL20.glUniform3iv(param0, param1);
        }

    }

    public static void glUniform3(int param0, FloatBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform3fvARB(param0, param1);
        } else {
            GL20.glUniform3fv(param0, param1);
        }

    }

    public static void glUniform4(int param0, IntBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform4ivARB(param0, param1);
        } else {
            GL20.glUniform4iv(param0, param1);
        }

    }

    public static void glUniform4(int param0, FloatBuffer param1) {
        if (useShaderArb) {
            ARBShaderObjects.glUniform4fvARB(param0, param1);
        } else {
            GL20.glUniform4fv(param0, param1);
        }

    }

    public static void glUniformMatrix2(int param0, boolean param1, FloatBuffer param2) {
        if (useShaderArb) {
            ARBShaderObjects.glUniformMatrix2fvARB(param0, param1, param2);
        } else {
            GL20.glUniformMatrix2fv(param0, param1, param2);
        }

    }

    public static void glUniformMatrix3(int param0, boolean param1, FloatBuffer param2) {
        if (useShaderArb) {
            ARBShaderObjects.glUniformMatrix3fvARB(param0, param1, param2);
        } else {
            GL20.glUniformMatrix3fv(param0, param1, param2);
        }

    }

    public static void glUniformMatrix4(int param0, boolean param1, FloatBuffer param2) {
        if (useShaderArb) {
            ARBShaderObjects.glUniformMatrix4fvARB(param0, param1, param2);
        } else {
            GL20.glUniformMatrix4fv(param0, param1, param2);
        }

    }

    public static int glGetAttribLocation(int param0, CharSequence param1) {
        return useShaderArb ? ARBVertexShader.glGetAttribLocationARB(param0, param1) : GL20.glGetAttribLocation(param0, param1);
    }

    public static int glGenBuffers() {
        return useVboArb ? ARBVertexBufferObject.glGenBuffersARB() : GL15.glGenBuffers();
    }

    public static void glGenBuffers(IntBuffer param0) {
        if (useVboArb) {
            ARBVertexBufferObject.glGenBuffersARB(param0);
        } else {
            GL15.glGenBuffers(param0);
        }

    }

    public static void glBindBuffer(int param0, int param1) {
        if (useVboArb) {
            ARBVertexBufferObject.glBindBufferARB(param0, param1);
        } else {
            GL15.glBindBuffer(param0, param1);
        }

    }

    public static void glBufferData(int param0, ByteBuffer param1, int param2) {
        if (useVboArb) {
            ARBVertexBufferObject.glBufferDataARB(param0, param1, param2);
        } else {
            GL15.glBufferData(param0, param1, param2);
        }

    }

    public static void glDeleteBuffers(int param0) {
        if (useVboArb) {
            ARBVertexBufferObject.glDeleteBuffersARB(param0);
        } else {
            GL15.glDeleteBuffers(param0);
        }

    }

    public static void glDeleteBuffers(IntBuffer param0) {
        if (useVboArb) {
            ARBVertexBufferObject.glDeleteBuffersARB(param0);
        } else {
            GL15.glDeleteBuffers(param0);
        }

    }

    public static boolean useVbo() {
        return true;
    }

    public static void glBindFramebuffer(int param0, int param1) {
        switch(fboMode) {
            case BASE:
                GL30.glBindFramebuffer(param0, param1);
                break;
            case ARB:
                ARBFramebufferObject.glBindFramebuffer(param0, param1);
                break;
            case EXT:
                EXTFramebufferObject.glBindFramebufferEXT(param0, param1);
        }

    }

    public static void glBindRenderbuffer(int param0, int param1) {
        switch(fboMode) {
            case BASE:
                GL30.glBindRenderbuffer(param0, param1);
                break;
            case ARB:
                ARBFramebufferObject.glBindRenderbuffer(param0, param1);
                break;
            case EXT:
                EXTFramebufferObject.glBindRenderbufferEXT(param0, param1);
        }

    }

    public static void glDeleteRenderbuffers(int param0) {
        switch(fboMode) {
            case BASE:
                GL30.glDeleteRenderbuffers(param0);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteRenderbuffers(param0);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteRenderbuffersEXT(param0);
        }

    }

    public static void glDeleteFramebuffers(int param0) {
        switch(fboMode) {
            case BASE:
                GL30.glDeleteFramebuffers(param0);
                break;
            case ARB:
                ARBFramebufferObject.glDeleteFramebuffers(param0);
                break;
            case EXT:
                EXTFramebufferObject.glDeleteFramebuffersEXT(param0);
        }

    }

    public static int glGenFramebuffers() {
        switch(fboMode) {
            case BASE:
                return GL30.glGenFramebuffers();
            case ARB:
                return ARBFramebufferObject.glGenFramebuffers();
            case EXT:
                return EXTFramebufferObject.glGenFramebuffersEXT();
            default:
                return -1;
        }
    }

    public static int glGenRenderbuffers() {
        switch(fboMode) {
            case BASE:
                return GL30.glGenRenderbuffers();
            case ARB:
                return ARBFramebufferObject.glGenRenderbuffers();
            case EXT:
                return EXTFramebufferObject.glGenRenderbuffersEXT();
            default:
                return -1;
        }
    }

    public static void glRenderbufferStorage(int param0, int param1, int param2, int param3) {
        switch(fboMode) {
            case BASE:
                GL30.glRenderbufferStorage(param0, param1, param2, param3);
                break;
            case ARB:
                ARBFramebufferObject.glRenderbufferStorage(param0, param1, param2, param3);
                break;
            case EXT:
                EXTFramebufferObject.glRenderbufferStorageEXT(param0, param1, param2, param3);
        }

    }

    public static void glFramebufferRenderbuffer(int param0, int param1, int param2, int param3) {
        switch(fboMode) {
            case BASE:
                GL30.glFramebufferRenderbuffer(param0, param1, param2, param3);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferRenderbuffer(param0, param1, param2, param3);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferRenderbufferEXT(param0, param1, param2, param3);
        }

    }

    public static int glCheckFramebufferStatus(int param0) {
        switch(fboMode) {
            case BASE:
                return GL30.glCheckFramebufferStatus(param0);
            case ARB:
                return ARBFramebufferObject.glCheckFramebufferStatus(param0);
            case EXT:
                return EXTFramebufferObject.glCheckFramebufferStatusEXT(param0);
            default:
                return -1;
        }
    }

    public static void glFramebufferTexture2D(int param0, int param1, int param2, int param3, int param4) {
        switch(fboMode) {
            case BASE:
                GL30.glFramebufferTexture2D(param0, param1, param2, param3, param4);
                break;
            case ARB:
                ARBFramebufferObject.glFramebufferTexture2D(param0, param1, param2, param3, param4);
                break;
            case EXT:
                EXTFramebufferObject.glFramebufferTexture2DEXT(param0, param1, param2, param3, param4);
        }

    }

    public static int getBoundFramebuffer() {
        switch(fboMode) {
            case BASE:
                return GlStateManager.getInteger(36006);
            case ARB:
                return GlStateManager.getInteger(36006);
            case EXT:
                return GlStateManager.getInteger(36006);
            default:
                return 0;
        }
    }

    public static void glActiveTexture(int param0) {
        if (useMultitextureArb) {
            ARBMultitexture.glActiveTextureARB(param0);
        } else {
            GL13.glActiveTexture(param0);
        }

    }

    public static void glClientActiveTexture(int param0) {
        if (useMultitextureArb) {
            ARBMultitexture.glClientActiveTextureARB(param0);
        } else {
            GL13.glClientActiveTexture(param0);
        }

    }

    public static void glMultiTexCoord2f(int param0, float param1, float param2) {
        if (useMultitextureArb) {
            ARBMultitexture.glMultiTexCoord2fARB(param0, param1, param2);
        } else {
            GL13.glMultiTexCoord2f(param0, param1, param2);
        }

    }

    public static void glBlendFuncSeparate(int param0, int param1, int param2, int param3) {
        if (separateBlend) {
            if (useSeparateBlendExt) {
                EXTBlendFuncSeparate.glBlendFuncSeparateEXT(param0, param1, param2, param3);
            } else {
                GL14.glBlendFuncSeparate(param0, param1, param2, param3);
            }
        } else {
            GL11.glBlendFunc(param0, param1);
        }

    }

    public static boolean isUsingFBOs() {
        return true;
    }

    public static String getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void renderCrosshair(int param0) {
        renderCrosshair(param0, true, true, true);
    }

    public static void renderCrosshair(int param0, boolean param1, boolean param2, boolean param3) {
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        GL11.glLineWidth(4.0F);
        var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 0, 0, 255).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(0, 0, 0, 255).endVertex();
        }

        var0.end();
        GL11.glLineWidth(2.0F);
        var1.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (param1) {
            var1.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
            var1.vertex((double)param0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
        }

        if (param2) {
            var1.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).endVertex();
            var1.vertex(0.0, (double)param0, 0.0).color(0, 255, 0, 255).endVertex();
        }

        if (param3) {
            var1.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).endVertex();
            var1.vertex(0.0, 0.0, (double)param0).color(127, 127, 255, 255).endVertex();
        }

        var0.end();
        GL11.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
    }

    public static String getErrorString(int param0) {
        return LOOKUP_MAP.get(param0);
    }

    public static <T> T make(Supplier<T> param0) {
        return param0.get();
    }

    public static <T> T make(T param0, Consumer<T> param1) {
        param1.accept(param0);
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    static enum FboMode {
        BASE,
        ARB,
        EXT;
    }
}
