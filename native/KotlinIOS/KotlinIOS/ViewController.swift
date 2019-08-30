import UIKit
import SharedCode

class ViewController: UIViewController {

    @IBOutlet weak var liveButton: UIButton!
    @IBOutlet weak var cartoonButton: UIButton!
    @IBOutlet weak var musicButton: UIButton!
    @IBOutlet weak var kotlinIsAwesome: UILabel!
    @IBOutlet weak var jokeOfTheDay: UILabel!
    @IBOutlet weak var bookButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        var count = 0

        kotlinIsAwesome.textAlignment = .center
        kotlinIsAwesome.textColor = UIColor.white
        kotlinIsAwesome.numberOfLines = 10
        kotlinIsAwesome.font = kotlinIsAwesome.font.withSize(20)
        kotlinIsAwesome.text = CommonKt.createApplicationScreenMessage()
        kotlinIsAwesome.addGesture(setup: { (easy: EasyTapGesture) in }, actions: { view, gesture in
            count += 1
            self.kotlinIsAwesome.text = "\(CommonKt.createApplicationScreenMessage())\npressed \(count) times"
        })
        print(CommonKt.getNewString())
        print(CommonKt.getOldString())
        CommonKt.doNewMessaged()

        DispatchQueue.main.async {
            // Getting
            let defaults = UserDefaults.standard
            let savedJoke = Joke.Companion().fromJSONString(json: defaults.string(forKey: "dailyjokes") ?? "") ?? Joke(title: "Sorry", joke: "No Joke", date: "2000-1-1")
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            dateFormatter.locale = Locale(identifier: "en_US_POSIX")
            let date = dateFormatter.date(from: savedJoke.date)!
            var joke: Joke
            if (!self.isSameDay(date1: date, date2: Date())) {
                joke = CommonKt.getJokeOfTheDay() ?? savedJoke
            } else {
                joke = savedJoke
            }
            self.jokeOfTheDay.text = "\(joke.title)\n\(joke.joke)"
            // Setting
            defaults.set(joke.toJSONString(), forKey: "dailyjokes")
        }

    }

    private func isSameDay(date1: Date, date2: Date) -> Bool {
        let diff = Calendar.current.dateComponents([.day], from: date1, to: date2)
        if diff.day == 0 && diff.year == 0 {
            return true
        } else {
            return false
        }
    }

    @IBAction func bookSend(_ sender: Any) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)

        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "bookactivity") as! BookVC

        self.present(nextViewController, animated: true, completion: nil)
    }
    
    @IBAction func musicSend(_ sender: Any) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)

        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "musicactivity") as! MusicGameVC

        self.present(nextViewController, animated: true, completion: nil)
    }

    @IBAction func liveSend(_ sender: Any) {
        sendToShows(source: Source.liveAction)
    }

    @IBAction func cartoonSend(_ sender: Any) {
        sendToShows(source: Source.cartoon)
    }

    func sendToShows(source: Source) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)

        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "showactivity") as! ShowTableViewController
        nextViewController.source = source

        self.present(nextViewController, animated: true, completion: nil)
    }

    @objc func onPress() {
        //ActualKt.getAndShow(Source.liveAction.link)
    }

}
