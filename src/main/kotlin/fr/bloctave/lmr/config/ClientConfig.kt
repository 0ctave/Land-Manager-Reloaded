package fr.bloctave.lmr.config

import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseRangeValue
import fr.bloctave.lmr.config.util.PreciseValue
import fr.bloctave.lmr.config.util.configureEnum
import fr.bloctave.lmr.util.ColorUtil
import net.minecraft.util.text.TextFormatting

object ClientConfig : BasicConfig() {

    @ConfigValue
    var showDistance: PreciseRangeValue<Int> = this.configureInRange("showDistance", 16, 0, Int.MAX_VALUE, "If true passive entities can spawn in an area by default")

    @ConfigValue
    val showChatLogs: PreciseValue<Boolean> = this.configure("showChatLogs", false, "Toggles whether OPs will see area changes in their chat")

    @ConfigValue
    val areaNameScale: PreciseRangeValue<Double> = this.configureInRange("areaNameScale", 1.0, 0.0, Double.MAX_VALUE, "The scale of the area label that's rendered")

    @ConfigValue
    val areaBoxAlpha: PreciseRangeValue<Double> = this.configureInRange("areaBoxAlpha", 0.2, 0.0, 1.0, "The alpha for the sides of area boxes rendered in the world")

    @ConfigValue
    val areaBoxEdgeThickness: PreciseRangeValue<Double> = this.configureInRange("areaBoxEdgeThickness", 0.025, 0.0, Double.MAX_VALUE, "The thickness of area box edges rendered in the world")

    @ConfigValue
    val areaBoxNearbySides: PreciseValue<Boolean> = this.configure("areaBoxNearbySides", true, "Toggles whether to render area box sides when rendering nearby areas")

    @ConfigValue
    val titleOnAreaChange: PreciseValue<Boolean> = this.configure("titleOnAreaChange", true, "Toggles whether title messages should be displayed when moving into a different area")

    @ConfigValue
    val titleColourWilderness: PreciseValue<TextFormatting> = this.configureEnum("titleColourWilderness",
        TextFormatting.GRAY, ColorUtil.COLOURS, "The colour of the area change title when you move into the Wilderness")

    @ConfigValue
    val titleColourAreaMember: PreciseValue<TextFormatting> = this.configureEnum("titleColourAreaMember",
        TextFormatting.GREEN, ColorUtil.COLOURS, "The colour of the area change title when you move into an area you're a member of")

    @ConfigValue
    val titleColourAreaOutsider: PreciseValue<TextFormatting> = this.configureEnum("titleColourAreaOutsider",
        TextFormatting.RED, ColorUtil.COLOURS, "The colour of the area change title when you move into an area you're not a member of")

}