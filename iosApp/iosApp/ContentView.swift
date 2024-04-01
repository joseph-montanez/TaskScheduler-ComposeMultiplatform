import UIKit
import SwiftUI
import ComposeApp


struct ComposeView: UIViewControllerRepresentable {
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
}

struct ContentView: View {

    var body: some View {
        ComposeView()
        .ignoresSafeArea(.keyboard) // Compose has its own keyboard handler
        .onAppear {
        }
    }
}



