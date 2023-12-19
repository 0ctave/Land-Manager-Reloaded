package fr.bloctave.lmr.api.proxy

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import fr.bloctave.lmr.command.AbstractCommand
import fr.bloctave.lmr.command.argumentType.AreaArgument
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.data.areas.AreaUpdateType
import fr.bloctave.lmr.util.*
import net.minecraft.command.CommandSource
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.event.RegisterCommandsEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class CommandProxyFactory(private val proxy: SoftProxy<*>) {

    companion object {
        const val AREA = "areaName"
        const val PAGE = "pageNum"
        const val AREA_REGEX = "areaNameRegex"
        const val PLAYER = "playerName"
        const val REQUEST = "requestId"

        val ERROR_CANT_EDIT = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.noPerm", it) }
        val ERROR_NO_AREA = DynamicCommandExceptionType { TranslationTextComponent("lmr.command.none", it) }

        fun permissionCommand(
            context: CommandContext<CommandSource>,
            action: (Area) -> Unit,
            feedback: (Area) -> ITextComponent
        ): Int {
            val source = context.source
            val area = AreaArgument.get(context, AREA)
            val areas = source.server.getWorldCapForArea(area) ?: throw ERROR_NO_AREA.create(area.name)
            val sender = source.source
            if (!sender.canEditArea(area))
                throw ERROR_CANT_EDIT.create(area.name)
            action(area)
            areas.dataChanged(area, AreaUpdateType.CHANGE)
            source.sendSuccess(feedback(area), true)
            return 1
        }
    }

    init {

        FORGE_BUS.apply {
            addListener<RegisterCommandsEvent> { it.dispatcher.register(CommandFactory(proxy)) }
        }
    }

    class CommandFactory(proxy: SoftProxy<*>) : AbstractCommand(
        "lmr",
        {
            thenLiteral("permissions") {
                thenArgument(AREA, AreaArgument) {
                    proxy.getConfig().fields.forEach {
                        thenCommand(ProxyConfigCommand(proxy, it.key, it.value))
                    }
                }
            }
        }

    )

}