//
//  MusicGameVC.swift
//  KotlinIOS
//
//  Created by Jacob Rein on 8/28/19.
//  Copyright © 2019 Evgeny Petrenko. All rights reserved.
//

import Foundation
import UIKit
import SharedCode
import SCLAlertView

class MusicGameVC: UIViewController {

    @IBOutlet weak var numCorrect: UILabel!
    @IBOutlet weak var snippetView: UILabel!
    @IBOutlet weak var nextQuestion: UIButton!
    @IBOutlet weak var aButton: UIButton!
    @IBOutlet weak var bButton: UIButton!
    @IBOutlet weak var cButton: UIButton!
    @IBOutlet weak var dButton: UIButton!
    @IBOutlet weak var instruct: UILabel!

    var currentTrack: Track? = nil
    var trackList: [Track] = []
    private var choices: [Track] = []

    var correct = 0
    var total = 0

    func setCorrect(num: Int) {
        correct = num
        numCorrect.text = "\(correct)/\(total)\n\(trackList.count) left"
    }

    func setTotal(num: Int) {
        total = num
        numCorrect.text = "\(correct)/\(total)\n\(trackList.count) left"
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.instruct.text = "Q: Song Snippet\nA: Song Name"

        self.getTracks()

    }

    private func getTracks() {
        let appearance = SCLAlertView.SCLAppearance(
                kTitleFont: UIFont(name: "HelveticaNeue", size: 20)!,
                kTextFont: UIFont(name: "HelveticaNeue", size: 14)!,
                kButtonFont: UIFont(name: "HelveticaNeue-Bold", size: 14)!,
                showCloseButton: false
        )
        let alert = SCLAlertView(appearance: appearance)
        let numTracks = alert.addTextField("Number Of Songs to Have (Default is 100)")
        let artistName = alert.addTextField("Artist Name")
        alert.addButton("OKAY!") {
            DispatchQueue.main.async {
                DispatchQueue.main.async {
                    var num = 0
                    do {
                        num = Int(numTracks.text!) ?? 100
                    } catch {
                        num = 100
                    }
                    self.trackList = TrackApi().getTrackByInfo(trackName: "", artistName: artistName.text, anyLyrics: "", amount: Int32(num))
                    for i in self.trackList {
                        track(i.track_name)
                    }
                    self.questionSetUp()
                }
            }
        }
        alert.addButton("NEVER MIND") {
            self.dismiss(animated: true)
        }
        alert.showEdit("Customize the game", subTitle: "Choose What Songs Can Be Shown")
    }

    private func questionSetUp() {
        if (self.trackList.count <= 4) {
            self.getTracks()
        } else {
            self.choices.removeAll()
            self.currentTrack = self.trackList.randomRemove()
            self.choices.append(currentTrack!)
            self.choices.append(self.trackList.randomRemove()!)
            self.choices.append(self.trackList.randomRemove()!)
            self.choices.append(self.trackList.randomRemove()!)
            self.choices.shuffle()
            aButton.setTitle("(A) \(self.choices[0].track_name)", for: .normal)
            bButton.setTitle("(B) \(self.choices[1].track_name)", for: .normal)
            cButton.setTitle("(C) \(self.choices[2].track_name)", for: .normal)
            dButton.setTitle("(D) \(self.choices[3].track_name)", for: .normal)
            DispatchQueue.main.async {
                track(self.currentTrack!.track_name)
                let snip = LyricApi().getLyricSnippet(track: self.currentTrack!)
                let question = snip?.snippet_body ?? "Sorry, something went wrong.\nFor your troubles: \(self.currentTrack!.track_name)"
                track(question)
                self.snippetView.text = question
            }
            self.numCorrect.text = "\(self.correct)/\(self.total)\n\(self.trackList.count) left"
            self.nextQuestion.isEnabled = false
            self.enableButtons(enable: true)
        }
    }

    private func guessed(num: Int) {
        if (self.choices[num] == self.currentTrack!) {
            self.setCorrect(num: self.correct + 1)
            getButton(num: num)!.setTitle("\(getButton(num: num)!.currentTitle!) ✅", for: .normal)
        } else {
            let location = choices.firstIndex(of: self.currentTrack!)!
            getButton(num: location)!.setTitle("\(getButton(num: location)!.currentTitle!) ✅", for: .normal)
        }
        setTotal(num: total + 1)
        self.nextQuestion.isEnabled = true
        self.enableButtons(enable: false)
    }

    private func getButton(num: Int) -> UIButton? {
        switch num {
        case 0:
            return aButton
        case 1:
            return bButton
        case 2:
            return cButton
        case 3:
            return dButton
        default:
            return nil
        }
    }

    private func enableButtons(enable: Bool) {
        self.aButton.isEnabled = enable
        self.bButton.isEnabled = enable
        self.cButton.isEnabled = enable
        self.dButton.isEnabled = enable
    }

    @IBAction func aPress(_ sender: Any) {
        self.guessed(num: 0)
    }

    @IBAction func bPress(_ sender: Any) {
        self.guessed(num: 1)
    }

    @IBAction func cPress(_ sender: Any) {
        self.guessed(num: 2)
    }

    @IBAction func dPress(_ sender: Any) {
        self.guessed(num: 3)
    }

    @IBAction func nextQuest(_ sender: Any) {
        self.questionSetUp()
    }
}

extension Array {
    /*mutating func randomRemove() -> Element {
        let randomIndex = Int(arc4random_uniform(UInt32(count)))
        return remove(at: randomIndex)
    }*/

    mutating func randomRemove() -> Element? {
        if isEmpty {
            return nil
        }
        let index = Int(arc4random_uniform(UInt32(self.count)))
        return remove(at: index)
    }
}
