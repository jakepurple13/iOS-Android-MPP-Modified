//
//  ShowTableViewController.swift
//  KotlinIOS
//
//  Created by Jacob Rein on 8/27/19.
//  Copyright © 2019 Evgeny Petrenko. All rights reserved.
//

import Foundation
import SharedCode
import UIKit
import SwipeTransition

extension ShowTableViewController: UISearchResultsUpdating {
    // MARK: - UISearchResultsUpdating Delegate
    func updateSearchResults(for searchController: UISearchController) {

    }
}

class ShowTableViewCell: UITableViewCell {
    @IBOutlet weak var nameToShow: UILabel!
}

class ShowTableViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate, UISearchDisplayDelegate {

    @IBOutlet weak var navBar: UINavigationBar!
    @IBOutlet weak var titleBar: UINavigationItem!

    // MARK: - Table view data source
    var list = [ShowInfo]()
    var source: Source? = nil
    var shows: ShowApi? = nil
    var filteredShows = [ShowInfo]()

    @IBOutlet weak var tableView: UITableView!

    var searchBars: UISearchBar!

    func backAction() {
        dismiss(animated: true, completion: nil)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.black

        self.searchBars = UISearchBar()
        self.searchBars.placeholder = "Search Shows in \(self.source!)"
        self.searchBars.showsCancelButton = false
        self.searchBars.delegate = self
        self.searchBars.sizeToFit()

        titleBar.titleView = searchBars

        definesPresentationContext = true
        self.navigationController?.swipeBack?.isEnabled = false
        self.swipeToDismiss?.isEnabled = true

        DispatchQueue.main.async {
            DispatchQueue.main.async {
                self.shows = ShowApi(source: self.source!)
                self.list = (self.shows?.showInfoList)!
                self.filteredShows = self.list
                self.tableView.dataSource = self
                self.tableView.delegate = self
                self.tableView.reloadData()
            }
        }
    }

    // This method updates filteredData based on the text in the Search Box
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        // When there is no text, filteredData is the same as the original data
        // When user has entered text into the search box
        // Use the filter method to iterate over all items in the data array
        // For each item, return true if the item should be included and false if the
        // item should NOT be included
        searchBar.showsCancelButton = false
        if let searchText = searchBar.text {
            filteredShows = searchText.isEmpty ? list : list.filter({ (dataString: ShowInfo) -> Bool in
                dataString.name.range(of: searchText, options: .caseInsensitive) != nil
            })
            tableView.reloadData()
        }
    }

    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        // Stop doing the search stuff
        // and clear the text in the search bar
        searchBar.text = ""
        // Hide the cancel button
        searchBar.showsCancelButton = false
        // You could also change the position, frame etc of the searchBar
        self.filteredShows = self.list
        self.tableView.reloadData()
    }

    func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        //track("here")
        //return list.count
        return filteredShows.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ShowTableViewCell", for: indexPath) as! ShowTableViewCell
        print("\(#function) --- section = \(indexPath.section), row = \(indexPath.row)")
        // Fetches the appropriate meal for the data source layout.
        let show = filteredShows[indexPath.row]

        cell.nameToShow?.text = show.name

        cell.tag = indexPath.row

        cell.textLabel?.textColor = UIColor.white
        cell.backgroundColor = UIColor.black

        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        track("here")
        track("section: \(indexPath.section)")
        track("row: \(indexPath.row)")
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)

        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "episodeactivity") as! EpisodeViewController

        nextViewController.showInfo = filteredShows[indexPath.row]

        self.present(nextViewController, animated: true, completion: nil)
    }

}
