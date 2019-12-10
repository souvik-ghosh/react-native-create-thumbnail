#import "CreateThumbnail.h"

@implementation CreateThumbnail

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(create:(NSDictionary *)config findEventsWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *url = (NSString *)[config objectForKey:@"url"] ?: @"";
    int timeStamp = [[config objectForKey:@"timeStamp"] intValue] ?: 1;
    NSString *type = (NSString *)[config objectForKey:@"type"] ?: @"remote";
    NSString *format = (NSString *)[config objectForKey:@"format"] ?: @"jpeg";
    
    @try {
        NSURL *vidURL = nil;
        if ([type isEqual: @"local"]) {
            url = [url stringByReplacingOccurrencesOfString:@"file://"
                                                  withString:@""];
            vidURL = [NSURL fileURLWithPath:url];
        } else {
            vidURL = [NSURL URLWithString:url];
        }

        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:vidURL options:nil];
        AVAssetImageGenerator *generator = [[AVAssetImageGenerator alloc] initWithAsset:asset];
        generator.appliesPreferredTrackTransform = YES;
        
        NSError *err = NULL;
        CMTime time = CMTimeMake(timeStamp, 1);
        
        CGImageRef imgRef = [generator copyCGImageAtTime:time actualTime:NULL error:&err];
        UIImage *thumbnail = [UIImage imageWithCGImage:imgRef];
        // save to temp directory
        NSString* tempDirectory = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
        
        NSData *data = nil;
        NSString *fullPath = nil;
        if ([format isEqual: @"png"]) {
            data = UIImagePNGRepresentation(thumbnail);
            fullPath = [tempDirectory stringByAppendingPathComponent: [NSString stringWithFormat:@"thumb-%@.png",[[NSProcessInfo processInfo] globallyUniqueString]]];
        } else {
            data = UIImageJPEGRepresentation(thumbnail, 1.0);
            fullPath = [tempDirectory stringByAppendingPathComponent: [NSString stringWithFormat:@"thumb-%@.jpeg",[[NSProcessInfo processInfo] globallyUniqueString]]];
        }

        NSFileManager *fileManager = [NSFileManager defaultManager];
        [fileManager createFileAtPath:fullPath contents:data attributes:nil];
        CGImageRelease(imgRef);
        resolve(@{
            @"path"     : fullPath,
            @"width"    : [NSNumber numberWithFloat: thumbnail.size.width],
            @"height"   : [NSNumber numberWithFloat: thumbnail.size.height]
        });
    } @catch(NSException *e) {
        reject(e.reason, nil, nil);
    }
}

@end
