package fr.bloctave.lmr.item

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.config.ServerConfig
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.Position
import fr.bloctave.lmr.message.MessageOpenCreateAreaGui
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.sendActionBarMessage
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class AreaCreateItem(props: Properties) : Item(props) {
	override fun isFoil(stack: ItemStack): Boolean = true

	override fun onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType {
		// Only work in main hand!
		if (context.hand != Hand.MAIN_HAND)
			return super.onItemUseFirst(stack, context)

		val world = context.level
		val player = context.player!!
		val pos1 = getPos(stack)
		val pos2 = context.clickedPos
		when {
			pos1 == null -> {
				// Store pos in item
				setPos(stack, Position(player.level.dimension().location(), pos2))
				if (world.isClientSide)
					player.sendActionBarMessage(
						"message.lmr.tool.saved",
						TextFormatting.GREEN,
						pos2.x,
						pos2.y,
						pos2.z
					)
			}
			pos1.dimension != player.level.dimension().location() -> {
				//  Stored pos in different dimension! Remove stored pos
				setPos(stack, null)
				if (world.isClientSide)
					player.sendActionBarMessage("message.lmr.tool.diffdim", TextFormatting.RED)
			}
			else -> {
				if (!world.isClientSide) {
					val area = Area("", pos1.dimension, pos1.position, pos2)
					val cap = world.areasCap

					if (!cap.validAreaSize(area))
						player.sendActionBarMessage("message.lmr.create.size", TextFormatting.RED, maxAreaSizeStr())
					else if (!cap.validAreaIntersections(area))
						player.sendActionBarMessage("message.lmr.create.intersects", TextFormatting.RED)
					else {
						LandManager.NETWORK.sendToPlayer(
							MessageOpenCreateAreaGui(pos1.dimension, pos1.position, pos2),
							player as ServerPlayerEntity
						)
					}
				}
			}
		}

		return ActionResultType.SUCCESS
	}

	fun maxAreaSizeStr(): String {
		return if (ServerConfig.maxAreaWidth() * ServerConfig.maxAreaLength() < ServerConfig.maxAreaSize() && ServerConfig.maxAreaSize() != -1.0) "${ServerConfig.maxAreaSize()}" else "${ServerConfig.maxAreaWidth()}x${ServerConfig.maxAreaLength()}"
	}

	override fun use(world: World, player: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
		// Only work in main hand!
		if (hand != Hand.MAIN_HAND)
			return super.use(world, player, hand)

		val stack = player.getItemInHand(hand)
		if (player.isCrouching && getPos(stack) != null) {
			// Clear position
			setPos(stack, null)
			if (world.isClientSide)
				player.sendActionBarMessage("message.lmr.tool.cleared")
			return ActionResult(ActionResultType.SUCCESS, stack)
		}

		return super.use(world, player, hand)
	}

	override fun appendHoverText(
		stack: ItemStack,
		world: World?,
		tooltip: MutableList<ITextComponent>,
		flag: ITooltipFlag
	) {
		tooltip.add(getPos(stack)
			?.let {
				TranslationTextComponent(
					"item.lmr.area_create.tooltip.set",
					it.dimension,
					posToString(it.position)
				)
			}
			?: TranslationTextComponent("item.lmr.area_create.tooltip.notset")
		)
	}

	private fun posToString(pos: BlockPos) = "${pos.x}, ${pos.y}, ${pos.z}"

	companion object {
		fun setPos(stack: ItemStack, position: Position?) {
			if (position == null)
				stack.tag = null
			else
				stack.addTagElement("pos", position.serializeNBT())

		}

		fun getPos(stack: ItemStack): Position? = stack.tag?.let { Position(it.getCompound("pos"))

		}
	}
}
