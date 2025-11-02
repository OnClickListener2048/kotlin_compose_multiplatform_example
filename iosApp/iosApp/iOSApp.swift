import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
   init() {
       // 这个调用仍然有效，因为它会使用默认的空 lambda

   }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
