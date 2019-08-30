//
//  BookVC.swift
//  KotlinIOS
//
//  Created by Jacob Rein on 8/30/19.
//  Copyright © 2019 Evgeny Petrenko. All rights reserved.
//

import Foundation
import UIKit
import SharedCode
import SwipeTransition

class BookTableViewCell: UITableViewCell {
    @IBOutlet weak var bookTitle: UILabel!
    @IBOutlet weak var bookCover: UIImageView!
    @IBOutlet weak var bookSubtitle: UILabel!
    @IBOutlet weak var bookAuthor: UILabel!
}

class BookVC: UITableViewController {//UIViewController, UITableViewDataSource, UITableViewDelegate {

    var books = [Book]()

    //@IBOutlet weak var tableView: UITableView!

    override func viewDidLoad() {
        super.viewDidLoad()
        //self.tableView.dataSource = self
        //self.tableView.delegate = self

        getBooks(search: "Cirque Du Freak")

    }

    private func getBooks(search: String) {
        DispatchQueue.main.async {
            self.books = CommonKt.searchForBook(search: search)
            self.tableView.register(BookTableViewCell.self, forCellReuseIdentifier: "BookTableViewCell")
            self.tableView.reloadData()
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BookTableViewCell", for: indexPath) as! BookTableViewCell

        print("\(#function) --- section = \(indexPath.section), row = \(indexPath.row)")
        // Fetches the appropriate meal for the data source layout.
        let book = books[indexPath.row]

        cell.bookTitle?.text = book.title
        cell.bookSubtitle?.text = book.subtitle
        cell.bookAuthor?.text = book.author
        cell.bookCover?.kf.setImage(with: URL(string: book.getCoverUrl(size: CoverSize.medium)))

        if (indexPath.row % 5 == 0) {
            //track(book)
            track(cell.bookTitle?.text ?? "null")
        }

        cell.tag = indexPath.row
        /*cell.textLabel?.text = book.title
        cell.textLabel?.numberOfLines = 20
        cell.textLabel?.textColor = UIColor.white
        cell.detailTextLabel?.text = "\(book.subtitle)\n\(book.author)"
        cell.detailTextLabel?.numberOfLines = 20
        cell.detailTextLabel?.textColor = UIColor.white
        cell.imageView?.kf.setImage(with: URL(string: book.getCoverUrl(size: CoverSize.medium)))*/
        cell.backgroundColor = UIColor.black

        return cell
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return books.count
    }


    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //super.tableView(tableView, didSelectRowAt: indexPath)
        tableView.deselectRow(at: indexPath, animated: true)
    }

}
