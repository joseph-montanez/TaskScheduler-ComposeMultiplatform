package com.shabb

class AppearanceManager {
    companion object {
        init {
            try {
                val path = System.getProperty("user.dir") + "/build/nativeLibs/libAppearanceObserver.dylib"
                System.load(path)
            } catch (e: UnsatisfiedLinkError) {
                println("Failed to load the library: ${e.message}")
            }
        }
    }

    external fun initializeAppearanceObserver(callback: AppearanceChangeCallback)
}