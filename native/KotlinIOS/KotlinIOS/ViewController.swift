import UIKit
import SharedCode

class ViewController: UIViewController {

    @IBOutlet weak var liveButton: UIButton!
    @IBOutlet weak var cartoonButton: UIButton!
    @IBOutlet weak var musicButton: UIButton!
    @IBOutlet weak var kotlinIsAwesome: UILabel!
    
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
