package fr.bloctave.lmr.mixin;


import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ControlledContraptionEntity.class)
public interface MixinControlledContraptionEntity {

    @Accessor(value = "controllerPos", remap = false)
    BlockPos getControllerPos();
}
