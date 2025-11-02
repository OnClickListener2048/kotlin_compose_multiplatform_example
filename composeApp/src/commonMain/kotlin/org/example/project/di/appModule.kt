package org.example.project.di

import org.example.project.viewmodel.ChatViewModel
import org.example.project.viewmodel.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    // single { ... } 创建一个单例
    println("appModule")
    factory {
        println("ChatViewModel")
        ChatViewModel(get())
    }

    factory {
        println("HomeViewModel")
        HomeViewModel(get())
    }

}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    println("initKoin")
    startKoin {
        printLogger(Level.DEBUG)
        modules(sharedModule,appModule , platformModule())
        // 调用传入的平台特定配置
        appDeclaration()
        // 加载通用模块和平台模块
    }
}