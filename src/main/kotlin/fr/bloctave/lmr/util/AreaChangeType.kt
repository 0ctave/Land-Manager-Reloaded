package fr.bloctave.lmr.util

import java.util.*

enum class AreaChangeType {
	CREATE,
	DELETE,
	ALLOCATE,
	CLEAR_ALLOCATION,
	CLAIM;

	val unlocalisedName = "area.change.${name.lowercase(Locale.getDefault())}"
}
