#ifndef FilePickerBridge_h
#define FilePickerBridge_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface FilePickerBridge : NSObject

+ (void)pickFileWithViewController:(void (^)(NSString * _Nullable))onResult;

+ (void)chooseFileSaveLocationWithCompletion:(void (^)(NSString * _Nullable))onResult;

+ (void)saveToFileWithContent:(NSString *)content filePath:(NSString *)filePath onComplete:(void (^)(BOOL))onComplete;

+ (void)readTextFromFileWithFilePath:(NSString *)filePath onResult:(void (^)(NSString * _Nullable))onResult;

@end

NS_ASSUME_NONNULL_END

#endif /* FilePickerBridge_h */
