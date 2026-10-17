package dev.androidisland.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dev.androidisland.TorchInfo

class TorchBridge(private val context: Context, private val changed: (TorchInfo?) -> Unit) {
    private val manager = context.getSystemService(CameraManager::class.java)
    private var cameraId: String? = null
    private var current = TorchInfo(false, false)
    private var registered = false
    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) { current = TorchInfo(enabled, true); changed(current) }
        }
        override fun onTorchModeUnavailable(id: String) {
            if (id == cameraId) { current = TorchInfo(false, false); changed(current) }
        }
    }
    fun start() {
        if (registered) return
        runCatching {
            cameraId = manager.cameraIdList.firstOrNull {
                val c = manager.getCameraCharacteristics(it)
                c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true && c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            manager.registerTorchCallback(context.mainExecutor, callback)
            registered = true
        }.onFailure { changed(TorchInfo(false, false)) }
    }
    fun stop() { if (registered) manager.unregisterTorchCallback(callback); registered = false }
    fun toggle(error: (String) -> Unit) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { error("Enable camera permission to control the flashlight"); return }
        val id = cameraId
        if (id == null || !current.available) { error("Flashlight is unavailable or in use by the camera"); return }
        runCatching { manager.setTorchMode(id, !current.enabled) }.onFailure { error("Flashlight is unavailable. Try again when the camera is free.") }
    }
}
