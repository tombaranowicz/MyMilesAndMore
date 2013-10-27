//
//  UIViewController+dismiss.m
//  My Miles & More
//
//  Created by Mateusz Glapiak on 10/26/13.
//  Copyright (c) 2013 Mateusz Glapiak. All rights reserved.
//

#import "UIViewController+dismiss.h"

@implementation UIViewController (dismiss)

-(void)dismissViewControllerShortcut
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void)insertCloseButton
{
    UIBarButtonItem* barButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(dismissViewControllerShortcut)];
    
    self.navigationItem.leftBarButtonItem = barButtonItem;
}

@end