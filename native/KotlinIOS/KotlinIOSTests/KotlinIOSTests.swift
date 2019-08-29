//
//  KotlinIOSTests.swift
//  KotlinIOSTests
//
//  Created by Evgeny Petrenko on 02.08.2019.
//  Copyright © 2019 Evgeny Petrenko. All rights reserved.
//

import XCTest
import SharedCode
@testable import KotlinIOS

class KotlinIOSTests: XCTestCase {

    override func setUp() {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
        //print(NSURL(string: Source.liveAction.link)?.absoluteString)
        //ActualKt.getShows(source: Source.liveAction)
        //let s = ActualKt.getShow(source: Source.liveAction)
        let s = ShowApi(Source.liveAction)
        print(s.showInfoList)
        let s1 = ShowApi(Source.recentAnime)
        print(s1.showInfoList)
        let s2 = ShowApi(Source.recentCartoon)
        print(s2.showInfoList)
    }

    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }
    
}
