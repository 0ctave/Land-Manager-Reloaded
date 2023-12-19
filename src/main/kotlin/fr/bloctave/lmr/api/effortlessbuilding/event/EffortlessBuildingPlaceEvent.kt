package fr.bloctave.lmr.api.effortlessbuilding.event

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class EffortlessBuildingPlaceEvent(blockSnapshot: BlockSnapshot, val direction: Direction, val player: PlayerEntity, val hitVector: Vector3d) : BlockEvent(blockSnapshot.world, blockSnapshot.pos, blockSnapshot.replacedBlock) {

    private val DEBUG = System.getProperty("forge.debugBlockEvent", "false").toBoolean()

    init {
        if (DEBUG) {
            System.out.printf(
                "Created EntityPlaceEvent - [PlacedBlock: %s ][PlacedAgainst: %s ][Entity: %s ]\n",
                blockSnapshot.replacedBlock,
                direction,
                player
            )
        }
    }




}