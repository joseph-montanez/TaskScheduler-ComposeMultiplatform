#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>
#include <jni.h>

@interface AppearanceObserver : NSObject
@property (strong) NSStatusItem *statusItem; // Declare statusItem as a property
- (void)setupObserver;
@end

// Forward declarations
void getJNIEnv(JNIEnv **env);
void cleanupJNIEnv();