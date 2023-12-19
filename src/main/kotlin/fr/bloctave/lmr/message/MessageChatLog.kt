package fr.bloctave.lmr.message

import fr.bloctave.lmr.config.ClientConfig
import fr.bloctave.lmr.util.AreaChangeType
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.appendTranslation
import fr.bloctave.lmr.util.readEnumValue
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Util
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.network.NetworkEvent
import java.text.SimpleDateFormat
import java.util.*

class MessageChatLog : Message {
	companion object {
		private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
	}

	private var timestamp: Long = 0
	private lateinit var type: AreaChangeType
	private lateinit var areaName: String
	private lateinit var playerName: String

	@Suppress("unused")
	constructor()

	constructor(timestamp: Long, type: AreaChangeType, areaName: String, playerName: String) {
		this.timestamp = timestamp
		this.type = type
		this.areaName = areaName
		this.playerName = playerName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeLong(timestamp)
		writeEnum(type)
		writeUtf(areaName)
		writeUtf(playerName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		timestamp = readLong()
		type = readEnumValue()
		areaName = readUtf()
		playerName = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		if (!ClientConfig.showChatLogs()) return
		context.enqueueWork {
			Minecraft.getInstance().player!!.sendMessage(
				StringTextComponent(DATE_FORMAT.format(Date(timestamp)))
					.withStyle(TextFormatting.GRAY)
					.append(" ")
					.appendTranslation(type.unlocalisedName)
					.append(": $areaName -> $playerName"),
				Util.NIL_UUID
			)
		}
	}
}
