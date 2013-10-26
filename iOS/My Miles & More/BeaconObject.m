//
//  BeaconObject.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "BeaconObject.h"

@implementation BeaconObject
-(instancetype)initWithDictionary:(NSDictionary*)dict
{
    self = [super init];
    
    if(self)
    {
        [self setupWithDictionary:dict];
    }
    
    return self;
}

-(void)setupWithDictionary:(NSDictionary*)dict
{
    self.tagID               = dict[@"tag_id"];
    self.name                = dict[@"title"];
    self.descriptionText     = dict[@"description"];
    self.link                = dict[@"link"];
    self.longitude           = [dict[@"longitude"] floatValue];
    self.latitude            = [dict[@"latitude"] floatValue];
    self.uuid                = dict[@"uuid"];
}

@end
