package fr.bloctave.lmr.mixin;


import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MechanicalBearingTileEntity.class)
public interface MixinMechanicalBearingTileEntity {

    @Accessor(value = "movedContraption", remap = false)
    ControlledContraptionEntity getMovedContraption();
}
