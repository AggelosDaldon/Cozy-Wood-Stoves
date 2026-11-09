package com.daldon.cozywoodstoves.wood;

import com.daldon.cozywoodstoves.CozyWoodStoves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

/** Validates wood type, distance, target, and stack counts on the server. */
public final class WoodPileNetwork {
    private static final String VERSION="1";
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CozyWoodStoves.MOD_ID,"wood_pile"),
        ()->VERSION, VERSION::equals, VERSION::equals);
    private WoodPileNetwork(){}
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered=true;
        CHANNEL.registerMessage(0, StackWood.class, StackWood::encode, StackWood::decode,
                StackWood::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    public record StackWood(BlockPos clicked, Direction face) {
        public static void encode(StackWood msg,FriendlyByteBuf out){
            out.writeBlockPos(msg.clicked); out.writeEnum(msg.face);
        }
        public static StackWood decode(FriendlyByteBuf in){
            return new StackWood(in.readBlockPos(),in.readEnum(Direction.class));
        }
        public static void handle(StackWood msg,Supplier<NetworkEvent.Context> supplier){
            NetworkEvent.Context ctx=supplier.get();
            ctx.enqueueWork(()->{
                ServerPlayer player=ctx.getSender();
                if(player==null) return;
                Level level=player.level();
                if(player.position().distanceToSqr(msg.clicked.getX()+0.5,
                        msg.clicked.getY()+0.5,msg.clicked.getZ()+0.5)>49) return;
                ItemStack held=player.getMainHandItem();
                if(!WoodPileBlock.isValidWood(held)) return;
                BlockPos place=level.getBlockState(msg.clicked).is(CozyWoodStoves.WOOD_PILE_BLOCK)
                        ? msg.clicked : msg.clicked.relative(msg.face);
                if(!level.hasChunkAt(place)) return;
                if(level.getBlockEntity(place) instanceof WoodPileBlock.PileEntity pile){
                    pile.add(held,player);
                    return;
                }
                if(msg.face != Direction.UP) return;
                BlockState support=level.getBlockState(msg.clicked);
                if(!support.isFaceSturdy(level,msg.clicked,Direction.UP)) return;
                if(!level.getBlockState(place).canBeReplaced()) return;
                if(!level.setBlock(place,CozyWoodStoves.WOOD_PILE_BLOCK.defaultBlockState(),3)) return;
                if(level.getBlockEntity(place) instanceof WoodPileBlock.PileEntity pile){
                    if(!pile.add(held,player)) level.removeBlock(place,false);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
