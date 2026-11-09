package com.daldon.cozywoodstoves.grill;

import com.daldon.cozywoodstoves.CozyWoodStoves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import javax.annotation.Nullable;
import java.util.Optional;

/** Thin cooking grate placed in the block space directly above either large stove. */
public class StoveGrillBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 2, 15);
    public StoveGrillBlock() { super(Properties.of().mapColor(MapColor.METAL).strength(2.5f).noOcclusion()); }

    public static boolean onLargeStove(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && id.getPath().equals("large_stove")
                && (id.getNamespace().equals("functional_stoves"));
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return onLargeStove(level.getBlockState(pos.below()));
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrillEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, CozyWoodStoves.GRILL_TYPE, GrillEntity::tick);
    }
    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos,
        net.minecraft.world.level.block.Block changed, BlockPos changedPos, boolean moving) {
        if (!state.canSurvive(level, pos) && !level.isClientSide) level.destroyBlock(pos, true);
        super.neighborChanged(state, level, pos, changed, changedPos, moving);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GrillEntity grill)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                if (grill.ready[i] && !grill.food.get(i).isEmpty()) {
                    if (!level.isClientSide) {
                        ItemStack cooked = grill.food.get(i).copy();
                        if (!player.getInventory().add(cooked)) player.drop(cooked, false);
                        grill.clear(i);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            return InteractionResult.PASS;
        }
        Optional<CampfireCookingRecipe> recipe = level.getRecipeManager().getRecipeFor(
            RecipeType.CAMPFIRE_COOKING, new SimpleContainer(held.copyWithCount(1)), level);
        if (recipe.isEmpty()) return InteractionResult.PASS;
        for (int i = 0; i < 6; i++) {
            if (grill.food.get(i).isEmpty()) {
                if (!level.isClientSide) {
                    grill.food.set(i, held.copyWithCount(1));
                    grill.progress[i] = 0;
                    grill.ready[i] = false;
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    grill.changedAndSync();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof GrillEntity grill) {
            if (!level.isClientSide) Containers.dropContents(level, pos, new SimpleContainer(grill.food.toArray(new ItemStack[0])));
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, next, moving);
    }

    public static class GrillEntity extends BlockEntity {
        public final NonNullList<ItemStack> food = NonNullList.withSize(6, ItemStack.EMPTY);
        public final int[] progress = new int[6];
        public final boolean[] ready = new boolean[6];
        private int syncCooldown;

        public GrillEntity(BlockPos pos, BlockState state) { super(CozyWoodStoves.GRILL_TYPE, pos, state); }
        public void clear(int slot) {
            food.set(slot, ItemStack.EMPTY); progress[slot] = 0; ready[slot] = false; changedAndSync();
        }
        public void changedAndSync() {
            setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        public static void tick(Level level, BlockPos pos, BlockState state, GrillEntity grill) {
            if (!(level instanceof ServerLevel server)) return;
            // Heat only while the original/compatible stove is lit.
            BlockState below = level.getBlockState(pos.below());
            if (!onLargeStove(below) || !below.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    || !below.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) return;
            boolean changed = false;
            for (int i=0;i<6;i++) {
                if (grill.ready[i] || grill.food.get(i).isEmpty()) continue;
                ItemStack ingredient = grill.food.get(i);
                Optional<CampfireCookingRecipe> recipe = level.getRecipeManager().getRecipeFor(
                    RecipeType.CAMPFIRE_COOKING, new SimpleContainer(ingredient), level);
                if (recipe.isEmpty()) continue;
                grill.progress[i]++;
                if (grill.progress[i] >= recipe.get().getCookingTime()) {
                    ItemStack cooked = recipe.get().assemble(new SimpleContainer(ingredient), level.registryAccess());
                    if (cooked.isEmpty()) continue;
                    cooked = cooked.copyWithCount(1);
                    cooked.getOrCreateTag().putBoolean("CozyGrillBonus", true);
                    grill.food.set(i, cooked);
                    grill.ready[i] = true;
                    changed = true;
                }
            }
            if (changed || (++grill.syncCooldown % 100 == 0)) grill.changedAndSync();
            else grill.setChanged();
        }
        @Override protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            net.minecraft.world.ContainerHelper.saveAllItems(tag, food);
            tag.putIntArray("Progress", progress);
            int mask = 0;
            for (int i=0;i<6;i++) if (ready[i]) mask |= 1 << i;
            tag.putInt("ReadyMask", mask);
        }
        @Override public void load(CompoundTag tag) {
            super.load(tag);
            net.minecraft.world.ContainerHelper.loadAllItems(tag, food);
            int[] saved = tag.getIntArray("Progress");
            for (int i=0;i<6;i++) {
                progress[i] = i < saved.length ? saved[i] : 0;
                ready[i] = (tag.getInt("ReadyMask") & (1 << i)) != 0;
            }
        }
        @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
        @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    }
}
