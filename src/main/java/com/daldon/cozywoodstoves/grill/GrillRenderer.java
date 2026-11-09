package com.daldon.cozywoodstoves.grill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.math.Axis;

public class GrillRenderer implements BlockEntityRenderer<StoveGrillBlock.GrillEntity> {
    private final ItemRenderer itemRenderer;
    public GrillRenderer(BlockEntityRendererProvider.Context ctx) { this.itemRenderer = ctx.getItemRenderer(); }
    @Override public void render(StoveGrillBlock.GrillEntity grill, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        for (int i=0;i<6;i++) {
            var stack = grill.food.get(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.25 + (i % 3)*0.25, 0.12, 0.37 + (i / 3)*0.26);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.scale(0.36f, 0.36f, 0.36f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers,
                    grill.getLevel(), (int)grill.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
