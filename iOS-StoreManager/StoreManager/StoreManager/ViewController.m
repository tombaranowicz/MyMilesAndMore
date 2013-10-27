//
//  ViewController.m
//  StoreManager
//
//  Created by Mateusz Glapiak on 10/27/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "ViewController.h"
#import "AFNetworking.h"
#import <QuartzCore/QuartzCore.h>
#import "UIColor+iOS7Colors.h"

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UITextField *titleTextField;
@property (weak, nonatomic) IBOutlet UITextView *descriptionTextView;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    self.descriptionTextView.layer.borderColor = [UIColor iOS7blueGradientEndColor].CGColor;
    self.descriptionTextView.layer.borderWidth = 0.5;
    self.descriptionTextView.layer.cornerRadius = 5.0;
    
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Send" style:UIBarButtonItemStyleDone target:self action:@selector(request)];
}


- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
}
- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)request
{
    NSString* urlString = @"http://198.245.54.12:8000/";
    
    AFHTTPClient *httpClient = [[AFHTTPClient alloc] initWithBaseURL:[NSURL URLWithString:urlString]];
    
    [httpClient postPath:@"/objects/send_push" parameters:@{
                                               @"tag_id":@"00:17:EA:93:AC:58",
                                               @"title":self.titleTextField.text,
                                               @"description":self.descriptionTextView.text
                                               } success:^(AFHTTPRequestOperation *operation, id responseObject) {
                                                   
                                                   if([[operation response] statusCode] == 200)
                                                   {
                                                       
                                                   }
                                               } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
                                                   NSLog(@"Error: %@", error);
                                               }];
    
}

@end
