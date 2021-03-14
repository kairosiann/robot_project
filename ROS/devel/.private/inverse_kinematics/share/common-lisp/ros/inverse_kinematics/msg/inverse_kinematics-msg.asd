
(cl:in-package :asdf)

(defsystem "inverse_kinematics-msg"
  :depends-on (:roslisp-msg-protocol :roslisp-utils )
  :components ((:file "_package")
    (:file "GoalPosition" :depends-on ("_package_GoalPosition"))
    (:file "_package_GoalPosition" :depends-on ("_package"))
  ))