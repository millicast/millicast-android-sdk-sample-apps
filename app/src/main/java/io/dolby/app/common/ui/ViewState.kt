package io.dolby.app.common.ui

/**
 * An analogous enum to the traditional Android view state model where in the state of a view can be
 * determined. For those familiar with the traditional android view of having a view state selector for
 * applying drawables or colors, this should feel like the jetpack compose equivalent.
 */
enum class ViewState {
    Unknown,
    Pressed,
    Selected,
    Focused,
    Disabled;

    companion object Factory {
        fun from(isPressed: Boolean, isSelected: Boolean, isFocused: Boolean, isEnabled: Boolean): ViewState {
            return if (isPressed) {
                Pressed
            } else if (isFocused) {
                Focused
            } else if (isSelected) {
                Selected
            } else if (!isEnabled) {
                Disabled
            } else Unknown
        }
    }
}
