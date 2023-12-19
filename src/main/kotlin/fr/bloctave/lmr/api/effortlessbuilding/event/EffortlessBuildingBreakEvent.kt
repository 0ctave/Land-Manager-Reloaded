package fr.bloctave.lmr.api.effortlessbuilding.event

import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class EffortlessBuildingBreakEvent(world: World?, pos: BlockPos?, state: BlockState?, val player: PlayerEntity) : BlockEvent(world, pos, state) {

    /** Reference to the Player who broke the block. If no player is available, use a EntityFakePlayer  */
    private var exp = 0

    init {
        if (state == null || !ForgeHooks.canHarvestBlock(
                state,
                player,
                world!!,
                pos!!
            )
        ) // Handle empty block or player unable to break block scenario
        {
            exp = 0
        } else {
            val bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player)
            val silklevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player)
            exp = state.getExpDrop(world, pos, bonusLevel, silklevel)
        }
    }

    /**
     * Get the experience dropped by the block after the event has processed
     *
     * @return The experience to drop or 0 if the event was canceled
     */
    fun getExpToDrop(): Int {
        return if (isCanceled) 0 else exp
    }

}