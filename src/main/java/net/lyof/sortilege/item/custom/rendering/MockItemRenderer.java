package net.lyof.sortilege.item.custom.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MockItemRenderer {
    public static final float DEFAULT_THICKNESS = 0.065f;
    public static final int MAX_LIGHT = 15728880;

    public static void renderItem(PoseStack matrixStack, MultiBufferSource bufferSource, int light, ResourceLocation texture) {
        renderTintedItem(matrixStack, bufferSource, light, texture, 0xFFFFFF);
    }

    public static void renderTintedItem(PoseStack matrixStack, MultiBufferSource bufferSource, int light, ResourceLocation texture, int tint) {
        int red = (tint >> 16) & 0xFF;
        int green = (tint >> 8) & 0xFF;
        int blue = tint & 0xFF;

        renderTintedItem(matrixStack, bufferSource, light, texture, red, green, blue);
    }

    public static void renderTintedItem(PoseStack matrixStack, MultiBufferSource bufferSource, int light, ResourceLocation texture, int red, int green, int blue) {
        Boolean[][] pixelData = loadPixelData(texture, 16);

        renderItem(pixelData, matrixStack, bufferSource, light, texture, DEFAULT_THICKNESS, red, green, blue);
    }

    public static void renderItem(Boolean[][] pixelData, PoseStack matrixStack, MultiBufferSource bufferSource,
                                  int light, ResourceLocation texture, float thickness, int red, int green, int blue) {

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(texture));
        if (light < 0) {
            light = MAX_LIGHT;
            thickness = 0;
        }

        matrixStack.pushPose();

        float halfZ = thickness * 0.5f;

        int width = pixelData.length;
        int height = (width > 0) ? pixelData[0].length : 0;

        Matrix4f pose = matrixStack.last().pose();
        Matrix3f normal = matrixStack.last().normal();

        renderItem(pixelData, buffer, pose, normal, halfZ, light, width, height, red, green, blue);

        matrixStack.popPose();
    }


    private static void renderItem(Boolean[][] pixelData, VertexConsumer buffer, Matrix4f pose, Matrix3f normal,
                                   float halfZ, int light, int width, int height, int red, int green, int blue) {
        // Front face
        addQuad(
                buffer, pose, normal,
                0, 0, halfZ,
                1, 0, halfZ,
                1, 1, halfZ,
                0, 1, halfZ,
                computeUV(0, 0, 1, 1), computeUV(1, 0, 1, 1),
                computeUV(1, 1, 1, 1), computeUV(0, 1, 1, 1),
                light, 0, 0, 1, red, green, blue
        );

        // Back face
        addQuad(
                buffer, pose, normal,
                1, 0, -halfZ,
                0, 0, -halfZ,
                0, 1, -halfZ,
                1, 1, -halfZ,
                computeUV(1, 0, 1, 1), computeUV(0, 0, 1, 1),
                computeUV(0.0f, 1, 1, 1), computeUV(1, 1, 1, 1),
                light, 0, 0, -1, red, green, blue
        );

        if (halfZ == 0) return;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Boolean pd = pixelData[x][y];
                if (!pd) {
                    continue;
                }

                float scaledX = (float) x / width;
                float scaledY = (float) y / height;
                float scaledXNext = (float) (x + 1) / width;
                float scaledYNext = (float) (y + 1) / height;

                if (x == 0 || !pixelData[x - 1][y]) {
                    float[][] sideUV = computeVerticalSliceUV(x, y, y + 1, width, height);
                    addQuad(
                            buffer, pose, normal,
                            scaledX, scaledYNext, halfZ,
                            scaledX, scaledYNext, -halfZ,
                            scaledX, scaledY, -halfZ,
                            scaledX, scaledY, halfZ,
                            sideUV[0], sideUV[1], sideUV[2], sideUV[3],
                            light, -1, 0, 0, red, green, blue
                    );
                }
                if (x == width - 1 || !pixelData[x + 1][y]) {
                    float[][] sideUV = computeVerticalSliceUV(x, y, y + 1, width, height);
                    addQuad(
                            buffer, pose, normal,
                            scaledXNext, scaledYNext, -halfZ,
                            scaledXNext, scaledYNext, halfZ,
                            scaledXNext, scaledY, halfZ,
                            scaledXNext, scaledY, -halfZ,
                            sideUV[0], sideUV[1], sideUV[2], sideUV[3],
                            light, 1, 0, 0, red, green, blue
                    );
                }

                if (y == 0 || !pixelData[x][y - 1]) {
                    float[][] sideUV = computeHorizontalSliceUV(x, x + 1, y, width, height);
                    addQuad(
                            buffer, pose, normal,
                            scaledXNext, scaledY, -halfZ,
                            scaledXNext, scaledY, halfZ,
                            scaledX, scaledY, halfZ,
                            scaledX, scaledY, -halfZ,
                            sideUV[0], sideUV[1], sideUV[2], sideUV[3],
                            light, 0, -1, 0, red, green, blue
                    );
                }
                if (y == height - 1 || !pixelData[x][y + 1]) {
                    float[][] sideUV = computeHorizontalSliceUV(x, x + 1, y, width, height);
                    addQuad(
                            buffer, pose, normal,
                            scaledXNext, scaledYNext, halfZ,
                            scaledXNext, scaledYNext, -halfZ,
                            scaledX, scaledYNext, -halfZ,
                            scaledX, scaledYNext, halfZ,
                            sideUV[0], sideUV[1], sideUV[2], sideUV[3],
                            light, 0, 1, 0, red, green, blue
                    );
                }
            }
        }
    }

    private static float[] computeUV(float x, float y, float width, float height) {
        float u = x / width;
        float v = y / height;
        return new float[]{u, v};
    }

    private static float[][] computeVerticalSliceUV(float x, float y0, float y1, float width, float height) {
        float u0 = x / width;
        float u1 = (x + 1) / width;
        float v0 = y0 / height;
        float v1 = y1 / height;

        return new float[][] {
                {u0, v0},
                {u1, v0},
                {u1, v1},
                {u0, v1}
        };
    }

    private static float[][] computeHorizontalSliceUV(float x0, float x1, float y, float width, float height) {
        float u0 = x0 / width;
        float u1 = x1 / width;
        float v0 = y / height;
        float v1 = (y + 1) / height;

        return new float[][] {
                {u0, v0},
                {u1, v0},
                {u1, v1},
                {u0, v1}
        };
    }

    private static void addQuad(VertexConsumer buffer, Matrix4f pose, Matrix3f normalMatrix,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float x2, float y2, float z2, float x3, float y3, float z3,
                                float[] uv0, float[] uv1, float[] uv2, float[] uv3,
                                int light, float nx, float ny, float nz, int r, int g, int b) {

        buffer.vertex(pose, x0, y0, z0)
                .color(r, g, b, 255)
                .uv(uv0[0], uv0[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
        buffer.vertex(pose, x1, y1, z1)
                .color(r, g, b, 255)
                .uv(uv1[0], uv1[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
        buffer.vertex(pose, x2, y2, z2)
                .color(r, g, b, 255)
                .uv(uv2[0], uv2[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
        buffer.vertex(pose, x3, y3, z3)
                .color(r, g, b, 255)
                .uv(uv3[0], uv3[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }


    private static Map<ResourceLocation, Boolean[][]> CACHE = new HashMap<>();

    public static Boolean[][] loadPixelData(ResourceLocation texture, int alphaThreshold) {
        if (CACHE.containsKey(texture)) return CACHE.get(texture);

        Resource resource = Minecraft.getInstance().getResourceManager().getResource(texture).orElseThrow();

        try (InputStream input = resource.open()) {
            BufferedImage image = ImageIO.read(input);
            int width  = image.getWidth();
            int height = image.getHeight();

            Boolean[][] pixelData = new Boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int argb  = image.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;

                    pixelData[x][y] = (alpha >= alphaThreshold);
                }
            }

            CACHE.putIfAbsent(texture, pixelData);

            return pixelData;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read texture: " + texture, e);
        }
    }
}