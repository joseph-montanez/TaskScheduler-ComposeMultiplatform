#import "DocumentPickerDelegateHandler.h"

@interface DocumentPickerDelegateHandler ()

@property (nonatomic, copy) DocumentPickerCompletionHandler completion;

@end

@implementation DocumentPickerDelegateHandler

- (instancetype)initWithCompletion:(DocumentPickerCompletionHandler)completion {
    self = [super init];
    if (self) {
        _completion = [completion copy];
    }
    return self;
}

#pragma mark - UIDocumentPickerDelegate

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    NSString *selectedFilePath = urls.firstObject.path;
    if (self.completion) {
        self.completion(selectedFilePath);
    }
    // Ensure to dismiss the delegate handler to avoid retain cycles
    controller.delegate = nil;
}

- (void)documentPickerWasCancelled:(UIDocumentPickerViewController *)controller {
    if (self.completion) {
        self.completion(nil);
    }
    // Ensure to dismiss the delegate handler to avoid retain cycles
    controller.delegate = nil;
}

@end
