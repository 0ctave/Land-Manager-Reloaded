package fr.bloctave.lmr.api.create.handler

import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity
import com.simibubi.create.content.contraptions.components.structureMovement.IControlContraption
import fr.bloctave.lmr.api.proxy.IEventHandler
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class SpawnCheckSpawnHandler : IEventHandler<EntityEvent.EnteringChunk> {

    @SubscribeEvent
    override fun handleEvent(event: EntityEvent.EnteringChunk) = event.run {

        if (!entity.level.isClientSide && entity is ControlledContraptionEntity) {
            val entity = entity as ControlledContraptionEntity
            //println(getController(entity.blockPosition(), entity.level))
            if (entity.contraption != null) {
                 //val controller = getController((entity as MixinControlledContraptionEntity).controllerPos, entity.level)
                //println(controller)
                /*if(entity.level.areasCap.intersectsAnArea(entity.boundingBox)) {
                    when (controller) {
                        is MechanicalBearingTileEntity -> {
                            controller.speed = 0f
                            //controller.disassemble()
                        }
                        is LinearActuatorTileEntity -> {
                            controller.speed = 0f

                            //controller.disassemble()
                        }

                        else -> {}
                    }
                }*/



                /*print(entity.contraption.bounds)
                print(entity.contraption.anchor)
                print(entity.contraption.simplifiedEntityColliders)*/
               /* if(entity.level.areasCap.intersectsAnArea(entity.boundingBox)){
                    println("intersects")
                    /*println(entity.contraption.actors)
                    entity.contraption.stop(entity.level)
                    entity.setContraptionMotion(Vector3d.ZERO)*/
                    entity.disassemble()
                    //(getController(entity.contraption.anchor.below(), entity.level) as MechanicalBearingTileEntity).disassemble()
                    //entity.disassemble()
                }*/
            }
                /*entity.entityData.set((entity as AbstractContraptionEntity)::class.java.getDeclaredField("STALLED").let { field ->
                    field.isAccessible = true
                    return@let field.get(this)
                } as DataParameter<Boolean>, true)*/
        }
        /*val area = EventUtil.basicChecks(entity, world as? World, entity.blockPosition()) ?: return@run
        val hostile = !entityLiving.type.category.isFriendly

        if ((hostile && area.getConfig<VanillaConfig>().hostileSpawning()) || (!hostile && area.getConfig<VanillaConfig>().passiveSpawning()))
            return@run

        result = Event.Result.DENY*/
    }

    fun getController(controllerPos: BlockPos, level: World): IControlContraption? {
        if (!level.isLoaded(controllerPos)) return null
        val te: TileEntity? = level.getBlockEntity(controllerPos)

        return te as IControlContraption
    }
}