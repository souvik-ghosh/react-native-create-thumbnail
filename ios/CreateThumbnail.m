#import "CreateThumbnail.h"

@implementation CreateThumbnail

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(create:(NSDictionary *)config findEventsWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *url = (NSString *)[config objectForKey:@"url"] ?: @"";
    int timeStamp = [[config objectForKey:@"timeStamp"] intValue] ?: 0;
    NSString *format = (NSString *)[config objectForKey:@"format"] ?: @"jpeg";
    int dirSize = [[config objectForKey:@"dirSize"] intValue] ?: 100;
    NSString *cacheName = (NSString *)[config objectForKey:@"cacheName"];
    NSDictionary *headers = config[@"headers"] ?: @{};

    unsigned long long cacheDirSize = dirSize * 1024 * 1024;

    @try {
        // Prepare cache folder
        NSString* tempDirectory = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
        tempDirectory = [tempDirectory stringByAppendingString:@"/thumbnails/"];
        // Create thumbnail directory if not exists
        [[NSFileManager defaultManager] createDirectoryAtPath:tempDirectory withIntermediateDirectories:YES attributes:nil error:nil];
        NSString *fileName = [NSString stringWithFormat:@"thumb-%@.%@", cacheName ?: [[NSProcessInfo processInfo] globallyUniqueString], format];
        NSString* fullPath = [tempDirectory stringByAppendingPathComponent:fileName];
        if ([[NSFileManager defaultManager] fileExistsAtPath:fullPath]) {
            NSData *imageData = [NSData dataWithContentsOfURL:[NSURL fileURLWithPath:fullPath]];
            UIImage *thumbnail = [UIImage imageWithData:imageData];
            resolve(@{
                @"path"     : fullPath,
                @"size"     : [NSNumber numberWithFloat: imageData.length],
                @"mime"     : [NSString stringWithFormat: @"image/%@", format],
                @"width"    : [NSNumber numberWithFloat: thumbnail.size.width],
                @"height"   : [NSNumber numberWithFloat: thumbnail.size.height]
            });
            return;
        }
        
        NSURL *vidURL = nil;
        NSString *url_ = [url lowercaseString];

        if ([url_ hasPrefix:@"http://"] || [url_ hasPrefix:@"https://"] || [url_ hasPrefix:@"file://"]) {
            vidURL = [NSURL URLWithString:url];
        } else {
            // Consider it's file url path 
            vidURL = [NSURL fileURLWithPath:url];
        }

        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:vidURL options:@{@"AVURLAssetHTTPHeaderFieldsKey": headers}];
        [self generateThumbImage:asset atTime:timeStamp completion:^(UIImage *thumbnail) {
            // Clean directory
            unsigned long long size = [self sizeOfFolderAtPath:tempDirectory];
            if (size >= cacheDirSize) {
                [self cleanDir:tempDirectory forSpace:cacheDirSize / 2];
            }
            
            // Generate thumbnail
            NSData *data = nil;
            if ([format isEqual: @"png"]) {
                data = UIImagePNGRepresentation(thumbnail);
            } else {
                data = UIImageJPEGRepresentation(thumbnail, 1.0);
            }

            NSFileManager *fileManager = [NSFileManager defaultManager];
            [fileManager createFileAtPath:fullPath contents:data attributes:nil];
            resolve(@{
                @"path"     : fullPath,
                @"size"     : [NSNumber numberWithFloat: data.length],
                @"mime"     : [NSString stringWithFormat: @"image/%@", format],
                @"width"    : [NSNumber numberWithFloat: thumbnail.size.width],
                @"height"   : [NSNumber numberWithFloat: thumbnail.size.height]
            });
        } failure:^(NSError *error) {
            reject(error.domain, error.description, nil);
        }];

        
        
    } @catch(NSException *e) {
        reject(e.name, e.reason, nil);
    }
}

- (unsigned long long) sizeOfFolderAtPath:(NSString *)path {
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:path error:nil];
    NSEnumerator *enumerator = [files objectEnumerator];
    NSString *fileName;
    unsigned long long size = 0;
    while (fileName = [enumerator nextObject]) {
        size += [[[NSFileManager defaultManager] attributesOfItemAtPath:[path stringByAppendingPathComponent:fileName] error:nil] fileSize];
    }
    return size;
}

- (void) cleanDir:(NSString *)path forSpace:(unsigned long long)size {
    NSFileManager *fm = [NSFileManager defaultManager];
    NSError *error = nil;
    unsigned long long deletedSize = 0;
    for (NSString *file in [fm contentsOfDirectoryAtPath:path error:&error]) {
        unsigned long long fileSize = [[[NSFileManager defaultManager] attributesOfItemAtPath:[path stringByAppendingPathComponent:file] error:nil] fileSize];
        BOOL success = [fm removeItemAtPath:[NSString stringWithFormat:@"%@%@", path, file] error:&error];
        if (success) {
            deletedSize += fileSize;
        }
        if (deletedSize >= size) {
            break;
        }
    }
    return;
}

- (void) generateThumbImage:(AVURLAsset *)asset atTime:(int)timeStamp completion:(void (^)(UIImage* thumbnail))completion failure:(void (^)(NSError* error))failure {
    AVAssetImageGenerator *generator = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    generator.appliesPreferredTrackTransform = YES;
    generator.maximumSize = CGSizeMake(512, 512);
    CMTime time = CMTimeMake(timeStamp, 1000);
    AVAssetImageGeneratorCompletionHandler handler = ^(CMTime timeRequested, CGImageRef image, CMTime timeActual, AVAssetImageGeneratorResult result, NSError *error) {
        if (result == AVAssetImageGeneratorSucceeded) {
            UIImage *thumbnail = [UIImage imageWithCGImage:image];
            completion(thumbnail);
        } else {
            failure(error);
        }
    };
    [generator generateCGImagesAsynchronouslyForTimes:[NSArray arrayWithObject:[NSValue valueWithCMTime:time]] completionHandler:handler];
}

@end
