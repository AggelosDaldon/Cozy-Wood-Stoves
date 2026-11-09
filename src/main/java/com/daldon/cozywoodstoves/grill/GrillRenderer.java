package com.daldon.cozywoodstoves.grill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/** Six visibly separated, camera-facing food models above the cast iron plate. */
public class GrillRenderer implements BlockEntityRenderer<StoveGrillBlock.GrillEntity> {
    private final ItemRenderer itemRenderer;
    public GrillRenderer(BlockEntityRendererProvider.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
    }
    @Override public void render(StoveGrillBlock.GrillEntity grill, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        for (int i=0;i<6;i++) {
            var stack=grill.food.get(i);
            if(stack.isEmpty()) continue;
            pose.pushPose();
            // Plate surface is 4.6px high; foods stand just above it.
            // Three evenly separated items in front, three behind.
            pose.translate(0.25+(i%3)*0.25,0.055,0.34+(i/3)*0.30);
            pose.mulPose(new org.joml.Quaternionf().rotationXYZ((float)Math.PI / 2f, 0f, 0f));
            pose.scale(-0.30f,0.30f,0.30f);
            itemRenderer.renderStatic(stack,ItemDisplayContext.FIXED,light,
                    OverlayTexture.NO_OVERLAY,pose,buffers,grill.getLevel(),
                    (int)grill.getBlockPos().asLong()+i);
            pose.popPose();
        }
    }
}
