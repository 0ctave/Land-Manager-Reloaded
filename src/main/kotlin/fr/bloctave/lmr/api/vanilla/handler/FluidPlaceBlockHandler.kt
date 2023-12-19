package fr.bloctave.lmr.api.vanilla.handler

import fr.bloctave.lmr.api.proxy.IEventHandler
import net.minecraftforge.event.world.BlockEvent

class FluidPlaceBlockHandler : IEventHandler<BlockEvent.FluidPlaceBlockEvent> {

    override fun handleEvent(event: BlockEvent.FluidPlaceBlockEvent) = event.run {

    }
}