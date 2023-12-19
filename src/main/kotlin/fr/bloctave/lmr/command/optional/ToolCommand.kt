package fr.bloctave.lmr.command.optional

import fr.bloctave.lmr.area_create
import fr.bloctave.lmr.command.AbstractCommand
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TranslationTextComponent

object ToolCommand : AbstractCommand(
	"tool",
	{
		executes {
			val player = it.source.playerOrException
			val result = player.addItem(ItemStack(area_create!!))
			if (!result)
				it.source.sendSuccess(TranslationTextComponent("lmr.command.tool.inventory"), false)
			return@executes if (result) 1 else 0
		}
	}
)
