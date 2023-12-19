package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.areasCap
import fr.bloctave.lmr.util.sendMessage
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import kotlin.math.ceil

class WorldTickHandler : IEventHandler<TickEvent.WorldTickEvent> {

    private var tickStack = 0

    private val refreshRate = 20
    @SubscribeEvent
    override fun handleEvent(event: TickEvent.WorldTickEvent) = event.run {
        if (phase == TickEvent.Phase.START)
            return@run

        val areas = world.areasCap.getAllAreas().ifEmpty { return@run }

        if (areas.size in (tickStack + 1) until refreshRate) {
            val area = areas[tickStack]
            area.lifetime += tickStack + refreshRate

            if (area.getConfig<VanillaConfig>().lifetime() != -1.0 && area.lifetime > area.getConfig<VanillaConfig>().lifetime()) {
                var time = (area.getConfig<VanillaConfig>().lifetime() / 20.0).toInt()
                val second = time % 60
                time = (time - second) / 60
                val minute = time % 60
                time = (time - minute) / 60
                val hour = time % 24
                time = (time - hour) / 24
                val day = time

                area.owner?.let { world.getPlayerByUUID(it)?.sendMessage("message.lmr.protection.lifetime", area.name, day, hour, minute, second) }
                world.areasCap.removeArea(area.name)
            }

        } else if (areas.size < refreshRate) {
            val range = Pair(ceil((((tickStack) % refreshRate.toDouble()) / refreshRate) * areas.size).toInt(), ceil(
                ((((tickStack + 1 ) % refreshRate.toDouble()) / refreshRate) * areas.size)
            ).toInt())
            for (area in world.areasCap.getAllAreas().slice(range.first until range.second)) {
                area.lifetime += tickStack + refreshRate
                if (area.getConfig<VanillaConfig>().lifetime() != -1.0 && area.lifetime > area.getConfig<VanillaConfig>().lifetime())
                    world.areasCap.removeArea(area.name)
            }
        }


        tickStack++
        tickStack %= refreshRate
    }
}