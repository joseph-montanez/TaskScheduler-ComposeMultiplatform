#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^DocumentPickerCompletionHandler)(NSString * _Nullable selectedFilePath);

@interface DocumentPickerDelegateHandler : NSObject <UIDocumentPickerDelegate>

- (instancetype)initWithCompletion:(DocumentPickerCompletionHandler)completion;

@end

NS_ASSUME_NONNULL_END
