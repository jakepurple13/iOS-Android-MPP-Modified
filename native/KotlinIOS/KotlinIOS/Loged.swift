//
// Created by Jacob Rein on 2019-08-12.
// Copyright (c) 2019 Jake Rein. All rights reserved.
//

import Foundation
import UIKit

public func track(_ message: Any?, file: String = #file, function: String = #function, line: Int = #line) {
    print("\(message ?? "null") called from \(function) \(file):\(line)")
}

public enum Toast {
    case LENGTH_SHORT, LENGTH_LONG

    public var time: TimeInterval {
        switch self {
        case .LENGTH_LONG:
            return 5.0
        case .LENGTH_SHORT:
            return 2.5
        }
    }

}

extension UIViewController {
    public func showToast(message: String, duration: TimeInterval = 4.0, delay: TimeInterval = Toast.LENGTH_SHORT.time) {
        let toastLabel = UILabel(frame: CGRect(x: self.view.frame.size.width / 2 - 75, y: self.view.frame.size.height - 100, width: 150, height: 35))
        toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastLabel.textColor = UIColor.white
        toastLabel.font = .boldSystemFont(ofSize: 16.0)
        toastLabel.textAlignment = .center;
        toastLabel.text = message
        toastLabel.alpha = 1.0
        toastLabel.layer.cornerRadius = 10;
        toastLabel.clipsToBounds = true
        self.view.addSubview(toastLabel)
        UIView.animate(withDuration: duration, delay: delay, options: .curveEaseOut, animations: {
            toastLabel.alpha = 0.0
        }, completion: { (isCompleted) in
            toastLabel.removeFromSuperview()
        })
    }
}

extension String {
    func track() {
        KotlinIOS.track(self)
    }
}

//Make this generic for swipe and tap
extension UIView {
    @discardableResult func addGesture<T: EasyGesture>(_ gestureRecognizer: T) -> T {
        let tap: EasyGesture = gestureRecognizer
        tap.addTarget(self, action: #selector(runActions(actions:)))
        self.addGestureRecognizer(tap)
        self.isUserInteractionEnabled = true
        return tap as! T
    }

    @discardableResult func addGesture<T: EasyGesture>(setup: @escaping (T) -> Void, actions: @escaping (UIView, EasyGesture) -> Void) -> T {
        let tap: EasyGesture = T()
        setup(tap as! T)
        tap.userAction = actions
        tap.addTarget(self, action: #selector(runActions(actions:)))
        self.addGestureRecognizer(tap)
        self.isUserInteractionEnabled = true
        return tap as! T
    }

    @objc internal func runActions(actions: EasyGestureAction) {
        let eg = actions as EasyGesture
        eg.userAction!(self, eg)
    }

    func removeAllGestures() {
        if let recognizers = gestureRecognizers {
            for recognizer in recognizers {
                removeGestureRecognizer(recognizer)
            }
        }
    }

    func removeGesture<T: EasyGesture>(gesture: T, customCheck: @escaping ((EasyGesture) -> Bool) = {_ in true}) {
        if let recognizers = gestureRecognizers {
            for recognizer in recognizers {
                if(recognizer is T && customCheck(recognizer as! T)) {
                    removeGestureRecognizer(recognizer)
                }
            }
        }
    }
}

protocol EasyGesture: UIGestureRecognizer {
    var userAction: ((UIView, EasyGesture) -> Void)? { get set }
}

internal class EasyGestureAction: UIGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyTapGesture: UITapGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasySwipeGesture: UISwipeGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyLongPressGesture: UILongPressGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyPanGesture: UIPanGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyRotationGesture: UIRotationGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyScreenEdgePanGesture: UIScreenEdgePanGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

class EasyPinchGesture: UIPinchGestureRecognizer, EasyGesture {
    var userAction: ((UIView, EasyGesture) -> Void)? = nil
}

public struct EasyDirection {
    public static var right: UISwipeGestureRecognizer.Direction = .right
    public static var left: UISwipeGestureRecognizer.Direction = .left
    public static var up: UISwipeGestureRecognizer.Direction = .up
    public static var down: UISwipeGestureRecognizer.Direction = .down
}