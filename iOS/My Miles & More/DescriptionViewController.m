//
//  DescriptionViewController.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "DescriptionViewController.h"
#import <MapKit/MapKit.h>
#import "UIColor+iOS7Colors.h"
#import "BeaconObject.h"
#import "NearbyStoresViewController.h"
#import "MapViewAnnotation.h"
#define METERS_PER_MILE 1609.344
@interface DescriptionViewController ()
@property (nonatomic,strong) BeaconObject* beaconObject;

@property (weak, nonatomic) IBOutlet UILabel *descriptionTitle;
@property (weak, nonatomic) IBOutlet UILabel *descriptionText;
@property (weak, nonatomic) IBOutlet MKMapView *map;
@property (weak, nonatomic) IBOutlet UIButton *button;

@property (nonatomic,assign) CGFloat lognitude;
@property (nonatomic,assign) CGFloat latitude;

@end

@implementation DescriptionViewController

- (id)initWithBeaconObject:(BeaconObject*)beaconObject
{
    self = [super init];
    if (self) {
        // Custom initialization
        self.beaconObject = beaconObject;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    self.descriptionTitle.text = self.beaconObject.name;
    
    NSString* text = self.beaconObject.descriptionText;
    
    
    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.lineBreakMode = NSLineBreakByWordWrapping;
    paragraphStyle.alignment = NSTextAlignmentJustified;
    
    NSAttributedString *string = [[NSAttributedString alloc] initWithString:text
                                                                 attributes:
                                                                          @{
                                                                            NSParagraphStyleAttributeName:paragraphStyle,
                                                                            NSBaselineOffsetAttributeName:@0
                                                                            }];
    
    self.descriptionText.attributedText = string;
    
    self.lognitude = self.beaconObject.longitude;
    self.latitude = self.beaconObject.latitude;
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(self.latitude, self.lognitude);
    [self.map setCenterCoordinate:coordinate];
    [self.map setUserInteractionEnabled:NO];
    
    MKCoordinateRegion viewRegion = MKCoordinateRegionMakeWithDistance(coordinate, 0.6*METERS_PER_MILE, 0.6*METERS_PER_MILE);
    MKCoordinateRegion adjustedRegion = [self.map regionThatFits:viewRegion];
    [self.map setRegion:adjustedRegion animated:YES];
    
    MapViewAnnotation *newAnnotation = [[MapViewAnnotation alloc] initWithTitle:self.descriptionTitle.text andCoordinate:coordinate andTag:0];
    [self.map addAnnotation:newAnnotation];
    
    
    
    
    self.button.backgroundColor = [UIColor iOS7blueGradientEndColor];

    [self.button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.button setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected];
    
}

- (IBAction)buttonTapped:(id)sender
{
    [self.navigationController pushViewController:[[NearbyStoresViewController alloc] initWithLatitude:self.latitude withLongitude:self.lognitude withCountryCode:self.beaconObject.countryCode withCity:self.beaconObject.city] animated:YES];
}


@end
