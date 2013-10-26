//
//  BeaconObject.h
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BeaconObject : NSObject
@property(nonatomic,strong) NSString* tagID;
@property(nonatomic,strong) NSString* name;
@property(nonatomic,strong) NSString* descriptionText;
@property(nonatomic,strong) NSString* link;
@property(nonatomic,assign) CGFloat longitude;
@property(nonatomic,assign) CGFloat latitude;
@property(nonatomic,strong) NSString* uuid;

-(instancetype)initWithDictionary:(NSDictionary*)dict;
@end
