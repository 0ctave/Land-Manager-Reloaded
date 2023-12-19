package fr.bloctave.lmr.handler

import fr.bloctave.lmr.LandManager
import fr.bloctave.lmr.config.ClientConfig
import fr.bloctave.lmr.data.areas.Area
import fr.bloctave.lmr.util.AreaRenderer
import fr.bloctave.lmr.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.Mod
import java.awt.Color
import kotlin.random.Random

@Mod.EventBusSubscriber(Dist.CLIENT, modid = LandManager.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ClientEventHandler {
	private val mc = Minecraft.getInstance()
	private val rand = Random.Default
	private var renderAll = true
	private val areasToRender = mutableSetOf<String>()
	private val colourCache = mutableMapOf<String, Color>()
	private var lastAreaInside: LastDetails? = null

	fun isAreaBeingRendered(areaName: String): Boolean = areasToRender.contains(areaName)

	// Used by command
	fun setRenderArea(areaName: String) {
		renderAll = false
		areasToRender.clear()
		areasToRender += areaName
		colourCache.clear()
	}

	// Used by GUI
	fun setRenderArea(areaName: String, show: Boolean) {
		renderAll = false
		if (show)
			areasToRender += areaName
		else {
			areasToRender -= areaName
			colourCache -= areaName
		}
	}

	fun toggleRenderAll() {
		renderAll = areasToRender.isNotEmpty() || !renderAll
		areasToRender.clear()
		colourCache.clear()
		mc.player!!.displayClientMessage(
			TranslationTextComponent("message.lmr.areas.${if (renderAll) "show" else "hide"}")
				.withStyle(TextFormatting.GREEN),
			true
		)
	}

	private fun randFloat(min: Float) = min + rand.nextFloat() * (1F - min)

	private fun getColour(areaName: String): Color =
		colourCache.computeIfAbsent(areaName) { Color.getHSBColor(rand.nextFloat(), randFloat(0.3F), randFloat(0.7F)) }

	@SubscribeEvent
	fun renderAreas(event: RenderWorldLastEvent) {
		if (!renderAll && areasToRender.isEmpty())
			return

		val cap = mc.level!!.areasCap
		val matrixStack = event.matrixStack
		val view = mc.gameRenderer.mainCamera.position

		if (renderAll)
			cap.getNearbyAreas(mc.player!!.blockPosition()).forEach {
				AreaRenderer.renderArea(matrixStack, view, it, getColour(it.name), ClientConfig.areaBoxNearbySides())
			}
		else
			areasToRender.stream()
				.map { cap.getArea(it) }
				.filter { it != null }
				.forEach { AreaRenderer.renderArea(matrixStack, view, it!!, getColour(it.name), true) }
	}

	@SubscribeEvent
	fun playerTick(event: TickEvent.PlayerTickEvent): Unit = event.run {
		// Send title message to client when moving into different area
		if (!ClientConfig.titleOnAreaChange() || side != LogicalSide.CLIENT || phase != TickEvent.Phase.END)
			return

		val cap = player.level.areasCap
		val pos = player.blockPosition()
		/*lastAreaInside?.let {
			// If still inside the same area, then no need to check further
			// Worth noting that this will only work while areas aren't allowed to overlap
			val area = it.area ?: return@let
			if (area.intersects(pos) && cap.hasArea(area.name))
				return
		}*/

		if (lastAreaInside == null || lastAreaInside!!.updateAndCheckPlayerPos(player)) {
			val areas = cap.intersectingAreas(pos)
			val area = cap.getSmallestArea(areas)
			if (lastAreaInside == null || lastAreaInside!!.updateAndCheckArea(area)) {
				if (lastAreaInside == null)
					lastAreaInside = LastDetails(area, areas, player)

				// Display area change message
				val text = area?.let { StringTextComponent(it.name) }
					?: TranslationTextComponent("misc.lmr.wilderness")
				text.withStyle(
					when {
						area == null -> ClientConfig.titleColourWilderness()
						area.isMember(player.uuid) -> ClientConfig.titleColourAreaMember()
						else -> ClientConfig.titleColourAreaOutsider()
					}
				)
				mc.gui.run {
					// Set area name as sub-title
					setTitles(null, text, 0, 0, 0) // displayTitle
					// Display empty title so that sub-title is shown
					setTitles(StringTextComponent(""), null, 0, 0, 0) // displayTitle
				}
			}
		}
	}

	class LastDetails(var area: Area?, var areas: Set<Area>, player: PlayerEntity) {
		var pos: BlockPos = player.blockPosition()
		private var dim: ResourceLocation = player.level.dimension().location()

		fun updateAndCheckPlayerPos(player: PlayerEntity): Boolean {
			val result = player.blockPosition() != pos || player.level.dimension().location() != dim
			pos = player.blockPosition()
			dim = player.level.dimension().location()
			return result
		}

		fun updateAndCheckArea(area: Area?): Boolean {
			val result = area == null != (this.area == null) || area != null && area.name != this.area?.name
			this.area = area
			return result
		}
	}



	/*@SubscribeEvent
	fun onGuiOpen(event: GuiOpenEvent) {
		if (event.gui is MainMenuScreen) {
			event.gui = CustomMainMenuScreen()
		}
	}

	class CustomMainMenuScreen : MainMenuScreen() {
		override fun init() {
			super.init()
			addButton(Button(width / 2 - 100, height / 4, 200, 20, StringTextComponent("Connect to Local Server")) {
				Minecraft.getInstance().setScreen(MultiplayerScreen(this))
			})
		}
	}*/
}
