#ifndef APPEARANCE_CALLBACK_H
#define APPEARANCE_CALLBACK_H

#include <stddef.h>

void InitializeAppearanceObserver();

typedef void (*AppearanceChangeCallback)(const char *mode);

// Declare the global variable here
extern AppearanceChangeCallback gAppearanceChangeCallback;

void RegisterAppearanceChangeCallback(AppearanceChangeCallback callback);

#endif // APPEARANCE_CALLBACK_H
