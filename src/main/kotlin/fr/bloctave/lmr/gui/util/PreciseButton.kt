package fr.bloctave.lmr.gui.util

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import fr.bloctave.lmr.gui.LMScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent

abstract class PreciseButton(
    val screen: PreciseScreen,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    @get:JvmName("_getIconX")
    protected val iconX: Int,
    @get:JvmName("_getIconY")
    protected val iconY: Int,
    text: String,
) : Button(screen.guiLeft + x, screen.guiTop + y, width, height, StringTextComponent(text), { }) {
    protected var hasIcon = false
    protected var drawWhenDisabled = false
    protected var textOffset = 0
    val tooltip: MutableList<ITextComponent> = mutableListOf()


    protected open fun getIconX(): Int = iconX
    protected open fun getIconY(): Int = iconY

    protected open fun getTextColour(): Int = if (active) LMScreen.TEXT_COLOUR_ACTIVE else LMScreen.TEXT_COLOUR_INACTIVE

    protected fun setTooltip(vararg textComponents: ITextComponent) {
        tooltip.clear()
        textComponents.forEach { tooltip.add(it) }
    }

    protected open fun drawText(matrixStack: MatrixStack, font: FontRenderer): Unit =
        screen.drawString(matrixStack, message.string, x + textOffset, y + (height - 8) / 2, getTextColour())

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible || (!drawWhenDisabled && !active))
            return
        val mc = Minecraft.getInstance()
        //if (hasIcon) {
            mc.textureManager.bind(screen.textureResLoc)
            RenderSystem.color3f(1F, 1F, 1F)
            blit(
                matrixStack,
                x,
                y,
                getIconX().toFloat(),
                getIconY().toFloat(),
                width,
                height,
                screen.textureWidth,
                screen.textureHeight
            )
        //}
        if (message.string.isNotBlank())
            drawText(matrixStack, mc.font)
    }

    override fun onPress() {
        this.onButtonPress()
        super.onPress()
    }

    abstract fun onButtonPress()
}