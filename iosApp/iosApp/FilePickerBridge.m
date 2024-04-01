#import "FilePickerBridge.h"
#import "DocumentPickerDelegateHandler.h"
#import <UIKit/UIKit.h>
#import <objc/runtime.h>

@implementation FilePickerBridge

+ (void)pickFileWithViewController:(void (^)(NSString * _Nullable))onResult {
    UIWindowScene *windowScene = [UIApplication.sharedApplication.connectedScenes.allObjects firstObject];
    UIViewController *rootViewController = windowScene.windows.firstObject.rootViewController;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.content"] inMode:UIDocumentPickerModeImport];
        DocumentPickerDelegateHandler *delegateHandler = [[DocumentPickerDelegateHandler alloc] initWithCompletion:onResult];
        // Retain the delegateHandler as long as needed
        objc_setAssociatedObject(documentPicker, @"delegateHandler", delegateHandler, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
        documentPicker.delegate = delegateHandler;
        [rootViewController presentViewController:documentPicker animated:YES completion:nil];
    });
}

+ (void)chooseFileSaveLocationWithCompletion:(void (^)(NSString * _Nullable))onResult {
    UIWindowScene *windowScene = [UIApplication.sharedApplication.connectedScenes.allObjects firstObject];
    UIViewController *rootViewController = windowScene.windows.firstObject.rootViewController;

    dispatch_async(dispatch_get_main_queue(), ^{
        UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.folder"] inMode:UIDocumentPickerModeOpen];
        documentPicker.delegate = (id<UIDocumentPickerDelegate>)rootViewController;
        documentPicker.allowsMultipleSelection = NO;
        documentPicker.modalPresentationStyle = UIModalPresentationFormSheet;
        [rootViewController presentViewController:documentPicker animated:YES completion:nil];
    });
}

+ (void)saveToFileWithContent:(NSString *)content filePath:(NSString *)filePath onComplete:(void (^)(BOOL))onComplete {
    NSURL *url = [NSURL URLWithString:filePath];
    NSError *error = nil;
    BOOL success = [content writeToURL:url atomically:YES encoding:NSUTF8StringEncoding error:&error];
    onComplete(success);
}

+ (void)readTextFromFileWithFilePath:(NSString *)filePath onResult:(void (^)(NSString * _Nullable))onResult {
    NSURL *url = [NSURL fileURLWithPath:filePath];
     NSError *error = nil;
     NSString *content = [NSString stringWithContentsOfURL:url encoding:NSUTF8StringEncoding error:&error];
     if (content) {
         onResult(content);
     } else {
         NSLog(@"Error reading file: %@", error.localizedDescription);
         onResult(nil);
     }
}

@end
