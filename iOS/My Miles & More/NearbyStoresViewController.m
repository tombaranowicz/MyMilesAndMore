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
#import <MapKit/MapKit.h>
#import "MapViewAnnotation.h"

#define METERS_PER_MILE 1609.344

@interface NearbyStoresViewController () <NSXMLParserDelegate, MKMapViewDelegate>
{
    NSString *response;
    NSXMLParser *parser;
    NSString *currentElement;
    NSString *val;

    NSMutableArray *stores;
    float storeLatitude;
    float storeLongitude;
    
    
    UILabel *hoursLabel;
    UITextView *hoursTextView;
}

@property(nonatomic,strong) Store *store;

@property (nonatomic, strong) MKMapView *mapView;

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
    
    self.title = @"Nearby shops";
    
    self.mapView = [[MKMapView alloc] initWithFrame:CGRectMake(10, 100, 300, 300)];
    self.mapView.delegate=self;
    [self.view addSubview:self.mapView];
    
    hoursLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 420, 300, 20)];
    hoursLabel.text = @"Opening hours:";
    [self.view addSubview:hoursLabel];
    
    hoursTextView = [[UITextView alloc] initWithFrame:CGRectMake(7, 430, 300, 100)];
    hoursTextView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:hoursTextView];
    
    CLLocationCoordinate2D zoomLocation;
    zoomLocation.latitude = lat;
    zoomLocation.longitude= lon;
    MKCoordinateRegion viewRegion = MKCoordinateRegionMakeWithDistance(zoomLocation, 1.5*METERS_PER_MILE, 1.5*METERS_PER_MILE);
    MKCoordinateRegion adjustedRegion = [_mapView regionThatFits:viewRegion];
    [_mapView setRegion:adjustedRegion animated:YES];
    
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
                int tag = 0;
                for (Store *st in stores) {
                    NSLog(@"%@|%@|%@|%f|%f", st.storeName, st.storeAddress, st.storeHours, st.longitude, st.latitude);
                    
                    CLLocationCoordinate2D location;
                    NSLog(@"%f %f", st.latitude, st.longitude);
                    location.latitude = st.latitude;
                    location.longitude = st.longitude;
                    MapViewAnnotation *newAnnotation = [[MapViewAnnotation alloc] initWithTitle:st.storeName andCoordinate:location andTag:tag++];
                    [self.mapView addAnnotation:newAnnotation];
                }
                 [self selectStore:[stores firstObject]];
                [self.mapView selectAnnotation:[[self.mapView annotations] firstObject] animated:YES];
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
        if (self.store) {

            BOOL exist = NO;
            for (Store *tempStore in stores) {
                if ([tempStore.storeAddress isEqualToString:self.store.storeAddress]) {
                    exist=YES;
                }
            }
            
            if (!exist) {
                self.store.longitude = storeLongitude;
                self.store.latitude = storeLatitude;
                [stores addObject:self.store];
            }
        }
        self.store = [[Store alloc] init];
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
        self.store.storeName = val;
    } else if([currentElement isEqualToString:@"store-address"] && val.length>0) {
        self.store.storeAddress = val;
    } else if([currentElement isEqualToString:@"store-openinghours"] && val.length>0) {
        self.store.storeHours = val;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser {
    
    if (self.store) {
        BOOL exist = NO;
        for (Store *tempStore in stores) {
            if ([tempStore.storeAddress isEqualToString:self.store.storeAddress]) {
                exist=YES;
            }
        }
        
        if (!exist) {
            self.store.longitude = storeLongitude;
            self.store.latitude = storeLatitude;
            [stores addObject:self.store];
        }
    }
}

#pragma mark MKMapViewDelegate

- (void)mapView:(MKMapView *)mapView didSelectAnnotationView:(MKAnnotationView *)view
{
    MapViewAnnotation *annotation = (MapViewAnnotation *)[view annotation];
    Store *store = [stores objectAtIndex:annotation.tag];
    [self selectStore:store];
}

- (void)selectStore:(Store*)store
{
    NSLog(@"hours: %@",store.storeHours);
    NSArray *hoursArray = [store.storeHours componentsSeparatedByString:@","];
    
    NSString *hoursString = @"";
    for (NSString *string in hoursArray) {
        hoursString = [NSString stringWithFormat:@"%@\n%@",hoursString, [string stringByReplacingOccurrencesOfString:@" " withString:@""]];
    }
    hoursTextView.text = hoursString;
}

@end
