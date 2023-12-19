package fr.bloctave.lmr.mixin;

import fr.bloctave.lmr.api.effortlessbuilding.event.EffortlessBuildingBreakEvent;
import fr.bloctave.lmr.api.effortlessbuilding.event.EffortlessBuildingPlaceEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.network.BlockBrokenMessage;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nl.requios.effortlessbuilding.buildmode.BuildModes.initializeMode;


@Debug(export = true)
@Mixin(BuildModes.class)
public abstract class MixinBuildModes {

   @Inject(method = "onBlockPlacedMessage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onBlockPlacedMessage(PlayerEntity player, BlockPlacedMessage message, CallbackInfo ci) {
        boolean canceled = false;

        if (player != null && message != null && message.isBlockHit() && message.getBlockPos() != null) {
            BlockSnapshot blockSnapshot = BlockSnapshot.create(player.level.dimension(), player.level, message.getBlockPos());
            Direction direction = message.getSideHit();
            canceled = MinecraftForge.EVENT_BUS.post(new EffortlessBuildingPlaceEvent(blockSnapshot, direction, player, message.getHitVec()));
        }

        if (canceled) {
            initializeMode(player);
            ci.cancel();
        }
    }

    @Inject(method = "onBlockBrokenMessage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onBlockBrokenMessage(PlayerEntity player, BlockBrokenMessage message, CallbackInfo ci) {
        boolean canceled = false;

        if (player != null && message != null && message.isBlockHit()) {
            BlockState state = player.level.getBlockState(message.getBlockPos());
            canceled = MinecraftForge.EVENT_BUS.post(new EffortlessBuildingBreakEvent(player.level, message.getBlockPos(), state, player));
        }

        if (canceled) {
            initializeMode(player);
            ci.cancel();
        }
    }


}
