package fr.bloctave.lmr.api.create.handler

import com.simibubi.create.content.contraptions.base.KineticTileEntity
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity
import com.simibubi.create.content.contraptions.components.structureMovement.piston.LinearActuatorTileEntity
import fr.bloctave.lmr.api.create.CreateConfig
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.mixin.MixinMechanicalBearingTileEntity
import fr.bloctave.lmr.util.areasCap
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class WorldTickHandler : IEventHandler<TickEvent.WorldTickEvent> {

    private var tickStack = 0

    private val refreshRate = 20
    @SubscribeEvent
    override fun handleEvent(event: TickEvent.WorldTickEvent) = event.run {
        if (world.isClientSide)
            return@run

        if (phase == TickEvent.Phase.START) {

            world.tickableBlockEntities.filterIsInstance<KineticTileEntity>().filter { it.speed != 0f }.forEach {
                when(it) {
                    is MechanicalBearingTileEntity -> {
                        val bearing = it as MixinMechanicalBearingTileEntity
                        if (bearing.movedContraption != null)
                            if(world.areasCap.getIntersectingAreas(bearing.movedContraption.boundingBox).any { area -> !area.getConfig<CreateConfig>().kinetics() }) {
                                it.speed = 0f
                                it.disassemble()
                            }

                    }
                    is LinearActuatorTileEntity -> {
                        if (it.movedContraption != null)
                            if(world.areasCap.getIntersectingAreas(it.movedContraption.boundingBox).any { area -> !area.getConfig<CreateConfig>().kinetics() })
                                it.speed = 0f
                    }
                    else -> {}
                }

            }
        }
        /*if (phase == TickEvent.Phase.START)
            return@run

        val areas = world.areasCap.getAllAreas().ifEmpty { return@run }

        if (areas.size in (tickStack + 1) until refreshRate) {
            val area = areas[tickStack]
            area.lifetime += tickStack + refreshRate

            if (area.getConfig<VanillaConfig>().lifetime() != -1.0 && area.lifetime > area.getConfig<VanillaConfig>().lifetime())
                world.areasCap.removeArea(area.name)

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
        tickStack %= refreshRate*/
    }
}