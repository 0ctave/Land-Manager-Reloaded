package fr.bloctave.lmr.message

import fr.bloctave.lmr.config.ServerConfig
import fr.bloctave.lmr.data.areas.AddAreaResult
import fr.bloctave.lmr.gui.CreateAreaScreen
import fr.bloctave.lmr.handler.ClientEventHandler
import fr.bloctave.lmr.item.AreaCreateItem
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.readEnumValue
import fr.bloctave.lmr.util.sendActionBarMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.network.NetworkEvent

class MessageCreateAreaReply : Message {
	private lateinit var areaName: String
	private lateinit var result: AddAreaResult

	@Suppress("unused")
	constructor()

	constructor(areaName: String, result: AddAreaResult) {
		this.areaName = areaName
		this.result = result
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeUtf(areaName)
		writeEnum(result)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		areaName = readUtf()
		result = readEnumValue()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val mc = Minecraft.getInstance()
			val player = mc.player!!
			val gui = mc.screen
			when (result) {
				AddAreaResult.SUCCESS -> {
					player.sendActionBarMessage("message.lmr.create.added", TextFormatting.GREEN, areaName)
					closeScreen(gui, player)
					ClientEventHandler.setRenderArea(areaName)
				}
				AddAreaResult.NAME_EXISTS -> {
					player.sendActionBarMessage("message.lmr.create.name", TextFormatting.RED, areaName)
					clearTextField(gui)
				}
				AddAreaResult.AREA_INTERSECTS -> {
					player.sendActionBarMessage("message.lmr.create.intersects", TextFormatting.RED)
					closeScreen(gui, player)
				}
				AddAreaResult.INVALID_SIZE -> {
					player.sendActionBarMessage("message.lmr.create.size", TextFormatting.RED, maxAreaSizeStr())
					clearTextField(gui)
				}
				AddAreaResult.INVALID_NAME -> {
					player.sendActionBarMessage("message.lmr.create.invalid_name", TextFormatting.RED)
					clearTextField(gui)
				}
				AddAreaResult.INVALID -> {
					player.sendActionBarMessage("message.lmr.create.invalid", TextFormatting.RED)
					closeScreen(gui, player)
				}
			}
		}
	}

	fun maxAreaSizeStr(): String {
		return if (ServerConfig.maxAreaWidth() * ServerConfig.maxAreaLength() < ServerConfig.maxAreaSize() && ServerConfig.maxAreaSize() != -1.0) "${ServerConfig.maxAreaSize()}" else "${ServerConfig.maxAreaWidth()}x${ServerConfig.maxAreaLength()}"
	}

	private fun closeScreen(gui: Screen?, player: PlayerEntity) {
		if (gui is CreateAreaScreen)
			player.closeContainer()
		val stack = player.mainHandItem
		if (AreaCreateItem.getPos(stack) != null)
			AreaCreateItem.setPos(stack, null)
	}

	private fun clearTextField(gui: Screen?) {
		if (gui is CreateAreaScreen)
			gui.clearTextField()
	}
}
