//
//  MapViewAnnotation.h
//  My Miles & More
//
//  Created by Tomasz Baranowicz on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>
#import "Store.h"

@interface MapViewAnnotation : NSObject <MKAnnotation> {
    
}

@property (nonatomic) int tag;
@property (nonatomic, copy) NSString *title;
@property (nonatomic) CLLocationCoordinate2D coordinate;

-(id)initWithTitle:(NSString *)title andCoordinate:(CLLocationCoordinate2D)c2d andTag:(int)tag;
@end
