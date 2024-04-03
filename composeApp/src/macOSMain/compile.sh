#clang -fobjc-arc -dynamiclib -framework Cocoa -o libAppearanceObserver.dylib AppearanceObserver.m AppearanceCallback.c
clang -fobjc-arc -dynamiclib -o libAppearanceObserver.dylib \
    AppearanceObserver.m AppearanceCallback.m \
    -framework Cocoa \
    -I/Users/josephmontanez/.sdkman/candidates/java/21.0.2-open/include \
    -I/Users/josephmontanez/.sdkman/candidates/java/21.0.2-open/include/darwin


#clang -o MyApp main.c -L. -lAppearanceObserver -framework Cocoa
#clang -o MyApp main.c -L. -lAppearanceObserver -framework Cocoa -arch arm64
clang -o MyApp main.m -L. -lAppearanceObserver -framework Cocoa -arch arm64
