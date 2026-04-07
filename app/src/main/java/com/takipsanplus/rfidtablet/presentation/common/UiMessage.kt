package com.takipsanplus.rfidtablet.presentation.common

import androidx.annotation.StringRes

sealed interface UiMessage {
    data class Resource(@StringRes val resId: Int) : UiMessage
    data class Text(val value: String) : UiMessage
}
