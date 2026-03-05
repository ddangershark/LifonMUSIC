package com.lifon.music

object PlayerCallbacks {
    var onNext: (() -> Unit)? = null
    var onPrev: (() -> Unit)? = null
}