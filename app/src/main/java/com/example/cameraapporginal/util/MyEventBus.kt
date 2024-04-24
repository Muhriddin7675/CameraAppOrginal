package com.example.cameraapporginal.util

object MyEventBus {
    var connectedEvent : (() -> Unit)?= null
    var notConnectedEvent : (() -> Unit)?= null
    var changeSavedScreen:(() -> Unit)?= null
}