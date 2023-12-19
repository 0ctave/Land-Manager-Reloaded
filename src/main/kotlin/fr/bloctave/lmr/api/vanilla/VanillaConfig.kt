package fr.bloctave.lmr.api.vanilla

import fr.bloctave.lmr.config.util.AreaConfigImpl
import fr.bloctave.lmr.config.util.ConfigValue
import fr.bloctave.lmr.config.util.PreciseRangeValue
import fr.bloctave.lmr.config.util.PreciseValue

class VanillaConfig : AreaConfigImpl() {

    @ConfigValue
    var showArea: PreciseValue<Boolean> =
        this.configure("showArea", false, "If true the boundaries of an area will be shown by default")

    @ConfigValue
    var passiveSpawning: PreciseValue<Boolean> =
        this.configure("passiveSpawning", false, "If true passive entities can spawn in an area by default")

    @ConfigValue
    var hostileSpawning: PreciseValue<Boolean> =
        this.configure("hostileSpawning", false, "If true hostile entities can spawn in an area by default")

    @ConfigValue
    var explosion: PreciseValue<Boolean> =
        this.configure("explosion", false, "If true explosions can destroy blocks in an area by default")

    @ConfigValue
    var interactItem: PreciseValue<Boolean> =
        this.configure("interactItem", false, "If true other players can interact (right click) with items in an area by default")

    @ConfigValue
    var interactBlock: PreciseValue<Boolean> =
        this.configure("interactBlock", false, "If true other players can interact (right click) with blocks in an area by default")

    @ConfigValue
    var useItem: PreciseValue<Boolean> =
        this.configure("useItem", false, "If true other players can use items in an area by default")

    @ConfigValue
    var placeBlock: PreciseValue<Boolean> =
        this.configure("placeBlock", false, "If true other players can place blocks in an area by default")

    @ConfigValue
    var breakBlock: PreciseValue<Boolean> =
        this.configure("breakBlock", false, "If true other players can break blocks in an area by default")

    @ConfigValue
    var pistonAction: PreciseValue<Boolean> =
        this.configure("pistonAction", false, "If true pistons can move blocks in an area by default")

    @ConfigValue
    var lifetime: PreciseRangeValue<Double> =
        this.configureInRange("lifetime", -1.0, -1.0, Double.MAX_VALUE, "The default maximum amount of ticks an area can exist (20 ticks = 1 second)", "Use -1.0 for no limit", "Must be a DOUBLE : 1 hour = 3600 seconds = 72000 ticks => 72.0E3")

}