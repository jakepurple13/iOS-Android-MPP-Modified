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

    var book: Book! {
        didSet {
            bookTitle?.text = book.title
            bookSubtitle?.text = book.subtitle
            bookAuthor?.text = book.author
            bookCover?.kf.setImage(with: URL(string: book.getCoverUrl(size: CoverSize.medium)))
        }
    }
}

class BookVC: UITableViewController {

    var books = [Book]() {
        didSet {
            self.tableView.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.tableView.reloadData()
        //self.tableView.register(BookTableViewCell.self, forCellReuseIdentifier: "BookTableViewCell")

        getBooks(search: "Cirque Du Freak")

    }

    private func getBooks(search: String) {
        DispatchQueue.main.async {
            self.books = CommonKt.searchForBook(search: search)
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BookTableViewCell", for: indexPath) as! BookTableViewCell
        print("\(#function) --- section = \(indexPath.section), row = \(indexPath.row)")
        cell.book = books[indexPath.row]
        if (indexPath.row % 5 == 0) {
            track(cell.bookTitle?.text ?? "null")
        }
        return cell
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return books.count
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }

}
