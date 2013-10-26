//
//  DescriptionViewController.h
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import <UIKit/UIKit.h>
@class BeaconObject;

@interface DescriptionViewController : UIViewController
- (id)initWithBeaconObject:(BeaconObject*)beaconObject;
@end
