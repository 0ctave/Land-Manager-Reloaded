package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.util.Message
import fr.bloctave.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageAreaRename : Message {
	private lateinit var oldName: String
	private lateinit var newName: String

	@Suppress("unused")
	constructor()

	constructor(oldName: String, newName: String) {
		this.oldName = oldName
		this.newName = newName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeString(oldName)
		writeString(newName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		oldName = readString()
		newName = readString()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().world!!.areasCap.renameArea(oldName, newName)
		}
	}
}
