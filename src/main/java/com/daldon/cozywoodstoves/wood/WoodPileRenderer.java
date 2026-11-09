package com.daldon.cozywoodstoves.wood;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/** The original log's block texture is used; no hardcoded wood varieties. */
public class WoodPileRenderer implements BlockEntityRenderer<WoodPileBlock.PileEntity> {
    private final BlockRenderDispatcher renderer;
    public WoodPileRenderer(BlockEntityRendererProvider.Context ctx) { renderer=ctx.getBlockRenderDispatcher(); }

    @Override public void render(WoodPileBlock.PileEntity pile, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        if (pile.count <= 0 || !(pile.wood.getItem() instanceof BlockItem blockItem)) return;
        BlockState log=blockItem.getBlock().defaultBlockState();
        // Horizontal wooden billets, with natural log/wood end grains and bark.
        if (log.hasProperty(RotatedPillarBlock.AXIS))
            log=log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);

        for (int i=0;i<pile.count;i++) {
            int layer=i/4, slot=i%4;
            // Each layer contains four small billets; odd layers are staggered.
            double x=0.12+(slot%2)*0.40 + (layer%2)*0.025;
            double z=0.15+(slot/2)*0.38;
            double y=0.015+layer*0.245;
            pose.pushPose();
            pose.translate(x,y,z);
            pose.scale(0.36f,0.235f,0.34f);
            renderer.renderSingleBlock(log,pose,buffers,light,OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }
}
