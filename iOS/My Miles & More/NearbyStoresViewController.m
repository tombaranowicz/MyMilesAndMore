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
    NSString *nextString;
    NSXMLParser *parser;
    NSString *currentElement;
    NSString *val;

    NSMutableArray *stores;
    NSMutableString *storeName;
    NSString *storeHours;
    NSString *storeAddress;
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

        // Print the response body in text
        NSString *response = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
        NSRange range = [response rangeOfString:@"<ul class=\"partner-stores\">"];
        NSString *substring = [[response substringFromIndex:range.location] stringByTrimmingCharactersInSet:[NSCharacterSet newlineCharacterSet]];
        
        range = [response rangeOfString:@"</ul>"];
        substring = [[substring substringToIndex:range.location-40] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        //nextString = [substring substringFromIndex:NSMaxRange(range)];
        
        //NSLog(@"NextString: %@", nextString);
        dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
//            NSURL *url = [NSURL URLWithString:urlString];
            parser = [[NSXMLParser alloc] initWithData:[substring dataUsingEncoding:NSUTF8StringEncoding]];//ContentsOfURL:url];
            [parser setDelegate:self];
            [parser setShouldResolveExternalEntities:NO];
            [parser parse];
        });
//        <div id="searchResults">
        
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
    [operation start];
    
//    NSLog(@"call string: %@", urlString);
}

#pragma mark NSXMLParserDelegate methods

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    
//    NSLog(@"did start: %@, params: %@", elementName, attributeDict);
    
    currentElement = [attributeDict objectForKey:@"class"];
    if ([currentElement isEqualToString:@"store-name"]) {
        if (store) {
            NSLog(@"%@|%@|%@", store.storeName, store.storeAddress, store.storeHours);
            [stores addObject:store];
        }
        store = [[Store alloc] init];
    }
    
    val=@"";
//    NSLog(@"current: %@", currentElement);
//    element = elementName;
//    
//    if ([element isEqualToString:@"item"]) {
//        feed = [[Feed alloc] init];
//        title = [[NSMutableString alloc] init];
//        link = [[NSMutableString alloc] init];
//        description = [[NSMutableString alloc] init];
//        pubDate = [[NSMutableString alloc] init];
//        content = [[NSMutableString alloc] init];
//    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    
//    NSLog(@"did end: %@", elementName);
//    if ([elementName isEqualToString:@"item"]) {
//        
//        //        NSLog(@"did end: %@", title);
//        feed.title = title;
//        feed.link = link;
//        feed.description = description;
//        
//        NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
//        [formatter setDateFormat:@"EEE, dd MM yyyy HH:mm:ss"];
//        
//        pubDate = [NSMutableString stringWithString:[pubDate stringByReplacingOccurrencesOfString:@"+0000" withString:@""]];
//        pubDate = [NSMutableString stringWithString:[pubDate stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]];
//        feed.date = [formatter dateFromString:pubDate];
//        
//        feed.content = content;
//        
//        NSError *error = NULL;
//        NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"(<img\\s[\\s\\S]*?src\\s*?=\\s*?['\"](.*?)['\"][\\s\\S]*?>)+?"
//                                                                               options:NSRegularExpressionCaseInsensitive
//                                                                                 error:&error];
//        
//        [regex enumerateMatchesInString:feed.content
//                                options:0
//                                  range:NSMakeRange(0, [feed.content length])
//                             usingBlock:^(NSTextCheckingResult *result, NSMatchingFlags flags, BOOL *stop) {
//                                 NSString *img = [feed.content substringWithRange:[result rangeAtIndex:2]];
//                                 feed.imagePath = img;
//                             }];
//        [tempArray addObject:feed];
//    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
//    NSLog(@"in %@ found characters %@", currentElement, string);
    val = [NSString stringWithFormat:@"%@%@",val,[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] ];
    val = [val stringByReplacingOccurrencesOfString:@"  " withString:@""];
    if ([currentElement isEqualToString:@"store-name"] && val.length>0) {
//        NSLog(@"name: |%@|", val);
        store.storeName = val;
    } else if([currentElement isEqualToString:@"store-address"] && val.length>0) {
//        NSLog(@"address: |%@|", val);
        store.storeAddress = val;
    } else if([currentElement isEqualToString:@"store-contact"] && val.length>0) {
//        NSLog(@"contact: |%@|", val);
    } else if([currentElement isEqualToString:@"store-openinghours"] && val.length>0) {
//        NSLog(@"hours: |%@|", val);
        store.storeHours = val;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser {
    
    if (store) {
        NSLog(@"%@|%@|%@", store.storeName, store.storeAddress, store.storeHours);
        [stores addObject:store];
    }
//    dispatch_async(dispatch_get_main_queue(), ^(void){
//        if (currentPage==1) {
//            feeds = [NSArray arrayWithArray:tempArray];
//        } else {
//            NSMutableArray *arr = [NSMutableArray arrayWithArray:feeds];
//            [arr addObjectsFromArray:tempArray];
//            feeds = [NSArray arrayWithArray:arr];
//        }
//        
//        if (tempArray.count<10) {
//            lastPageDownloaded=YES;
//        }
//        [refreshControl endRefreshing];
//        [self.tableView reloadData];
//        [progressView removeFromSuperview];
//        isRefreshing=NO;
//        self.tableView.userInteractionEnabled=YES;
//    });
}

@end
