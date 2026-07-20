package org.example.project.di

import org.example.project.database.Database
import org.example.project.network.provideHttpClient
import org.example.project.repo.ChatRepository
import org.example.project.repo.ApiKeyRepository
import org.example.project.chat.OpenAICompatibleProvider
import org.example.project.chat.ChatProvider
import org.example.project.chat.ProviderType
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val sharedModule = module {
    println("sharedModule")

    single {
        println("HttpClient")
        provideHttpClient()
    }

    single {
        println("Database")
        Database(get())
    }

    single {
        println("WatsonQueries")
        get<Database>().watsonQueries
    }

    single {
        println("ChatRepository")
        ChatRepository(get())
    }

    single {
        println("ApiKeyRepository")
        ApiKeyRepository(get())
    }

    single<ChatProvider> {
        println("OpenAICompatibleProvider")
        OpenAICompatibleProvider(ProviderType.OpenAI, get())
    }
}

expect fun platformModule(): Module

fun initKoin2(appDeclaration: KoinAppDeclaration = {}) {
    println("initKoin")
    startKoin {
        printLogger(Level.DEBUG)
        modules(sharedModule, platformModule())
        appDeclaration()
    }
}
