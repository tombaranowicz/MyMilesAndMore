//
//  AppDelegate.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "AppDelegate.h"
#import "ViewController.h"
#import "BeaconManager.h"
#import "ViewController.h"
#import "DescriptionViewController.h"
#import "AFHTTPClient.h"
#import "AFHTTPRequestOperation.h"
#import "BeaconObject.h"
#import "DescriptionViewController.h"
#import "UIViewController+dismiss.h"

#define app_uuid @"B929D963-23FA-8D33-7039-D000B9B8FA10"

@interface AppDelegate()<BeaconManagerDelegate>
@property (nonatomic) BeaconManager *beaconManager;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    self.beaconManager = [[BeaconManager alloc] initWithUUID:app_uuid];
    [self.beaconManager setDelegate:self];
    
    self.window.rootViewController = [ViewController new];
    
    return YES;
}
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}



// SERVER

- (void)requestWithUUID:(NSString*)uuid
{
    
    NSString* urlString = [@"http://198.245.54.12:8000/objects/get_tag_details/" stringByAppendingString:uuid];
    
    AFHTTPClient *httpClient = [[AFHTTPClient alloc] initWithBaseURL:[NSURL URLWithString:urlString]];
    
    NSMutableURLRequest *request = [httpClient requestWithMethod:@"GET"
                                                            path:urlString
                                                      parameters:nil];
    
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    
    [httpClient registerHTTPOperationClass:[AFHTTPRequestOperation class]];
    
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
        
        if([[operation response] statusCode] == 200)
        {
        
            NSString* response = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
            NSError *e = nil;
            NSDictionary* jsonDict = [NSJSONSerialization JSONObjectWithData:responseObject options:NSJSONReadingMutableContainers error:&e];
            NSLog(@"%@",response);
            
            BeaconObject* beaconObject = [[BeaconObject alloc] initWithDictionary:jsonDict[@"object"]];
        
            DescriptionViewController* descriptopnVC = [[DescriptionViewController alloc] initWithBeaconObject:beaconObject];
            
            
            UINavigationController* navVC = [[UINavigationController alloc] initWithRootViewController:descriptopnVC];
            
            [descriptopnVC insertCloseButton];
            
            [self.window.rootViewController presentViewController:navVC animated:YES completion:nil];
            
            
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
    
    [operation start];
    
}

#pragma mark - BeaconDelegate

- (void)foundDeviceWithUUID:(NSString *)uuid
{
    [self requestWithUUID:uuid];
}

@end


