// ISillyService.aidl
package sh.tablet.bgclipboard;

// Declare any non-default types here with import statements

interface ISillyService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	IBinder getFs();
	IBinder getThis();
	boolean seteuid(int uid);
	boolean setegid(int gid);
}