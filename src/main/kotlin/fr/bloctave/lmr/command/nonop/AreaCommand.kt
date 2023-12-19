package fr.bloctave.lmr.command.nonop

import com.mojang.brigadier.context.CommandContext
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.LMCommand.AREA
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.*
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
        val player = context.source.playerOrException

        if (!CommandUtil.canSeeArea(area, player)) {
            context.source.sendSuccess(TranslationTextComponent("lmr.command.area.noPerm", area.name), true)
            return 0
        }

        val ownerName = area.owner?.let { ownerUuid ->
            server.getUsernameFromUuid(ownerUuid)?.let { StringTextComponent(it) }
        } ?: TranslationTextComponent("lmr.command.area.none")

        val members = area.members.mapNotNull { server.getUsernameFromUuid(it) }.sorted().let {
            if (it.isEmpty())
                TranslationTextComponent("lmr.command.area.none")
            else
                StringTextComponent(it.joinToString(", "))
        }

        var time = area.lifetime / 20
        val second = (time % 60).toInt()
        time = (time - second) / 60
        val minute = (time % 60).toInt()
        time = (time - minute) / 60
        val hour = (time % 24).toInt()
        time = (time - hour) / 24
        val day = time.toInt()

        player.sendMessage(
            StringTextComponent("").withStyle(TextFormatting.WHITE)
                .append(TranslationTextComponent("lmr.command.area.name").withStyle(TextFormatting.YELLOW))
                .append(" ${area.name}")
                .append("\n ").append(goldText("lmr.command.area.owner")).append(" ")
                .append(ownerName)
                .append("\n ").append(goldText("lmr.command.area.members")).append(" ")
                .append(members)
                .append("\n ").append(goldText("lmr.command.area.dim")).append(" ${area.dim}")
                .append("\n ").append(goldText("lmr.command.area.posmin")).append(" ")
                .append(posToText(area.minPos))
                .append("\n ").append(goldText("lmr.command.area.posmax")).append(" ")
                .append(posToText(area.maxPos))
                .append("\n ").append(goldText("lmr.command.area.living", day, hour, minute, second))
                .append(getPermissions(area))

            /*.append("\n ").append(goldText("lmr.command.area.passives")).append(" ")
            .append(boolToText(area.canPassiveSpawn))
            .append("\n ").append(goldText("lmr.command.area.hostiles")).append(" ")
            .append(boolToText(area.canHostileSpawn))
            .append("\n ").append(goldText("lmr.command.area.explosions")).append(" ")
            .append(boolToText(area.explosions))
            .append("\n ").append(goldText("lmr.command.area.interactions")).append(" ")
            .append(boolToText(area.interactions))*/
        )
        return 1
    }



    private fun getPermissions(area: Area): StringTextComponent {
        val permissionString = StringTextComponent("")
        area.permissions.forEach { (_, config) ->
            config.fields.forEach {
                permissionString.append("\n").append(goldText("lmr.command.area.${it.key}")).append(" ")
                    .append(
                        when (it.value.type) {
                            Boolean::class -> boolToText(config.getValue(it.key))
                            Int::class -> StringTextComponent(config.getValue(it.key, it.value.type).toString())
                            Double::class -> StringTextComponent(config.getValue(it.key, it.value.type).toString())
                            else -> throw IllegalArgumentException("Unknown type")
                        }
                    )
            }
        }

        return permissionString
    }

    private fun goldText(langKey: String, vararg args: Any): ITextComponent =
        TranslationTextComponent(langKey, *args).withStyle(TextFormatting.GOLD)

    private fun posToText(pos: BlockPos): ITextComponent = StringTextComponent("")
        .appendStyledString("X: ", TextFormatting.YELLOW).append("${pos.x}, ")
        .appendStyledString("Y: ", TextFormatting.YELLOW).append("${pos.y}, ")
        .appendStyledString("Z: ", TextFormatting.YELLOW).append(pos.z)

    private fun boolToText(bool: Boolean): ITextComponent =
        TranslationTextComponent(if (bool) "message.lmr.misc.true" else "message.lmr.misc.false")
}
