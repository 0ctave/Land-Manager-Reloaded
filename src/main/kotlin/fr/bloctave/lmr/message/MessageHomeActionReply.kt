package fr.bloctave.lmr.message

import fr.bloctave.lmr.gui.HomeScreen
import fr.bloctave.lmr.util.HomeGuiActionType
import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.readEnumValue
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

class MessageHomeActionReply : Message {
	private lateinit var type: HomeGuiActionType
	private lateinit var uuid: UUID
	private lateinit var name: String

	@Suppress("unused")
	constructor()

	constructor(type: HomeGuiActionType, uuid: UUID, name: String) {
		this.type = type
		this.uuid = uuid
		this.name = name
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeEnum(type)
		writeUUID(uuid)
		writeUtf(name)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		type = readEnumValue()
		uuid = readUUID()
		name = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val mc = Minecraft.getInstance()
			val gui = mc.screen
			if (gui !is HomeScreen)
				return@enqueueWork
			when (type) {
				HomeGuiActionType.ADD -> {
					gui.playerList.addMember(Pair(name, uuid))

					gui.clearInput()
				}
				HomeGuiActionType.KICK -> {
					gui.playerList.removeMember(Pair(name, uuid))
					gui.clearPlayerSelection()
				}
				HomeGuiActionType.PASS -> {
					val player = mc.player!!
					if (player.uuid == uuid)
						player.closeContainer()
				}
			}
		}
	}
}
