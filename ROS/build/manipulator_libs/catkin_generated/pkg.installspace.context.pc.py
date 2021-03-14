# generated from catkin/cmake/template/pkg.context.pc.in
CATKIN_PACKAGE_PREFIX = ""
PROJECT_PKG_CONFIG_INCLUDE_DIRS = "${prefix}/include;/usr/include/eigen3".split(';') if "${prefix}/include;/usr/include/eigen3" != "" else []
PROJECT_CATKIN_DEPENDS = "roscpp;robotis_manipulator;dynamixel_workbench_toolbox;cmake_modules".replace(';', ' ')
PKG_CONFIG_LIBRARIES_WITH_PREFIX = "-lmanipulator_libs".split(';') if "-lmanipulator_libs" != "" else []
PROJECT_NAME = "manipulator_libs"
PROJECT_SPACE_DIR = "/home/ryan/robot_project/install"
PROJECT_VERSION = "2.0.1"
