//
//  PhotoPickerClient.h
//  PhotoPickerPlus-SampleApp
//
//  Created by ARANEA on 8/7/13.
//  Copyright (c) 2013 Chute. All rights reserved.
//

#import "AFHTTPClient.h"
#import <Chute-SDK/GCResponse.h>

@interface PhotoPickerClient : AFHTTPClient

+ (PhotoPickerClient *)sharedClient;

- (void)request:(NSMutableURLRequest *)request factoryClass:(Class)factoryClass success:(void (^)(GCResponse *response))success failure:(void (^)(NSError *error))failure;
- (void)parseJSON:(id)JSON withFactoryClass:(Class)factoryClass success:(void (^)(GCResponse *))success;

@end
