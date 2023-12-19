package fr.bloctave.lmr.block

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.message.MessageOpenHomeGui
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.getUsernameFromUuid
import fr.bloctave.lmr.util.isOp
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld

class HomeBlock(props: Properties) : Block(props) {
	@Deprecated("Deprecated in Java")
	override fun use(
		state: BlockState,
		world: World,
		pos: BlockPos,
		player: PlayerEntity,
		hand: Hand,
		hit: BlockRayTraceResult
	): ActionResultType {
		if (world.isClientSide || world !is ServerWorld || player !is ServerPlayerEntity || player.isCrouching || hand != Hand.MAIN_HAND)
			return ActionResultType.SUCCESS
		world.areasCap.intersectingArea(pos)?.let { area ->
			if (area.isMember(player.uuid) || player.isOp()) {
				val server = world.server
				val owner = area.owner?.let { uuid -> server.getUsernameFromUuid(uuid)?.let { it to uuid } }
				val members = area.members.mapNotNull { uuid -> server.getUsernameFromUuid(uuid)?.let { it to uuid } }
				LandManager.NETWORK.sendToPlayer(MessageOpenHomeGui(pos, player.isOp(), owner, members), player)
			} else
				player.displayClientMessage(TranslationTextComponent("message.lmr.home.notMember"), true)
		} ?: run {
			player.displayClientMessage(TranslationTextComponent("message.lmr.home.none"), true)
		}
		return ActionResultType.SUCCESS
	}
}
