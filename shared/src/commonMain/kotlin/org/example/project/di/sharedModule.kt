package org.example.project.di

import org.example.project.database.Database
import org.example.project.network.provideHttpClient
import org.example.project.repo.ChatRepository
import org.example.project.repo.ApiKeyRepository
import org.example.project.chat.OpenAICompatibleProvider
import org.example.project.chat.ChatProvider
import org.example.project.chat.ProviderType
import org.example.project.core.context.ContextEngine
import org.example.project.core.context.FilePromptProvider
import org.example.project.core.context.HistoryPromptProvider
import org.example.project.core.context.MemoryPromptProvider
import org.example.project.core.context.SystemPromptProvider
import org.example.project.core.context.TemplatePromptProvider
import org.example.project.core.context.WorkspacePromptProvider
import org.example.project.feature.files.FileAssetRepository
import org.example.project.feature.memory.MemoryRepository
import org.example.project.feature.memory.ConversationMemoryService
import org.example.project.feature.model.ChatProviderModelGateway
import org.example.project.feature.model.ModelGateway
import org.example.project.feature.prompt.PromptTemplateRepository
import org.example.project.feature.workspace.WorkspaceRepository
import org.example.project.feature.settings.SettingsRepository
import org.example.project.feature.user.CurrentUserProvider
import org.example.project.feature.user.UserRepository
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

    single { UserRepository(get()) }
    single<CurrentUserProvider> { get<UserRepository>() }

    single {
        println("ChatRepository")
        ChatRepository(get(), get())
    }

    single {
        println("ApiKeyRepository")
        ApiKeyRepository(get(), get())
    }

    single { WorkspaceRepository(get(), get()) }
    single { MemoryRepository(get(), get()) }
    single { PromptTemplateRepository(get(), get()) }
    single { FileAssetRepository(get(), get()) }
    single { SettingsRepository(get(), get()) }

    single<ChatProvider> {
        println("OpenAICompatibleProvider")
        OpenAICompatibleProvider(ProviderType.OpenAI, get())
    }

    single<ModelGateway> { ChatProviderModelGateway(get()) }
    single { ConversationMemoryService(get(), get()) }

    single {
        ContextEngine(
            setOf(
                SystemPromptProvider(),
                TemplatePromptProvider(get()),
                WorkspacePromptProvider(),
                MemoryPromptProvider(get()),
                FilePromptProvider(get()),
                HistoryPromptProvider()
            )
        )
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
