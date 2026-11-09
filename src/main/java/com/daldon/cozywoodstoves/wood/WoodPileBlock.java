package com.daldon.cozywoodstoves.wood;

import com.daldon.cozywoodstoves.CozyWoodStoves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class WoodPileBlock extends BaseEntityBlock {
    public WoodPileBlock() { super(Properties.of().mapColor(MapColor.WOOD).strength(1.2f).noOcclusion()); }
    public static final int MAX_LOGS = 12;

    // Minecraft's logs item tag covers both natural logs/stems and bark-covered wood/hyphae.
    // This deliberately rejects planks, stripped planks and unrelated wooden items.
    public static boolean isValidWood(ItemStack stack) {
        return stack.is(ItemTags.LOGS) && stack.getItem() instanceof BlockItem;
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (level.getBlockEntity(pos) instanceof PileEntity pile)
            return box(1, 0, 1, 15, Math.max(3, Math.min(15, ((pile.count + 3) / 4) * 4 + 1)), 15);
        return box(1, 0, 1, 15, 5, 15);
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PileEntity(pos,state);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                            Player player, InteractionHand hand, BlockHitResult hit) {
        // Ordinary empty-hand right-click retrieves one log; Ctrl-right-click stacking
        // is handled by the client keybind's server-validated network packet.
        if (!player.getItemInHand(hand).isEmpty() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof PileEntity pile) || pile.count == 0) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack removed = pile.wood.copyWithCount(1);
            if (!player.getInventory().add(removed)) player.drop(removed, false);
            pile.count--;
            if (pile.count == 0) {
                level.removeBlock(pos, false);
            } else pile.sync();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof PileEntity pile && pile.count > 0) {
            Containers.dropItemStack(level, pos.getX()+0.5,pos.getY()+0.3,pos.getZ()+0.5,
                    pile.wood.copyWithCount(pile.count));
        }
        super.onRemove(state,level,pos,next,moving);
    }

    public static class PileEntity extends BlockEntity {
        public ItemStack wood = ItemStack.EMPTY;
        public int count = 0;
        public PileEntity(BlockPos pos, BlockState state) { super(CozyWoodStoves.WOOD_PILE_TYPE, pos, state); }
        public void sync() {
            setChanged();
            if (level != null && !level.isClientSide)
                level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
        public boolean add(ItemStack held, Player player) {
            if (!isValidWood(held) || count >= MAX_LOGS) return false;
            if (count != 0 && !ItemStack.isSameItemSameTags(wood, held)) return false;
            if (count == 0) wood=held.copyWithCount(1);
            count++;
            if (!player.getAbilities().instabuild) held.shrink(1);
            sync();
            return true;
        }
        @Override protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            tag.put("Wood",wood.save(new CompoundTag()));
            tag.putInt("Count",count);
        }
        @Override public void load(CompoundTag tag) {
            super.load(tag);
            wood=tag.contains("Wood")?ItemStack.of(tag.getCompound("Wood")):ItemStack.EMPTY;
            count=Math.max(0,Math.min(MAX_LOGS,tag.getInt("Count")));
            if (wood.isEmpty()) count=0;
        }
        @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
        @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    }
}
