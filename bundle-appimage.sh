dpkg-deb -x /home/joseph/Documents/TaskScheduler/composeApp/build/compose/binaries/main/deb/com.shabb.taskscheduler_1.0.0_amd64.deb TaskScheduler.AppDir
chmod +x /home/joseph/Documents/TaskScheduler/TaskScheduler.AppDir/opt/com.shabb.taskscheduler/bin/com.shabb.taskscheduler
chmod +x /home/joseph/Documents/TaskScheduler/TaskScheduler.AppDir/opt/com.shabb.taskscheduler/lib/com.shabb.taskscheduler-com.shabb.taskscheduler.desktop
mv TaskScheduler.AppDir/opt/com.shabb.taskscheduler/lib/com.shabb.taskscheduler-com.shabb.taskscheduler.desktop TaskScheduler.AppDir/
mv TaskScheduler.AppDir/opt/com.shabb.taskscheduler/lib/com.shabb.taskscheduler.png TaskScheduler.AppDir/
chmod +x TaskScheduler.AppDir/opt/com.shabb.taskscheduler/bin/com.shabb.taskscheduler
chmod +x TaskScheduler.AppDir/com.shabb.taskscheduler-com.shabb.taskscheduler.desktop
~/Downloads/pkg2appimage--x86_64.AppImage TaskScheduler.AppDir
rm -rf TaskScheduler.AppDir