package com.gmits.watchfacetouchsample

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

class NeonMachineTextView : TextView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        val tf = Typeface.createFromAsset(context.assets, "fonts/Neon machine.ttf")
        typeface = tf
    }

}