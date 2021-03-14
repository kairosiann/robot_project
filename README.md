# Robot Project Code    

The code in the following repository is split into two parts: android and ros.     
ROS (Robot Operating System) is used to control the manipulator and mobile base, while Android is used to control the end effector.     

##Running Robot Project    
###Base Control:    
```    
roslaunch turtlebot3_bringup turtlebot3_robot.launch    
rolsaunch turtlebot3_teleop turtlebot3_teleop_key.launch    
roslaunch turtlebot3_navigation robot_project_navigation.launch map_file:=$HOME/map.yaml    
roslaunch turtlebot3_navigation robot_project_navigation_rviz.launch     
rosrun move_robot_project move_robot.py    
```    

###Arm Control:    
```    
roslaunch camera bringup_cam.launch    
roslaunch yolov3_pytorch_ros detector.launch    
rosrun yolov3_pytorch_ros detect_person.py    
rosrun inverse_kinematics arm_command.py     
rosrun inverse_kinematics ik_move.py    
```    

###Django Control:     
```    
redis-server    
python manage.py runserver 0.0.0.0:8000 (inside the venv in django-ros-turtlesim-server)    
roslaunch django-ros-turtlesim-client run.launch    
```   

##Additional Information    
###SLAM (Simultaneous Localization and Mapping) using Gmapping Package    
```    
roslaunch turtlebot3_slam robot_project_slam.launch    
roslaunch turtlebot3_slam robot_project_slam_rviz.launch    
```    

###Android Wireless Debugging:    
``    
adb kill-server    
(connect w/ cable) adb tcpip 5555    
adb connect 192.168.x.xx    
(then you can disconnect the cable)    
```    

###if redis-server does not work, try:    
```    
sudo service redis-server stop    
redis-server    
```    
