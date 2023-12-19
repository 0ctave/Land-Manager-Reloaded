package fr.bloctave.lmr.message

import fr.bloctave.lmr.handler.ClientEventHandler
import fr.bloctave.lmr.util.Message
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageShowArea : Message {
	private var toggleShowAll: Boolean = false
	private var showArea: String? = null

	@Suppress("unused")
	constructor()

	constructor(showArea: String?) {
		toggleShowAll = showArea == null
		this.showArea = showArea
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBoolean(toggleShowAll)
		if (!toggleShowAll)
			writeUtf(showArea!!)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		toggleShowAll = readBoolean()
		if (!toggleShowAll)
			showArea = readUtf()
	}

	override fun consume(context: NetworkEvent.Context) {
		if (toggleShowAll)
			ClientEventHandler.toggleRenderAll()
		else
			ClientEventHandler.setRenderArea(showArea!!)
	}
}
