package fr.bloctave.lmr.gui.util

import java.util.*
import kotlin.reflect.KClass

class ListWidget<T : Any>(
    screen: PreciseScreen,
    val x: Int,
    val y: Int,
    val width: Int,
    val buttonHeight: Int,
    val size: Int,
    navX: Int,
    navY: Int,
    navDistance: Int,
    buttonClass: KClass<out ListButton<T>>
) {


    var members: MutableList<Pair<String, T>> = ArrayList()

    var startIndex = 0
    var selectedMemberIndex = -1


    val buttons: Array<ListButton<T>> = Array(size) { buttonClass.constructors.first().call(screen, this, it) }

    var upButton: ArrowButton = ArrowButton(screen, navX, navY, true)
    var downButton: ArrowButton = ArrowButton(screen, navX, navY + navDistance + 13, false)


    fun update() {
        if (members.isEmpty())
            return

        members.sortWith(Comparator.comparing(Pair<String, T>::first) { s1, s2 -> s1.compareTo(s2, true) })

        //members.sortWith(Comparator.comparing { s1, s2 -> s1.compareTo(s2, true) })

        // Update members list
        buttons.forEach { button ->
            val index = button.id + startIndex
            button.updateValue(if (index < members.size) members[index] else null)
            button.setSelected(selectedMemberIndex == index)
            //button.update()
        }

        // Update arrows
        upButton.active = canScrollUp()
        downButton.active = canScrollDown()
    }

    fun addMember(name: Pair<String, T>) {
        members.add(name)
        update()
    }

    fun removeMember(name: Pair<String, T>) {
        members.remove(name)
        update()
    }

    inner class ArrowButton(screen: PreciseScreen, x: Int, y: Int, val isUp: Boolean) :
        PreciseButton(screen, x, y, 10, 13, 162, 84, "") {

        override fun getIconY(): Int = if (isUp) iconY else iconY + height

        override fun onButtonPress() {
            screen.message = null


            startIndex += if (isUp) -1 else 1


            update()
        }
    }

    private fun canScrollUp(): Boolean = startIndex > 0

    private fun canScrollDown(): Boolean = members.size > startIndex + size

}