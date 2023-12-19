package fr.bloctave.lmr.message

import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.areasCap
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
		writeUtf(oldName)
		writeUtf(newName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		oldName = readUtf()
		newName = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().level!!.areasCap.renameArea(oldName, newName)
		}
	}
}
