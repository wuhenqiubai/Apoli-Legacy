package io.github.apace100.apoli.power;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.OptionalInt;

public class OverlayPower extends Power {
    @Environment(EnvType.CLIENT)
    private static final RenderPipeline.Snippet OVERLAY_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET)
        .withFragmentShader("core/position_tex_color")
        .withVertexShader("core/position_tex_color")
        .withSampler("Sampler0")
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .withUniform("ColorModulator", UniformType.VEC4) // We don't need this, but also, for some reason it will spam the logs if we don't use it?
        .withBlend(BlendFunction.PANORAMA)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .buildSnippet();

    @Environment(EnvType.CLIENT)
    private static final RenderPipeline OVERLAY_PIPELINE = RenderPipeline.builder(OVERLAY_SNIPPET)
        .withLocation(Apoli.identifier("pipeline/overlay"))
        .build();

    @Environment(EnvType.CLIENT)
    private static final RenderPipeline NAUSEA_PIPELINE = RenderPipeline.builder(OVERLAY_SNIPPET)
        .withLocation(Apoli.identifier("pipeline/overlay_nausea"))
        .withBlend(BlendFunction.ADDITIVE)
        .build();

    private final ResourceLocation texture;
    private final float strength;
    private final float red;
    private final float green;
    private final float blue;
    private final DrawMode drawMode;
    private final DrawPhase drawPhase;
    private final boolean hideWithHud;
    private final boolean visibleInThirdPerson;

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

    public OverlayPower(PowerType<?> type, LivingEntity entity, ResourceLocation texture, float strength, float red, float green, float blue, DrawMode drawMode, DrawPhase drawPhase, boolean hideWithHud, boolean visibleInThirdPerson) {
        super(type, entity);
        this.texture = texture;
        this.strength = strength;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.drawMode = drawMode;
        this.drawPhase = drawPhase;
        this.hideWithHud = hideWithHud;
        this.visibleInThirdPerson = visibleInThirdPerson;
    }

    public DrawPhase getDrawPhase() {
        return drawPhase;
    }

    public boolean shouldBeVisibleInThirdPerson() {
        return visibleInThirdPerson;
    }

    public boolean doesHideWithHud() {
        return hideWithHud;
    }

    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics guiGraphics) {
        Minecraft client = Minecraft.getInstance();
        int i = client.getWindow().getGuiScaledWidth();
        int j = client.getWindow().getGuiScaledHeight();

        float d, e, l, m, n;
        float g, h, k, a;

        switch(drawMode) {
            case NAUSEA:
                d = Mth.lerp(strength, 2.0f, 1.0f);
                g = red * strength;
                h = green * strength;
                k = blue * strength;
                e = i * d;
                l = j * d;
                m = (i - e) / 2.0f;
                n = (j - l) / 2.0f;
                a = 1.0F;
                break;
            case TEXTURE: default:
                g = red;
                h = green;
                k = blue;
                a = strength;
                e = i;
                l = j;
                m = 0;
                n = 0;
                break;
        }

        var renderTarget = client.getMainRenderTarget();
        var encoder = RenderSystem.getDevice().createCommandEncoder();

        guiGraphics.blit(location -> RenderType.guiNauseaOverlay(), texture, 0, 0, 0f, 0f, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), ARGB.colorFromFloat(a, g, h, k));
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(m, n + l, -90.0f).setUv(0.0F, 1.0F).setColor(g, h, k, a);
        bufferBuilder.addVertex(m + e, n + l, -90.0f).setUv(1.0F, 1.0F).setColor(g, h, k, a);
        bufferBuilder.addVertex(m + e, n, -90.0f).setUv(1.0F, 0.0F).setColor(g, h, k, a);
        bufferBuilder.addVertex(m, n, -90.0f).setUv(0.0F, 0.0F).setColor(g, h, k, a);

        try (MeshData meshData = bufferBuilder.buildOrThrow()) {
            var vertexBuffer = DefaultVertexFormat.POSITION_TEX.uploadImmediateVertexBuffer(meshData.vertexBuffer());
            var indexBufferStorage = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
            var indexBuffer = indexBufferStorage.getBuffer(meshData.drawState().indexCount());
            var indexType = indexBufferStorage.type();
            var gpuTexture = client.getTextureManager().getTexture(texture).getTexture();

            try (RenderPass pass = encoder.createRenderPass(renderTarget.getColorTexture(), OptionalInt.empty())) {
                pass.bindSampler("Sampler0", gpuTexture);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indexBuffer, indexType);

                if (drawMode == DrawMode.NAUSEA) {
                    pass.setPipeline(NAUSEA_PIPELINE);
                } else {
                    pass.setPipeline(OVERLAY_PIPELINE);
                }

                pass.drawIndexed(0, meshData.drawState().indexCount());
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("overlay"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER)
                .add("strength", SerializableDataTypes.FLOAT, 1.0F)
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F)
                .add("draw_mode", SerializableDataType.enumValue(OverlayPower.DrawMode.class))
                .add("draw_phase", SerializableDataType.enumValue(OverlayPower.DrawPhase.class))
                .add("hide_with_hud", SerializableDataTypes.BOOLEAN, true)
                .add("visible_in_third_person", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> new OverlayPower(type, player,
                    data.getId("texture"),
                    data.getFloat("strength"),
                    data.getFloat("red"),
                    data.getFloat("green"),
                    data.getFloat("blue"),
                    (OverlayPower.DrawMode) data.get("draw_mode"),
                    (OverlayPower.DrawPhase) data.get("draw_phase"),
                    data.getBoolean("hide_with_hud"),
                    data.getBoolean("visible_in_third_person")))
            .allowCondition();
    }
}
