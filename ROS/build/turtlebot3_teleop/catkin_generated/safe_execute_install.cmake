execute_process(COMMAND "/home/ryan/robot_project/build/turtlebot3_teleop/catkin_generated/python_distutils_install.sh" RESULT_VARIABLE res)

if(NOT res EQUAL 0)
  message(FATAL_ERROR "execute_process(/home/ryan/robot_project/build/turtlebot3_teleop/catkin_generated/python_distutils_install.sh) returned error code ")
endif()
