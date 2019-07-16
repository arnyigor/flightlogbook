package com.arny.adapters

import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle


class ADSpannable() {
    private var mSpanStr: SpannableString? = null

    constructor(s: String) : this() {
        mSpanStr = SpannableString(s)
    }

    fun setSpan(input: String, o: Any?): ADSpannable {
        val ss = input.trim()
        if (mSpanStr == null)
            mSpanStr = SpannableString(ss)
        val index = mSpanStr!!.toString().indexOf(ss)
        if (index != -1) {
            if (o != null) {
                mSpanStr!!.setSpan(o, index, index + ss.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        return this
    }

    fun setSpans(input: String, spanStyles: ArrayList<CharacterStyle>?): ADSpannable {
        val ss = input.trim()
        if (mSpanStr == null)
            mSpanStr = SpannableString(ss)
        val index = mSpanStr!!.toString().indexOf(ss)
        if (index != -1) {
            if (spanStyles != null) {
                for (style in spanStyles) {
                    mSpanStr!!.setSpan(style, index, index + ss.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }
        return this
    }

    fun get(): SpannableString? {
        return mSpanStr
    }
}