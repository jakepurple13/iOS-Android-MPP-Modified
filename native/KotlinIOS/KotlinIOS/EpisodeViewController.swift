//
//  EpisodeViewController.swift
//  KotlinIOS
//
//  Created by Jacob Rein on 8/28/19.
//  Copyright © 2019 Evgeny Petrenko. All rights reserved.
//

import UIKit
import SharedCode
import Kingfisher
import SwipeTransition

class EpisodeTableCell: UITableViewCell {
    @IBOutlet weak var episodeNumber: UILabel!
}

class EpisodeViewController: UIViewController, UITableViewDataSource {

    @IBOutlet weak var coverImage: UIImageView!
    @IBOutlet weak var descriptionOfShow: UITextView!
    @IBOutlet weak var episodeList: UITableView!

    @IBOutlet weak var titleItem: UINavigationItem!

    var defaultSession: URLSession!
    var downloadTask: URLSessionDownloadTask!

    var showInfo: ShowInfo? = nil
    var list = [EpisodeInfo]()
    var shows: EpisodeApi? = nil
    var videoTitle = ""
    var videoDes = ""
    var videoImage = ""

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.black
        descriptionOfShow.textColor = UIColor.white
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true

        DispatchQueue.main.async {
            DispatchQueue.main.async {
                self.shows = EpisodeApi(source: self.showInfo!, timeOut: 1000)
                track("Name: \(self.shows!.name)")
                track("ImageURL: \(self.shows!.image)")
                track("Des: \(self.shows!.showDescription)")
                track("Episode Count: \(self.shows!.episodeList.count)")
                self.videoTitle = self.shows!.name
                self.titleItem.title = self.videoTitle
                self.videoDes = self.shows!.showDescription
                self.descriptionOfShow.text = "\(self.shows!.source.url)\n\(self.videoDes)"
                self.videoImage = self.shows!.image
                self.coverImage.kf.setImage(with: URL(string: self.videoImage))
                self.list = self.shows!.episodeList
                self.episodeList.dataSource = self
                self.episodeList.reloadData()
            }
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return list.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "episode_number")! as! EpisodeTableCell //1.

        let text = list[indexPath.row] //2.

        //cell.textLabel?.text = text //3.
        cell.episodeNumber.text = text.name

        cell.episodeNumber.textColor = UIColor.white
        cell.backgroundColor = UIColor.black
        return cell //4.
    }

}
