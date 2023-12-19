package fr.bloctave.lmr.mixin;

import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PistonBlock.class)
public interface MixinPistonBlock {
    @Accessor("isSticky")
    boolean isSticky();

}
