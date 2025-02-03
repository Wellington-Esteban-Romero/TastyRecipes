package com.tasty.recipes.utils

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat


/**
 * Adjusts the margins of the view to account for the system bars' insets
 * (such as the status bar and navigation bar) using `WindowInsets` and apply custom margins.
 *
 * This function listens for window insets and updates the layout parameters of the view,
 * ensuring that the view content is correctly positioned within the window and does not overlap
 * with system bars.
 *
 * Example usage:
 * ```
 * view.setWindowInsets(Insets.of(0, 0, 10, 10))
 * ```
 * This would add an additional margin of 10px on the right and the bottom, in addition to the system bar insets.
 *
 * @param marginInsets Additional margins to be added on top of the system bars' insets.
 *                     The default value is [Insets.of(0, 0, 0, 0)], meaning no extra margin.
 *                     You can customize the top, right, bottom, and left margins.
 *
 * @receiver The view to which the margin adjustments will be applied.
 *           This view should have `MarginLayoutParams` as its layout parameters.
 */

@RequiresApi(Build.VERSION_CODES.R)
fun View.setWindowInsets() {
    setOnApplyWindowInsetsListener { view, insets ->
        val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
        insets
    }

}