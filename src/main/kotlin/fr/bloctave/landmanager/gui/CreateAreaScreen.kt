package fr.bloctave.landmanager.gui

import fr.bloctave.landmanager.LandManager
import fr.bloctave.landmanager.data.areas.Area
import fr.bloctave.landmanager.message.MessageCreateArea
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import org.lwjgl.glfw.GLFW

class CreateAreaScreen(
	private val dim: ResourceLocation,
	private val pos1: BlockPos,
	private val pos2: BlockPos
) : LMScreen("Create Area", "gui_create_area", 118, 42, 130, 42) {
	private lateinit var nameInputField: TextFieldWidget
	private lateinit var extendCheckBox: ToggleButton

	private var sentCreateMessage = false

	override fun init() {
		super.init()
		nameInputField = TextFieldWidget(font, guiLeft + 4, guiTop + 15, 110, 9, StringTextComponent("")).apply {
			setEnableBackgroundDrawing(false)
			setFocused2(true)
			this@CreateAreaScreen.setFocusedDefault(this)
		}
		children += nameInputField
		extendCheckBox = addButton(object : ToggleButton(4, 26, 118, 0, I18n.format("gui.lm.create.checkbox")) {
			override fun getTextColour(): Int = TEXT_COLOUR_TITLE
		})
		addButton(object : Button(
			guiLeft + 71,
			guiTop + 26,
			43,
			12,
			TranslationTextComponent("gui.lm.create.confirm"),
			{ complete() }) {
			override fun renderWidget(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) =
				drawCenteredString(matrixStack, font, message, x + width / 2, y + 2, TEXT_COLOUR_ACTIVE)
		})
	}

	override fun tick() {
		super.tick()
		nameInputField.tick()
	}

	override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
		super.render(matrixStack, mouseX, mouseY, partialTicks)
		nameInputField.render(matrixStack, mouseX, mouseY, partialTicks)
		drawLangString(matrixStack, "gui.lm.create.area", 5 + guiLeft, 5 + guiTop)
	}

	override fun keyPressed(keyCode: Int, p2: Int, p3: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			complete()
			return true
		}
		return nameInputField.keyPressed(keyCode, p2, p3) || super.keyPressed(keyCode, p2, p3)
	}

	// Used by MessageCreateAreaReply when the name already exists
	fun clearTextField() {
		nameInputField.text = ""
		sentCreateMessage = false
	}

	// Sends message to the server to add the new area
	// Doesn't close the GUI - let the returned message do it if successful
	private fun complete() {
		if (sentCreateMessage)
			return
		val areaName = nameInputField.text.trim()
		if (areaName.isNotEmpty()) {
			val area = Area(areaName, dim, pos1, pos2)
			if (extendCheckBox.isOn)
				area.extendToMinMaxY(minecraft!!.world!!)
			sentCreateMessage = true
			LandManager.NETWORK.sendToServer(MessageCreateArea(area))
		}
	}
}
