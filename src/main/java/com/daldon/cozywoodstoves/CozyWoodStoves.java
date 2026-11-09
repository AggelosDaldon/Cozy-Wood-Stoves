package com.daldon.cozywoodstoves;

import com.daldon.cozywoodstoves.grill.GrillRenderer;
import com.daldon.cozywoodstoves.grill.StoveGrillBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CozyWoodStoves.MOD_ID)
public class CozyWoodStoves {
    public static final String MOD_ID = "cozy_wood_stoves";
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // This addon does not replace or re-register any Functional Stoves blocks.
    public static final RegistryObject<Block> STOVE_GRILL = BLOCKS.register("stove_grill", StoveGrillBlock::new);
    public static final RegistryObject<Item> GRILL_ITEM = ITEMS.register("stove_grill",
            () -> new BlockItem(STOVE_GRILL.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<StoveGrillBlock.GrillEntity>> GRILL_ENTITY =
            BLOCK_ENTITIES.register("stove_grill",
                    () -> BlockEntityType.Builder.of(StoveGrillBlock.GrillEntity::new, STOVE_GRILL.get()).build(null));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () ->
            CreativeModeTab.builder().title(Component.literal("Stove Cooking Grate"))
                    .icon(() -> new ItemStack(GRILL_ITEM.get()))
                    .displayItems((parameters, output) -> output.accept(GRILL_ITEM.get())).build());

    public CozyWoodStoves() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientOnly::registerRenderers));
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        ItemStack consumed = event.getItem();
        if (consumed.isEdible() && consumed.hasTag() && consumed.getTag().getBoolean("CozyGrillBonus")) {
            // Eat 3 extra food points and 3 additional saturation points.
            player.getFoodData().eat(3, 0.5f);
        }
    }

    private static class ClientOnly {
        static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(GRILL_ENTITY.get(), GrillRenderer::new);
        }
    }
}
