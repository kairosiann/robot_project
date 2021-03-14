;; Auto-generated. Do not edit!


(when (boundp 'dynamixel_ros_control::HomingFeedback)
  (if (not (find-package "DYNAMIXEL_ROS_CONTROL"))
    (make-package "DYNAMIXEL_ROS_CONTROL"))
  (shadow 'HomingFeedback (find-package "DYNAMIXEL_ROS_CONTROL")))
(unless (find-package "DYNAMIXEL_ROS_CONTROL::HOMINGFEEDBACK")
  (make-package "DYNAMIXEL_ROS_CONTROL::HOMINGFEEDBACK"))

(in-package "ROS")
;;//! \htmlinclude HomingFeedback.msg.html


(defclass dynamixel_ros_control::HomingFeedback
  :super ros::object
  :slots (_is_moving ))

(defmethod dynamixel_ros_control::HomingFeedback
  (:init
   (&key
    ((:is_moving __is_moving) nil)
    )
   (send-super :init)
   (setq _is_moving __is_moving)
   self)
  (:is_moving
   (&optional __is_moving)
   (if __is_moving (setq _is_moving __is_moving)) _is_moving)
  (:serialization-length
   ()
   (+
    ;; bool _is_moving
    1
    ))
  (:serialize
   (&optional strm)
   (let ((s (if strm strm
              (make-string-output-stream (send self :serialization-length)))))
     ;; bool _is_moving
       (if _is_moving (write-byte -1 s) (write-byte 0 s))
     ;;
     (if (null strm) (get-output-stream-string s))))
  (:deserialize
   (buf &optional (ptr- 0))
   ;; bool _is_moving
     (setq _is_moving (not (= 0 (sys::peek buf ptr- :char)))) (incf ptr- 1)
   ;;
   self)
  )

(setf (get dynamixel_ros_control::HomingFeedback :md5sum-) "552aeb9cf1ea5fa2b0b9d933540d4e52")
(setf (get dynamixel_ros_control::HomingFeedback :datatype-) "dynamixel_ros_control/HomingFeedback")
(setf (get dynamixel_ros_control::HomingFeedback :definition-)
      "# ====== DO NOT MODIFY! AUTOGENERATED FROM AN ACTION DEFINITION ======
# feedback
bool is_moving

")



(provide :dynamixel_ros_control/HomingFeedback "552aeb9cf1ea5fa2b0b9d933540d4e52")

