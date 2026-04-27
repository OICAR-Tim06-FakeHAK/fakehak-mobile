package hr.algebra.myapplication

import android.content.Context

object AppContext {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun get(): Context {
        if (!::context.isInitialized) {
            throw IllegalStateException("AppContext has not been initialized. Call AppContext.init(context) in your Application class.")
        }
        return context
    }
}

