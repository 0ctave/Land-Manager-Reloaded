package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.Message
import fr.bloctave.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageAreaChange : Message {
	private lateinit var area: Area

	@Suppress("unused")
	constructor()

	constructor(area: Area) {
		this.area = area
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeCompoundTag(area.serializeNBT())
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		area = Area(readCompoundTag()!!)
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().world!!.areasCap.updateArea(area)
		}
	}
}
