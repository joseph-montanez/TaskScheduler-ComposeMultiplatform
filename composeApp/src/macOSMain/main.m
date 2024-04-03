#import <Cocoa/Cocoa.h>
#import "AppearanceCallback.h"

void appearanceChangeHandler(const char *mode) {
    printf("Appearance changed to: %s\n", mode);
}

int main() {
    @autoreleasepool {
        // Initialize the shared application instance
        [NSApplication sharedApplication];

        InitializeAppearanceObserver();
        RegisterAppearanceChangeCallback(appearanceChangeHandler);

        // Start the application event loop
        [[NSApplication sharedApplication] run];
    }
    return 0;
}
