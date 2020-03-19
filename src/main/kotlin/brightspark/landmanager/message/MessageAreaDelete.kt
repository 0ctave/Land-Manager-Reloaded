package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageAreaDelete : Message {
	private lateinit var areaName: String

	@Suppress("unused")
	constructor()

	constructor(areaName: String) {
		this.areaName = areaName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeString(areaName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		areaName = readString()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			Minecraft.getInstance().world.areasCap.removeArea(areaName)
		}
	}
}
