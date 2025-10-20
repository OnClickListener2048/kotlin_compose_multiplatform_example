package org.example.project.di

import org.example.project.database.Database
import org.example.project.network.ApiService
import org.example.project.network.MainRepository
import org.example.project.network.provideHttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// 1. 定义平台无关的模块
val sharedModule = module {
    // single { ... } 创建一个单例
    single { provideHttpClient() }

    single { ApiService(get()) }
    // Database 依赖 DatabaseDriverFactory。Koin 会自动从 platformModule 中寻找并注入它。
    single { Database(get()).chatItemQueries }

    single { MainRepository(get(),get()) }



}

// 2. 期望一个平台特定的模块，这个模块将负责提供 DatabaseDriverFactory
expect fun platformModule(): Module

// 3. 创建一个公共的初始化函数
