//
//  AppDelegate.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "AppDelegate.h"
@interface AppDelegate()
@property (nonatomic) CBCentralManager *btManager;
@property (nonatomic) NSMutableArray *askedUUIDsArray;
@property (nonatomic,assign) BOOL processing;
@property (nonatomic) CLLocationManager* locationManager;
@property (nonatomic) NSUUID* myUUID;
@property (nonatomic) NSString* myID;
@property (nonatomic) CBPeripheralManager* peripheralManager;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Override point for customization after application launch.
    self.btManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    self.askedUUIDsArray = [NSMutableArray arrayWithObject:@"B929D963-23FA-8D33-7039-D000B9B8FA10"];
    
    self.myUUID = [[NSUUID alloc] initWithUUIDString:@"B929D963-23FA-8D33-7039-D000B9B8FA10"];
    
    self.peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];

    
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    self.locationManager.activityType = CLActivityTypeFitness;
    
    [self startMonitoringForStores];
    
    return YES;
}

- (void)startMonitoringForStores
{
    CLBeaconRegion *region = [[CLBeaconRegion alloc] initWithProximityUUID:self.myUUID identifier:self.myID];
    region.notifyEntryStateOnDisplay = YES;
    region.notifyOnEntry = YES;
    region.notifyOnExit = YES;
    [self.locationManager startMonitoringForRegion:region];
}

- (void)locationManager:(CLLocationManager *)manager
         didEnterRegion:(CLRegion *)region
{
    NSLog(@"ENTERED");

}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"FAILED");
}

- (void)locationManager:(CLLocationManager *)manager
          didExitRegion:(CLRegion *)region
{
    // clear notification
    NSLog(@"EXIT");
}

- (void)locationManager:(CLLocationManager *)manager
      didDetermineState:(CLRegionState)state
              forRegion:(CLRegion *)region
{
    NSLog(@"DETERMINE");
}

- (void)locationManager:(CLLocationManager *)manager
        didRangeBeacons:(NSArray *)beacons
               inRegion:(CLBeaconRegion *)region
{
    NSLog(@"didRange");
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

#pragma mark CBPeripheralManagerDelegate methods
- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"Central Manager did change state %@", central.description);
    switch (central.state) {
        case CBCentralManagerStatePoweredOn:
            [self.btManager scanForPeripheralsWithServices:nil options:@{CBCentralManagerScanOptionAllowDuplicatesKey : @YES }];
            break;
        default:
            break;
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    
    CFUUIDRef theUUID = CFUUIDCreate(NULL);
    CFStringRef string = CFUUIDCreateString(NULL, peripheral.UUID);
    CFRelease(theUUID);
    NSString *UUID = (__bridge_transfer NSString *)string;
    
    if([self.askedUUIDsArray containsObject:UUID] && !self.processing){
        _processing=YES;
        //[[Server sharedInstance] getSenderByBluetooth:UUID];
        NSLog(@"FOUND !!!!! UUID %@", UUID);
    }
}

- (void)retrievePeripherals:(NSArray *)peripheralUUIDs
{
    NSLog(@"Retrieve");
}


@end
