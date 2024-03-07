package fr.bloctave.lmr.config

import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseRangeValue
import fr.bloctave.lmr.config.util.PreciseValue

object ServerConfig : BasicConfig() {

    @ConfigValue
    val disableAutoClaiming: PreciseValue<Boolean> = this.configure("disableClaiming", false, "Whether players have to claim chunks using '/lm claim'")

    @ConfigValue
    val claimRequest: PreciseValue<Boolean> = this.configure("claimRequest", false, "If true then the 'claim' command will create a request rather than take instant effect", "An OP will then need to use the 'approve' command to accept the request")

    @ConfigValue
    val maxAreaCapacity: PreciseRangeValue<Int> = this.configureInRange("maxAreaCapacity", -1, -1, Int.MAX_VALUE, "The max amount of player an area can have", "Use -1 for no limit")

    @ConfigValue
    val maxAreaLength: PreciseRangeValue<Int> = this.configureInRange("maxAreaLength", -1, -1, Int.MAX_VALUE, "The maximum length of an area (along x)", "Use -1 for no limit")

    @ConfigValue
    val maxAreaWidth: PreciseRangeValue<Int> = this.configureInRange("maxAreaWidth", -1, -1, Int.MAX_VALUE, "The maximum width of an area (along z)", "Use -1 for no limit")

    @ConfigValue
    val maxAreaSize: PreciseRangeValue<Double> = this.configureInRange("maxAreaSize", -1.0, -1.0, Double.MAX_VALUE, "The maximum of an area", "Use -1 for no limit")

    @ConfigValue
    val maxAreasCanOwn: PreciseRangeValue<Int> = this.configureInRange("maxAreaCanOwn", -1, -1, Int.MAX_VALUE, "The maximum amount of areas a player can own", "Use -1 for no limit")

}