//
//  AppDelegate.h
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate,CBCentralManagerDelegate>
{
    CBCentralManager *btManager;
    NSMutableArray *askedUUIDsArray;
    BOOL processing;
}

@property (strong, nonatomic) UIWindow *window;

@end
