package fr.bloctave.lmr.api.create

import fr.bloctave.lmr.api.create.handler.WorldTickHandler
import fr.bloctave.lmr.api.proxy.SoftProxy

object CreateProxy : SoftProxy<CreateConfig>("create", CreateConfig::class) {

    init {
        this.addEventHandler(WorldTickHandler::class)
       // this.addEventHandler(SpawnCheckSpawnHandler::class)

    }




}