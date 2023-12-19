package fr.bloctave.lmr.gui.util

import fr.bloctave.lmr.gui.LMScreen


abstract class ListButton<T : Any>(screen: PreciseScreen, val list: ListWidget<T>, val id: Int, iconX: Int, iconY: Int) : PreciseButton(screen, list.x, list.y + (id * list.buttonHeight), list.width, list.buttonHeight, iconX, iconY, "") {


    var value: String = ""
    //var selected = false

    init {
        textOffset = 1
        drawWhenDisabled = true
    }

    override fun getTextColour(): Int = LMScreen.TEXT_COLOUR_ACTIVE

    fun setSelected(selected: Boolean) {
        hasIcon = selected
    }
    override fun onButtonPress() {
        screen.message = null

        list.buttons.forEach {
            if (it.id == id && it == this) {
                it.setSelected(true)
                list.selectedMemberIndex = list.startIndex + id
                list.startIndex += if (list.startIndex + id == 0 || list.selectedMemberIndex > list.members.size - list.size + 1) 0 else (id - 1)
            } else {
                it.setSelected(false)
            }
        }

        list.update()
    }
    abstract fun updateValue(value: Pair<String, T>?)
}

