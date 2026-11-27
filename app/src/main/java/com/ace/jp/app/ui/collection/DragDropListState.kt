package com.ace.jp.app.ui.collection

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class DragDropListState<T>(
    val lazyListState: LazyListState,
    private val keySelector: (T) -> Any,
    private val getOnCommitOrder: () -> (List<T>) -> Unit
) {
    var items by mutableStateOf<List<T>>(emptyList())
        private set
    var draggedItemKey by mutableStateOf<Any?>(null)
        private set
    var draggedOffsetY by mutableFloatStateOf(0f)
        private set
    var autoScrollSpeed by mutableFloatStateOf(0f)
        private set

    fun syncExternalItems(external: List<T>) {
        if (draggedItemKey == null) {
            items = external
        }
    }

    fun onDragStart(itemKey: Any) {
        draggedItemKey = itemKey
        draggedOffsetY = 0f
        autoScrollSpeed = 0f
    }

    fun onDrag(itemKey: Any, deltaY: Float) {
        if (draggedItemKey != itemKey) return
        draggedOffsetY += deltaY

        val currentIndex = items.indexOfFirst { keySelector(it) == itemKey }
        if (currentIndex == -1) return

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val currentInfo = visibleItems.firstOrNull { it.index == currentIndex } ?: return

        val currentTop = currentInfo.offset + draggedOffsetY
        val currentBottom = currentTop + currentInfo.size

        val nextInfo = visibleItems.firstOrNull { it.index == currentIndex + 1 }
        val prevInfo = visibleItems.firstOrNull { it.index == currentIndex - 1 }

        if (nextInfo != null && currentBottom > nextInfo.offset + nextInfo.size / 2f) {
            val mutable = items.toMutableList()
            mutable[currentIndex] = mutable[currentIndex + 1].also {
                mutable[currentIndex + 1] = mutable[currentIndex]
            }
            items = mutable
            draggedOffsetY -= (nextInfo.offset - currentInfo.offset)
        } else if (prevInfo != null && currentTop < prevInfo.offset + prevInfo.size / 2f) {
            val mutable = items.toMutableList()
            mutable[currentIndex] = mutable[currentIndex - 1].also {
                mutable[currentIndex - 1] = mutable[currentIndex]
            }
            items = mutable
            draggedOffsetY += (currentInfo.offset - prevInfo.offset)
        }

        val viewportStart = lazyListState.layoutInfo.viewportStartOffset
        val viewportEnd = lazyListState.layoutInfo.viewportEndOffset
        val threshold = 80f
        autoScrollSpeed = when {
            currentBottom > viewportEnd - threshold -> 14f
            currentTop < viewportStart + threshold -> -14f
            else -> 0f
        }
    }

    fun onDragEnd() {
        if (draggedItemKey != null) {
            val committed = items
            draggedItemKey = null
            draggedOffsetY = 0f
            autoScrollSpeed = 0f
            getOnCommitOrder().invoke(committed)
        }
    }
}

@Composable
fun <T> rememberDragDropListState(
    externalItems: List<T>,
    keySelector: (T) -> Any,
    onCommitOrder: (List<T>) -> Unit
): DragDropListState<T> {
    val lazyListState = rememberLazyListState()
    val latestCommit by rememberUpdatedState(onCommitOrder)
    val state = remember(lazyListState) {
        DragDropListState(
            lazyListState = lazyListState,
            keySelector = keySelector,
            getOnCommitOrder = { latestCommit }
        )
    }

    LaunchedEffect(externalItems) {
        state.syncExternalItems(externalItems)
    }

    LaunchedEffect(state.autoScrollSpeed, state.draggedItemKey) {
        while (state.draggedItemKey != null && state.autoScrollSpeed != 0f) {
            val scrolled = state.lazyListState.scrollBy(state.autoScrollSpeed)
            val key = state.draggedItemKey ?: break
            if (scrolled == 0f) break
            state.onDrag(key, scrolled)
            delay(16L.milliseconds)
        }
    }

    return state
}