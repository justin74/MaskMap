package com.justin.huang.maskmap.di

import android.app.Application
import com.justin.huang.maskmap.MaskMapApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        MapsActivityModule::class]
)
interface AppComponent {
    // provide application dependency
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(maskMapApp: MaskMapApp)
}