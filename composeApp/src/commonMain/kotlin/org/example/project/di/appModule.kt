package org.example.project.di

import org.example.project.viewmodel.ChatViewModel
import org.example.project.viewmodel.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    // single { ... } 创建一个单例
    factory { ChatViewModel(get()) }
    factory { HomeViewModel(get()) }

}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(appModule, sharedModule, platformModule())
        // 调用传入的平台特定配置
        appDeclaration()
        // 加载通用模块和平台模块
    }
}