package com.lifon.music

object PlayerCallbacks {
    var currentTrackId: Int? = null

    var onNext: (() -> Unit)? = null
    var onPrev: (() -> Unit)? = null
    var onPlayPause: (() -> Unit)? = null
    var onLike: (() -> Unit)? = null
    var onLikeStateChanged: ((Int, Boolean) -> Unit)? = null
}