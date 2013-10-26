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

@interface DescriptionViewController ()
@property (weak, nonatomic) IBOutlet UILabel *descriptionTitle;
@property (weak, nonatomic) IBOutlet UILabel *descriptionText;
@property (weak, nonatomic) IBOutlet MKMapView *map;
@property (weak, nonatomic) IBOutlet UIButton *button;

@property (nonatomic,assign) CGFloat lognitude;
@property (nonatomic,assign) CGFloat latitude;

@end

@implementation DescriptionViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    self.descriptionTitle.text = @"Snorlaxen Shopiren";
    
   NSString* text = @"Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
    
    
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
    
    self.lognitude = 13.406091;
    self.latitude = 52.519173;
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(self.latitude, self.lognitude);
    [self.map setCenterCoordinate:coordinate];
    [self.map setUserInteractionEnabled:NO];
    
    self.button.backgroundColor = [UIColor iOS7blueGradientEndColor];

    [self.button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.button setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected];
    
    
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
