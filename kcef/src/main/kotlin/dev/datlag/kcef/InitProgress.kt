package dev.datlag.kcef

interface InitProgress {

    fun locating() {}

    fun downloading(progress: Float) {}

    fun extracting() {}

    fun install() {}

    fun initializing() {}

    fun initialized() {}

    class Builder {
        private var locatingCallback: NoProgressCallback = NoProgressCallback {  }
        private var downloadingCallback: ProgressCallback = ProgressCallback {  }
        private var extractingCallback: NoProgressCallback = NoProgressCallback {  }
        private var installCallback: NoProgressCallback = NoProgressCallback {  }
        private var initializingCallback: NoProgressCallback = NoProgressCallback {  }
        private var initializedCallback: NoProgressCallback = NoProgressCallback {  }

        fun onLocating(callback: NoProgressCallback) = apply {
            locatingCallback = callback
        }

        fun onDownloading(callback: ProgressCallback) = apply {
            downloadingCallback = callback
        }

        fun onExtracting(callback: NoProgressCallback) = apply {
            extractingCallback = callback
        }

        fun onInstall(callback: NoProgressCallback) = apply {
            installCallback = callback
        }

        fun onInitializing(callback: NoProgressCallback) = apply {
            initializingCallback = callback
        }

        fun onInitialized(callback: NoProgressCallback) = apply {
            initializedCallback = callback
        }

        fun build(): InitProgress = object : InitProgress {
            override fun locating() {
                locatingCallback()
            }

            override fun downloading(progress: Float) {
                downloadingCallback(progress)
            }

            override fun extracting() {
                extractingCallback()
            }

            override fun install() {
                installCallback()
            }

            override fun initializing() {
                initializingCallback()
            }

            override fun initialized() {
                initializedCallback()
            }
        }

        fun interface NoProgressCallback {
            operator fun invoke()
        }

        fun interface ProgressCallback {
            operator fun invoke(progress: Float)
        }
    }
}