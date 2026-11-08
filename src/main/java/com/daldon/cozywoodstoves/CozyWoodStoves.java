package com.daldon.cozywoodstoves;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import java.util.function.Supplier;

@Mod(CozyWoodStoves.MOD_ID)
public class CozyWoodStoves {
    public static final String MOD_ID = "cozy_wood_stoves";
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ForgeRegistries.Keys.CREATIVE_MODE_TABS, MOD_ID);

    public static final RegistryObject<Block> SMALL_STOVE = BLOCKS.register("small_stove",
        () -> new Stove(Block.Properties.of().mapColor(MapColor.METAL).strength(3.5f).lightLevel(s -> s.getValue(Stove.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> LARGE_STOVE = BLOCKS.register("large_stove",
        () -> new Stove(Block.Properties.of().mapColor(MapColor.METAL).strength(4f).lightLevel(s -> s.getValue(Stove.LIT) ? 14 : 0)));
    public static final RegistryObject<Block> CHIMNEY_PIPE = BLOCKS.register("chimney_pipe",
        () -> new Block(Block.Properties.of().mapColor(MapColor.METAL).strength(3f)));
    public static final RegistryObject<Block> CHIMNEY_ELBOW = BLOCKS.register("chimney_elbow",
        () -> new Block(Block.Properties.of().mapColor(MapColor.METAL).strength(3f)));
    public static final RegistryObject<Block> ROOF_CHIMNEY = BLOCKS.register("roof_chimney",
        () -> new Block(Block.Properties.of().mapColor(MapColor.METAL).strength(3f)));

    private static RegistryObject<Item> blockItem(String name, Supplier<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static final RegistryObject<Item> SMALL_ITEM = blockItem("small_stove", SMALL_STOVE);
    public static final RegistryObject<Item> LARGE_ITEM = blockItem("large_stove", LARGE_STOVE);
    public static final RegistryObject<Item> PIPE_ITEM = blockItem("chimney_pipe", CHIMNEY_PIPE);
    public static final RegistryObject<Item> ELBOW_ITEM = blockItem("chimney_elbow", CHIMNEY_ELBOW);
    public static final RegistryObject<Item> ROOF_ITEM = blockItem("roof_chimney", ROOF_CHIMNEY);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () ->
        CreativeModeTab.builder().title(Component.literal("Cozy Wood Stoves"))
            .icon(() -> new ItemStack(SMALL_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(SMALL_ITEM.get()); output.accept(LARGE_ITEM.get());
                output.accept(PIPE_ITEM.get()); output.accept(ELBOW_ITEM.get()); output.accept(ROOF_ITEM.get());
            }).build());

    public CozyWoodStoves() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus); ITEMS.register(bus); TABS.register(bus);
    }

    public static class Stove extends HorizontalDirectionalBlock {
        public static final BooleanProperty LIT = BlockStateProperties.LIT;
        public Stove(Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, LIT);
        }
        @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
        }
        @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                               InteractionHand hand, BlockHitResult hit) {
            ItemStack held = player.getItemInHand(hand);
            boolean fuel = held.is(ItemTags.LOGS) || held.is(Items.COAL) || held.is(Items.CHARCOAL)
                || held.is(Items.COAL_BLOCK) || held.is(Items.BLAZE_ROD);
            if (!fuel) return InteractionResult.PASS;
            if (!level.isClientSide && !state.getValue(LIT)) {
                level.setBlock(pos, state.setValue(LIT, true), 3);
                level.scheduleTick(pos, this, 1200);
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 0.9F);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (state.getValue(LIT)) level.setBlock(pos, state.setValue(LIT, false), 3);
        }
        @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            if (state.getValue(LIT) && random.nextInt(5) == 0)
                level.addParticle(ParticleTypes.SMOKE, pos.getX()+0.5, pos.getY()+1.05, pos.getZ()+0.5, 0, 0.03, 0);
        }
    }
}
