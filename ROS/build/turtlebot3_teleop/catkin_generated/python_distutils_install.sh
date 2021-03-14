#!/bin/sh

if [ -n "$DESTDIR" ] ; then
    case $DESTDIR in
        /*) # ok
            ;;
        *)
            /bin/echo "DESTDIR argument must be absolute... "
            /bin/echo "otherwise python's distutils will bork things."
            exit 1
    esac
fi

echo_and_run() { echo "+ $@" ; "$@" ; }

echo_and_run cd "/home/ryan/robot_project/src/turtlebot3/turtlebot3_teleop"

# ensure that Python install destination exists
echo_and_run mkdir -p "$DESTDIR/home/ryan/robot_project/install/lib/python2.7/dist-packages"

# Note that PYTHONPATH is pulled from the environment to support installing
# into one location when some dependencies were installed in another
# location, #123.
echo_and_run /usr/bin/env \
    PYTHONPATH="/home/ryan/robot_project/install/lib/python2.7/dist-packages:/home/ryan/robot_project/build/turtlebot3_teleop/lib/python2.7/dist-packages:$PYTHONPATH" \
    CATKIN_BINARY_DIR="/home/ryan/robot_project/build/turtlebot3_teleop" \
    "/usr/bin/python2" \
    "/home/ryan/robot_project/src/turtlebot3/turtlebot3_teleop/setup.py" \
     \
    build --build-base "/home/ryan/robot_project/build/turtlebot3_teleop" \
    install \
    --root="${DESTDIR-/}" \
    --install-layout=deb --prefix="/home/ryan/robot_project/install" --install-scripts="/home/ryan/robot_project/install/bin"
