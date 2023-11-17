package fr.bloctave.landmanager.command.nonop

import fr.bloctave.landmanager.command.AbstractCommand
import fr.bloctave.landmanager.command.LMCommand.AREA
import fr.bloctave.landmanager.command.argumentType.AreaArgument
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.*
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

object AreaCommand : AbstractCommand(
	"area",
	{
		thenArgument(AREA, AreaArgument) {
			// area <name>
			executes { AreaCommand.doCommand(it, AreaArgument.get(it, AREA)) }
		}
	}
) {
	private fun doCommand(context: CommandContext<CommandSource>, area: Area): Int {
		val server = context.source.server
		val player = context.source.asPlayer()

		val ownerName = area.owner?.let { ownerUuid ->
			server.getUsernameFromUuid(ownerUuid)?.let { StringTextComponent(it) }
		} ?: TranslationTextComponent("lm.command.area.none")

		val members = area.members.mapNotNull { server.getUsernameFromUuid(it) }.sorted().let {
			if (it.isEmpty())
				TranslationTextComponent("lm.command.area.none")
			else
				StringTextComponent(it.joinToString(", "))
		}

		player.sendMessage(
			StringTextComponent("").mergeStyle(TextFormatting.WHITE)
				.appendSibling(TranslationTextComponent("lm.command.area.name").mergeStyle(TextFormatting.YELLOW))
				.appendString(" ${area.name}")
				.appendString("\n ").appendSibling(goldText("lm.command.area.owner")).appendString(" ").appendSibling(ownerName)
				.appendString("\n ").appendSibling(goldText("lm.command.area.members")).appendString(" ").appendSibling(members)
				.appendString("\n ").appendSibling(goldText("lm.command.area.dim")).appendString(" ${area.dim}")
				.appendString("\n ").appendSibling(goldText("lm.command.area.posmin")).appendString(" ")
				.appendSibling(posToText(area.minPos))
				.appendString("\n ").appendSibling(goldText("lm.command.area.posmax")).appendString(" ")
				.appendSibling(posToText(area.maxPos))
				/*.appendString("\n ").appendSibling(goldText("lm.command.area.passives")).appendString(" ")
				.appendSibling(boolToText(area.canPassiveSpawn))
				.appendString("\n ").appendSibling(goldText("lm.command.area.hostiles")).appendString(" ")
				.appendSibling(boolToText(area.canHostileSpawn))
				.appendString("\n ").appendSibling(goldText("lm.command.area.explosions")).appendString(" ")
				.appendSibling(boolToText(area.explosions))
				.appendString("\n ").appendSibling(goldText("lm.command.area.interactions")).appendString(" ")
				.appendSibling(boolToText(area.interactions))*/
		)
		return 1
	}

	private fun goldText(langKey: String): ITextComponent =
		TranslationTextComponent(langKey).mergeStyle(TextFormatting.GOLD)

	private fun posToText(pos: BlockPos): ITextComponent = StringTextComponent("")
		.appendStyledString("X: ", TextFormatting.YELLOW).appendString("${pos.x}, ")
		.appendStyledString("Y: ", TextFormatting.YELLOW).appendString("${pos.y}, ")
		.appendStyledString("Z: ", TextFormatting.YELLOW).appendString(pos.z)

	private fun boolToText(bool: Boolean): ITextComponent =
		TranslationTextComponent(if (bool) "message.landmanager.misc.true" else "message.landmanager.misc.false")
}
