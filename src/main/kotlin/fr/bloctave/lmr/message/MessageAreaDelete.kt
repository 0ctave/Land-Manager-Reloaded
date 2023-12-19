package fr.bloctave.lmr.message

import fr.bloctave.lmr.util.Message
import fr.bloctave.lmr.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageAreaDelete : Message {
	private lateinit var areaName: String

	@Suppress("unused")
	constructor()

	constructor(areaName: String) {
		this.areaName = areaName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeUtf(areaName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		areaName = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().level!!.areasCap.removeArea(areaName)
		}
	}
}
