android_file_monitor_utility
============================

 * Description: This is a prototype app, demonstrating how to use Android's file observer class to watch
 * for file changes in the public file system using the inotify subsystem in Linux. This is a fully
 * functioning app, but it is not "polished", as such it is most intended as an example.
 *
 * Specifically, there is a bug in older versions of the Android SDK documents which suggest that the FileObserver object
 * is recursive when watching a directory (i.e. it will watch child directories).  This is false.  However,
 * this code should provide an example of how to achieve this functionality by simply recursively creating
 * FileObservers (please note that when monitoring many directories, this may carry heavy memory burden).
 *
 * Architecture:
 * 1 - Activity (FileMonitorActivity): This class, provides the user interface for starting and stopping the monitoring service.
 * 1 - Service (FileMonitorService): Runs as a persistent foreground service which creates FileObserverNodes and keeps a live reference to the object
 *				so that the FileObserver classes are not garbage collected as per the docs.
 * 1 - Other Class (FileObserverNode): This class extends the FileObserver class, and implements some additional functionality.
 * 				Specifically, objects of this class know the fully qualified path they are watching, as well as the data structure
 * 				which contains all of the access data.
 *
 *  Written by: Chris Jarabek (chris.jarabek@gmail.com)
 *
 *  Origin: November 24, 2011
 *  Last Updated: November 24, 2013 - Ported to Android Studio + API level 19