package fr.bloctave.lmr.api.effortlessbuilding

import fr.bloctave.lmr.api.effortlessbuilding.handler.BlockEntityBreakHandler
import fr.bloctave.lmr.api.effortlessbuilding.handler.BlockEntityPlaceHandler
import fr.bloctave.lmr.api.proxy.SoftProxy


object EffortlessBuildingProxy : SoftProxy<EffortlessBuildingConfig>("effortlessbuilding", EffortlessBuildingConfig::class) {


    init {

        this.addEventHandler(BlockEntityPlaceHandler::class)
        this.addEventHandler(BlockEntityBreakHandler::class)



    }




}