package fr.bloctave.lmr.api.effortlessbuilding.handler

import fr.bloctave.lmr.api.effortlessbuilding.event.EffortlessBuildingPlaceEvent
import fr.bloctave.lmr.api.proxy.IEventHandler
import fr.bloctave.lmr.api.vanilla.VanillaConfig
import fr.bloctave.lmr.util.EventUtil
import fr.bloctave.lmr.util.sendAreaActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.eventbus.api.SubscribeEvent
import nl.requios.effortlessbuilding.buildmode.*
import nl.requios.effortlessbuilding.buildmodifier.ModifierSettingsManager
import nl.requios.effortlessbuilding.helper.ReachHelper
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class BlockEntityPlaceHandler : IEventHandler<EffortlessBuildingPlaceEvent> {

    @SubscribeEvent
    override fun handleEvent(event: EffortlessBuildingPlaceEvent) = event.run {

        val modifierSettings = ModifierSettingsManager.getModifierSettings(player)
        val modeSettings = ModeSettingsManager.getModeSettings(player)
        val buildMode = modeSettings.buildMode

        val coordinates = buildMode.instance.findCoordinates(player, pos, modifierSettings.doQuickReplace()) ?: return@run

        coordinates.forEach {
            val area = EventUtil.basicChecks(player, world as? World, it) ?: return@forEach
            if (area.getConfig<VanillaConfig>().placeBlock())
                return@forEach

            player.sendAreaActionBarMessage("message.lmr.protection.place", TextFormatting.RED, area.name)
            event.isCanceled = true
            return@run
        }
    }


    fun findLine(player: PlayerEntity, firstPos: BlockPos, skipRaytrace: Boolean): BlockPos? {
        val look = BuildModes.getPlayerLookVec(player)
        val start = Vector3d(player.x, player.y + player.eyeHeight.toDouble(), player.z)
        val criteriaList: MutableList<Criteria> = ArrayList<Criteria>(3)
        val xBound = BuildModes.findXBound(firstPos.x.toDouble(), start, look)
        criteriaList.add(Criteria(xBound, firstPos, start))
        val yBound = BuildModes.findYBound(firstPos.y.toDouble(), start, look)
        criteriaList.add(Criteria(yBound, firstPos, start))
        val zBound = BuildModes.findZBound(firstPos.z.toDouble(), start, look)
        criteriaList.add(Criteria(zBound, firstPos, start))
        val reach = ReachHelper.getPlacementReach(player) * 4
        criteriaList.removeIf { criteriax: Criteria ->
            !criteriax.isValid(
                start,
                look,
                reach,
                player,
                skipRaytrace
            )
        }
        return if (criteriaList.isEmpty()) {
            null
        } else {
            var selected = criteriaList[0]
            if (criteriaList.size > 1) {
                for (i in 1 until criteriaList.size) {
                    val criteria = criteriaList[i]
                    if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                        if (criteria.distToPlayerSq < selected.distToPlayerSq) {
                            selected = criteria
                        }
                    } else if (criteria.distToLineSq < selected.distToLineSq) {
                        selected = criteria
                    }
                }
            }
            BlockPos(selected.lineBound!!)
        }
    }


    internal class Criteria(var planeBound: Vector3d, firstPos: BlockPos, start: Vector3d) {
        var lineBound: Vector3d?
        var distToLineSq: Double
        var distToPlayerSq: Double

        init {
            lineBound = toLongestLine(planeBound, firstPos)
            distToLineSq = lineBound!!.subtract(planeBound).lengthSqr()
            distToPlayerSq = planeBound.subtract(start).lengthSqr()
        }

        private fun toLongestLine(boundVec: Vector3d, firstPos: BlockPos): Vector3d? {
            val bound = BlockPos(boundVec)
            var firstToSecond = bound.subtract(firstPos)
            firstToSecond = BlockPos(
                abs(firstToSecond.x.toDouble()),
                abs(firstToSecond.y.toDouble()),
                abs(firstToSecond.z.toDouble())
            )
            val longest = max(firstToSecond.x.toDouble(), max(firstToSecond.y.toDouble(), firstToSecond.z.toDouble()))
                .toInt()
            return if (longest == firstToSecond.x) {
                Vector3d(bound.x.toDouble(), firstPos.y.toDouble(), firstPos.z.toDouble())
            } else if (longest == firstToSecond.y) {
                Vector3d(firstPos.x.toDouble(), bound.y.toDouble(), firstPos.z.toDouble())
            } else {
                if (longest == firstToSecond.z) Vector3d(
                    firstPos.x.toDouble(),
                    firstPos.y.toDouble(),
                    bound.z.toDouble()
                ) else null
            }
        }

        fun isValid(
            start: Vector3d?,
            look: Vector3d?,
            reach: Int,
            player: PlayerEntity?,
            skipRaytrace: Boolean
        ): Boolean {
            return BuildModes.isCriteriaValid(
                start, look, reach, player, skipRaytrace, lineBound, planeBound,
                distToPlayerSq
            )
        }
    }


}