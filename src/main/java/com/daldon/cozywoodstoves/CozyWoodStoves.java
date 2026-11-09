package com.daldon.cozywoodstoves;

// No @Mod class here. The original FunctionalStoves class is preserved byte-for-byte
// inside the unified JAR. RegisterEvents below are picked up by the Forge mod scanner.

import com.daldon.cozywoodstoves.grill.StoveGrillBlock;
import com.daldon.cozywoodstoves.grill.GrillRenderer;
import com.daldon.cozywoodstoves.wood.WoodPileBlock;
import com.daldon.cozywoodstoves.wood.WoodPileRenderer;
import com.daldon.cozywoodstoves.wood.WoodPileNetwork;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CozyWoodStoves {
    public static final String MOD_ID = "functional_stoves";
    public static final ResourceLocation GRILL_ID = new ResourceLocation(MOD_ID, "stove_grill");
    public static Block GRILL_BLOCK;
    public static Item GRILL_ITEM;
    public static BlockEntityType<StoveGrillBlock.GrillEntity> GRILL_TYPE;
    public static final ResourceLocation WOOD_PILE_ID = new ResourceLocation(MOD_ID,"wood_pile");
    public static Block WOOD_PILE_BLOCK;
    public static BlockEntityType<WoodPileBlock.PileEntity> WOOD_PILE_TYPE;

    private CozyWoodStoves() {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
                GRILL_BLOCK = new StoveGrillBlock();
                helper.register(GRILL_ID, GRILL_BLOCK);
            });
            event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
                WOOD_PILE_BLOCK = new WoodPileBlock();
                helper.register(WOOD_PILE_ID, WOOD_PILE_BLOCK);
            });
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                GRILL_ITEM = new BlockItem(GRILL_BLOCK, new Item.Properties());
                helper.register(GRILL_ID, GRILL_ITEM);
            });
            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
                GRILL_TYPE = BlockEntityType.Builder.of(
                        StoveGrillBlock.GrillEntity::new, GRILL_BLOCK).build(null);
                helper.register(GRILL_ID, GRILL_TYPE);
                WOOD_PILE_TYPE = BlockEntityType.Builder.of(WoodPileBlock.PileEntity::new,WOOD_PILE_BLOCK).build(null);
                helper.register(WOOD_PILE_ID,WOOD_PILE_TYPE);
            });
        }

        @SubscribeEvent
        public static void setupNetworking(FMLCommonSetupEvent event) {
            event.enqueueWork(WoodPileNetwork::register);
        }

        @SubscribeEvent
        public static void addCreativeItem(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    || event.getTabKey().location().getNamespace().equals(MOD_ID)) {
                event.accept(GRILL_ITEM);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}
        @SubscribeEvent
        public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(GRILL_TYPE, GrillRenderer::new);
            event.registerBlockEntityRenderer(WOOD_PILE_TYPE,WoodPileRenderer::new);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Gameplay {
        private Gameplay() {}
        @SubscribeEvent
        public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
            if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
            ItemStack stack = event.getItem();
            if (stack.isEdible() && stack.hasTag() && stack.getTag().getBoolean("CozyGrillBonus")) {
                // FoodData.eat(food, saturationModifier):
                // +3 hunger, +3 actual saturation (=3*2*0.5), subject to vanilla caps.
                player.getFoodData().eat(3, 0.5f);
            }
        }
    }
}
