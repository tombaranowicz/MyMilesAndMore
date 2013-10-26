//
//  MapViewAnnotation.m
//  My Miles & More
//
//  Created by Tomasz Baranowicz on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "MapViewAnnotation.h"

@implementation MapViewAnnotation

-(id)initWithTitle:(NSString *)title andCoordinate:(CLLocationCoordinate2D)c2d andTag:(int)tag {
    
    self = [super init];
    if (self) {
        self.title = title;
        self.tag = tag;
        self.coordinate = c2d;
    }
    return self;
}

@end