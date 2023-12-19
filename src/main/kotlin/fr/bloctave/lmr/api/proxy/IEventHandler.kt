package fr.bloctave.lmr.api.proxy

import net.minecraftforge.eventbus.api.SubscribeEvent

interface IEventHandler<T> {
    @SubscribeEvent
    fun handleEvent(event: T)
}
