package fr.bloctave.landmanager.handler

import fr.bloctave.landmanager.LMConfig
import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.util.areasCap
import fr.bloctave.landmanager.util.sendActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object CommonEventHandler {

	private fun sendCapToPlayer(player: PlayerEntity) {
		if (player is ServerPlayerEntity)
			player.world.areasCap.sendDataToPlayer(player)
	}

	@SubscribeEvent
	fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = sendCapToPlayer(event.player)

	@SubscribeEvent
	fun onPlayerChangeDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = sendCapToPlayer(event.player)
}
