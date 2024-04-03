#include "AppearanceObserver.h"
#include "AppearanceCallback.h"

static AppearanceObserver* observer = nil;
AppearanceChangeCallback gAppearanceChangeCallback = NULL;

// Implementation of the function
void RegisterAppearanceChangeCallback(AppearanceChangeCallback callback) {
    gAppearanceChangeCallback = callback;
}

void InitializeAppearanceObserver() {
    @autoreleasepool {
        if (!observer) {
            observer = [AppearanceObserver new];
            [observer setupObserver];
        }
    }
}