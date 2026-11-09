package com.daldon.cozywoodstoves.wood;

import com.daldon.cozywoodstoves.CozyWoodStoves;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class WoodPileClient {
    private WoodPileClient(){}

    // Left Control by default, fully remappable in Minecraft Options > Controls > Key Binds.
    public static final KeyMapping STACK_KEY=new KeyMapping("key.functional_stoves.stack_wood",
        KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_LEFT_CONTROL,
        "key.categories.functional_stoves");

    @Mod.EventBusSubscriber(modid=CozyWoodStoves.MOD_ID,value=Dist.CLIENT,
            bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(STACK_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid=CozyWoodStoves.MOD_ID,value=Dist.CLIENT,
            bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent public static void stackWood(PlayerInteractEvent.RightClickBlock event) {
            if(!event.getLevel().isClientSide || event.getHand()!=InteractionHand.MAIN_HAND) return;
            if(Minecraft.getInstance().screen!=null || !STACK_KEY.isDown()) return;
            if(!WoodPileBlock.isValidWood(event.getEntity().getMainHandItem())) return;
            WoodPileNetwork.CHANNEL.sendToServer(new WoodPileNetwork.StackWood(
                    event.getPos(),event.getFace()));
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
