#import "AppearanceObserver.h"
#import "AppearanceCallback.h"
#import <Cocoa/Cocoa.h>
#include <jni.h>

static jobject gCallbackObject = NULL;
static jmethodID gCallbackMethodID = NULL;
static JavaVM* gJvm = NULL;

@implementation AppearanceObserver

- (instancetype)init {
    self = [super init];
    if (self) {
        self.statusItem = [[NSStatusBar systemStatusBar] statusItemWithLength:NSVariableStatusItemLength];
        [self setupObserver];
    }
    return self;
}

- (void)setupObserver {
    self.statusItem.button.title = @"Initial Title";
    self.statusItem.visible = YES;

    [self.statusItem.button addObserver:self forKeyPath:@"effectiveAppearance" options:NSKeyValueObservingOptionNew context:NULL];
    [self.statusItem.button addObserver:self forKeyPath:@"frame" options:NSKeyValueObservingOptionNew context:NULL];
}

- (void)dealloc {
    [_statusItem.button removeObserver:self forKeyPath:@"effectiveAppearance"];
    [_statusItem.button removeObserver:self forKeyPath:@"frame"];
    [[NSStatusBar systemStatusBar] removeStatusItem:self.statusItem];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context {
    NSAppearance *appearance = self.statusItem.button.effectiveAppearance;
    NSString *appearanceName = appearance.name;

    if ([appearanceName.lowercaseString containsString:@"dark"]) {
        self.statusItem.button.title = @"Dark Mode";
        if (gAppearanceChangeCallback != NULL) {
            gAppearanceChangeCallback("dark");
        }
    } else {
        self.statusItem.button.title = @"Light Mode";
        if (gAppearanceChangeCallback != NULL) {
            gAppearanceChangeCallback("light");
        }
    }
}

@end

void appearanceChangeHandler(const char *mode) {
    JNIEnv *env = NULL;
    getJNIEnv(&env);

    if (env == NULL) return;

    if (gCallbackObject == NULL || gCallbackMethodID == NULL) return;

    jstring modeString = (*env)->NewStringUTF(env, mode);
    (*env)->CallVoidMethod(env, gCallbackObject, gCallbackMethodID, modeString);
    (*env)->DeleteLocalRef(env, modeString);

    //cleanupJNIEnv(); // Detach the thread if you're done with all JNI operations
}

void ensureCocoaInitialization() {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      [NSApplication sharedApplication];
      
      InitializeAppearanceObserver();
      RegisterAppearanceChangeCallback(appearanceChangeHandler);
    });
}

void getJNIEnv(JNIEnv **env) {
    if (gJvm == NULL) return; // JVM should have been stored during JNI_OnLoad

    // Try to get the JNIEnv for this thread. If the thread is not already attached to the JVM, attach it.
    if ((*gJvm)->GetEnv(gJvm, (void**) env, JNI_VERSION_1_6) != JNI_OK) {
        
        jint attachResult = (*gJvm)->AttachCurrentThread(gJvm, (void **)env, NULL);
        if (attachResult != JNI_OK) {
            // Handle error
            return;
        }
    }
}

void cleanupJNIEnv() {
    if (gJvm != NULL) {
        (*gJvm)->DetachCurrentThread(gJvm);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    gJvm = jvm;
    return JNI_VERSION_1_6; // specify the JNI version
}

JNIEXPORT void JNICALL Java_com_shabb_AppearanceManager_initializeAppearanceObserver(JNIEnv *env, jobject obj, jobject callback) {
    // Delete previous global ref if exists
    if (gCallbackObject != NULL) {
        (*env)->DeleteGlobalRef(env, gCallbackObject);
        gCallbackObject = NULL;
    }

    // Create new global ref for the callback object
    gCallbackObject = (*env)->NewGlobalRef(env, callback);
    if (gCallbackObject == NULL) return; // Out of memory error handling

    // Get the callback class
    jclass callbackClass = (*env)->GetObjectClass(env, gCallbackObject);

    // Get the Method ID for the 'appearanceChanged' method
    gCallbackMethodID = (*env)->GetMethodID(env, callbackClass, "appearanceChanged", "(Ljava/lang/String;)V");
    if (gCallbackMethodID == NULL) return; // Method not found

    // Ensure Cocoa initialization and registration is done on the main thread
    dispatch_async(dispatch_get_main_queue(), ^{
      ensureCocoaInitialization();
    });
}
