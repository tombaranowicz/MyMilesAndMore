//
//  NearbyStoresViewController.m
//  My Miles & More
//
//  Created by Tomasz Baranowicz on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "NearbyStoresViewController.h"
#import "AFHTTPClient.h"
#import "AFHTTPRequestOperation.h"
#import "Store.h"

@interface NearbyStoresViewController () <NSXMLParserDelegate>
{
    NSString *response;
    NSXMLParser *parser;
    NSString *currentElement;
    NSString *val;

    NSMutableArray *stores;
    float storeLatitude;
    float storeLongitude;
    Store *store;
}

@end

@implementation NearbyStoresViewController

-(id)initWithLatitude:(float)latitude withLongitude:(float)longitude withCountryCode:(NSString *)country_ withCity:(NSString *)city_
{
    self = [super init];
    if (self) {
        lat = latitude;
        lon = longitude;
        country = country_;
        city = city_;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    
    NSString *urlString = [NSString stringWithFormat:@"http://www.miles-and-more-shopfinder.de/stores/search/lang:eng?lat=%f&lng=%f&radius=1&country_code=%@&address=%@", lat,lon, country, city];
    
    stores = [[NSMutableArray alloc] init];
    
    AFHTTPClient *httpClient = [[AFHTTPClient alloc] initWithBaseURL:[NSURL URLWithString:urlString]];
    NSMutableURLRequest *request = [httpClient requestWithMethod:@"GET"
                                                            path:urlString
                                                      parameters:nil];
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    [httpClient registerHTTPOperationClass:[AFHTTPRequestOperation class]];
    [operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {

        dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
            response = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
            NSRange rangeStart = [response rangeOfString:@"<ul class=\"partner-stores\">"];
            
            while (rangeStart.location != NSNotFound) {
                
                NSString *substring = [[response substringFromIndex:rangeStart.location] stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]];
                
                NSRange rangeEnd = [substring rangeOfString:@"</ul>"];
                response = [substring substringFromIndex:NSMaxRange(rangeEnd)];
                substring = [[substring substringToIndex:rangeEnd.location+5] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
                
                parser = [[NSXMLParser alloc] initWithData:[substring dataUsingEncoding:NSUTF8StringEncoding]];//ContentsOfURL:url];
                [parser setDelegate:self];
                [parser setShouldResolveExternalEntities:NO];
                [parser parse];
                
                rangeStart = [response rangeOfString:@"<ul class=\"partner-stores\">"];
            }
            
            dispatch_async(dispatch_get_main_queue(), ^(void){
                NSLog(@"did end");
                for (Store *store in stores) {
                    NSLog(@"%@|%@|%@|%f|%f", store.storeName, store.storeAddress, store.storeHours, store.longitude, store.latitude);
                }
            });
        });
        
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
    [operation start];
}

#pragma mark NSXMLParserDelegate methods

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    
//    NSLog(@"did start: %@, params: %@", elementName, attributeDict);
    
    currentElement = [attributeDict objectForKey:@"class"];
    if ([currentElement isEqualToString:@"store-name"]) {
        if (store) {

            BOOL exist = NO;
            for (Store *tempStore in stores) {
                if ([tempStore.storeAddress isEqualToString:store.storeAddress]) {
                    exist=YES;
                }
            }
            
            if (!exist) {
                store.longitude = storeLongitude;
                store.latitude = storeLongitude;
                [stores addObject:store];
            }
        }
        store = [[Store alloc] init];
    } else if([currentElement isEqualToString:@"store "]) {
        storeLatitude = [[attributeDict objectForKey:@"data-latitude"] floatValue];
        storeLongitude = [[attributeDict objectForKey:@"data-longitude"] floatValue];
    }
    
    val=@"";
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    val = [NSString stringWithFormat:@"%@%@",val,[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] ];
    val = [val stringByReplacingOccurrencesOfString:@"  " withString:@""];
    if ([currentElement isEqualToString:@"store-name"] && val.length>0) {
        store.storeName = val;
    } else if([currentElement isEqualToString:@"store-address"] && val.length>0) {
        store.storeAddress = val;
    } else if([currentElement isEqualToString:@"store-openinghours"] && val.length>0) {
        store.storeHours = val;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser {
    
    if (store) {
        BOOL exist = NO;
        for (Store *tempStore in stores) {
            if ([tempStore.storeAddress isEqualToString:store.storeAddress]) {
                exist=YES;
            }
        }
        
        if (!exist) {
            store.longitude = storeLongitude;
            store.latitude = storeLongitude;
            [stores addObject:store];
        }
    }
}

@end
