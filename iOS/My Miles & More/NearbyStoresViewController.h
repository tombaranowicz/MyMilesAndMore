//
//  NearbyStoresViewController.h
//  My Miles & More
//
//  Created by Tomasz Baranowicz on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NearbyStoresViewController : UIViewController
{
    float lat;
    float lon;
    NSString *country;
    NSString *city;
}

-(id)initWithLatitude:(float)latitude withLongitude:(float)longitude withCountryCode:(NSString *)country withCity:(NSString *)city;

@end
