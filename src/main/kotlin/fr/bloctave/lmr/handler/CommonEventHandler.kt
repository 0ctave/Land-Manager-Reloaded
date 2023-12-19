package fr.bloctave.lmr.handler

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.message.MessageRequestAdd
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.requests
import fr.bloctave.lmr.util.sendToPlayer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object CommonEventHandler {

	private fun sendCapToPlayer(player: PlayerEntity) {


		if (player is ServerPlayerEntity)
			player.level.areasCap.sendDataToPlayer(player)
	}

	@SubscribeEvent
	fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
		if (event.player is ServerPlayerEntity) {
			val player: ServerPlayerEntity = event.player as ServerPlayerEntity
			event.player.server!!.requests.getAll().forEach { request ->
				LandManager.NETWORK.sendToPlayer(MessageRequestAdd(request), player)
			}
		}

		sendCapToPlayer(event.player)
	}


	@SubscribeEvent
	fun onPlayerChangeDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = sendCapToPlayer(event.player)
}
