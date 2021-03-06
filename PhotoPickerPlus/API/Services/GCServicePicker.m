//
//  GCServiceAccount.m
//  Chute-SDK
//
//  Created by ARANEA on 7/30/13.
//  Copyright (c) 2013 Aleksandar Trpeski. All rights reserved.
//

#import "GCServicePicker.h"
#import "GCAccount.h"
#import "GCAccountAlbum.h"
#import "GCAccountAssets.h"
#import "PhotoPickerClient.h"
#import "GCConfiguration.h"
#import "NSDictionary+GCAccountAsset.h"

#import "GCOAuth2Client.h"
#import "NSString+QueryString.h"
#import "GCResponse.h"
#import "GCClient.h"
#import "GCResponseStatus.h"
#import "GetChute.h"
#import "AFJSONRequestOperation.h"

static NSString * const kGCAuth = @"Authorization";
static NSString * const kClientGET = @"GET";

@implementation GCServicePicker

+ (void)getProfileInfoWithSuccess:(void (^)(GCResponseStatus *, NSArray *))success failure:(void (^)(NSError *))failure
{
    GCClient *apiClient = [GCClient sharedClient];
    
    NSString *path = [NSString stringWithFormat:@"me/accounts"];
    
    NSMutableURLRequest *request = [apiClient requestWithMethod:kGCClientGET path:path parameters:nil];

    [apiClient request:request factoryClass:[GCAccount class] success:^(GCResponse *response) {
        success(response.response, response.data);
    } failure:failure];
}

+ (void)getDataForServiceWithName:(NSString *)serviceName forAccountWithID:(NSString *)accountID forAlbumWithID:(NSNumber *)albumID success:(void (^)(GCResponseStatus *, NSArray *, NSArray *))success failure:(void (^)(NSError *))failure
{
    PhotoPickerClient *apiClient = [PhotoPickerClient sharedClient];
    NSString *path;
    if(albumID == nil)
        path = [NSString stringWithFormat:@"%@/%@/files",serviceName,accountID];
    else
    {
        NSString *albumIDString = [[NSString stringWithFormat:@"%@",albumID] stringByEscapingForURL];
        path = [NSString stringWithFormat:@"%@/%@/folders/%@/files",serviceName,accountID,albumIDString];
    }
    
    NSMutableURLRequest *request = [apiClient requestWithMethod:kClientGET path:path parameters:nil];
    
    [apiClient request:request success:^(GCResponseStatus *responseStatus, NSArray *folders, NSArray *files) {
        success(responseStatus,folders,files);
    } failure:failure];
}

+ (void)postSelectedImages:(NSArray *)selectedImages success:(void(^)(GCResponseStatus *, NSArray *))success failure:(void(^)(NSError *))failure
{
    GCClient *apiClient = [GCClient sharedClient];
        
    NSString *clientID = [[GCConfiguration configuration] appId];
    
    
    NSMutableArray *media = [[NSMutableArray alloc] initWithCapacity:[selectedImages count]];
    for(GCAccountAssets *asset in selectedImages)
    {
        NSDictionary *dictFromAsset = [NSDictionary dictionaryFromGCAccountAssets:asset];
        [media addObject:dictFromAsset];
    }
    
    NSDictionary *param = @{@"options":@{kGCClientID:clientID},
                            @"media":media};

    NSString *path = @"widgets/native";
    
    NSMutableURLRequest *request = [apiClient requestWithMethod:kGCClientPOST path:path parameters:param];
    
    AFJSONRequestOperation *operation = [AFJSONRequestOperation JSONRequestOperationWithRequest:request success:^(NSURLRequest *request, NSHTTPURLResponse *response, id JSON) {
        NSDictionary *data = @{
                               @"data":[[JSON objectForKey:@"data"] objectForKey:@"media"],
                               @"response":[JSON objectForKey:@"response"]
                               };
        [apiClient parseJSON:data withFactoryClass:[GCAsset class] success:^(GCResponse *gcResponse) {
            success(gcResponse.response, gcResponse.data);
        }];
    } failure:^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON) {
        failure(error);
    }];
    
    [apiClient enqueueHTTPRequestOperation:operation];
}

@end