package fr.bloctave.landmanager.message

import fr.bloctave.landmanager.AreaCreationEvent
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.data.areas.AddAreaResult
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.AreaChangeType
import fr.bloctave.landmanager.util.Message
import fr.bloctave.landmanager.util.areasCap
import fr.bloctave.landmanager.util.sendToPlayer
import net.minecraft.network.PacketBuffer
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.network.NetworkEvent

class MessageCreateArea : Message {
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
			val player = context.sender ?: return@enqueueWork
			val world = player.world
			var result = AddAreaResult.INVALID
			if (area.dim == player.world.dimensionKey.location && area.minPos.y >= 0 && area.maxPos.y <= world.height) {
				val cap = world.areasCap
				when {
					!Area.validateName(area.name) -> result = AddAreaResult.INVALID_NAME
					cap.hasArea(area.name) -> result = AddAreaResult.NAME_EXISTS
					cap.intersectsAnArea(area) -> result = AddAreaResult.AREA_INTERSECTS
					!MinecraftForge.EVENT_BUS.post(fr.bloctave.landmanager.AreaCreationEvent(area)) -> {
						result = if (cap.addArea(area)) AddAreaResult.SUCCESS else AddAreaResult.NAME_EXISTS
						if (result == AddAreaResult.SUCCESS)
							LandManager.areaChange(
								(world as ServerWorld).server,
								AreaChangeType.CREATE,
								area.name,
								player
							)
					}
				}
			}
			LandManager.NETWORK.sendToPlayer(MessageCreateAreaReply(area.name, result), player)
		}
	}
}
