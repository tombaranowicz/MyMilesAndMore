//
//  ViewController.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "ViewController.h"
#import "NearbyStoresViewController.h"
const CGFloat duration = 10.0;

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    NearbyStoresViewController *vc = [[NearbyStoresViewController alloc] initWithLatitude:52.519171 withLongitude:13.4060912 withCountryCode:@"DE" withCity:@"Berlin"];
    [self.navigationController pushViewController:vc animated:YES];
    
    int max = 5;
    for(int i=1;i<=max;i++)
    {
            [self.view addSubview:[self viewWithDelay:i*duration/(CGFloat)max]];
    }

}

- (UIView*)viewWithDelay:(CGFloat)delay
{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(200, 200, 100, 100)];
    view.backgroundColor = [UIColor clearColor];
    view.layer.cornerRadius = 50;
    view.layer.borderColor = [UIColor orangeColor].CGColor;
    view.layer.borderWidth = 1;

    view.center = CGPointMake(CGRectGetWidth(self.view.frame)/2.0, CGRectGetHeight(self.view.frame)/2.0);
    view.hidden = YES;
    [self performSelector:@selector(animateView:) withObject:view afterDelay:delay];
    return view;
}

- (void)animateView:(UIView*)view
{
    view.hidden = NO;
    CABasicAnimation *scaleAnimation = [CABasicAnimation animationWithKeyPath:@"transform.scale"];
    scaleAnimation.duration = duration;
    scaleAnimation.repeatCount = HUGE_VAL;
    scaleAnimation.autoreverses = NO;
    scaleAnimation.fromValue = [NSNumber numberWithFloat:0.1];
    scaleAnimation.toValue = [NSNumber numberWithFloat:6.5];
    
    [view.layer addAnimation:scaleAnimation forKey:@"scale"];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
