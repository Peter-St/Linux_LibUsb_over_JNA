package libusbone;
/**
 * <i>native declaration : /usr/include/bits/pthreadtypes.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class pthread_rwlockattr_t extends com.sun.jna.Union {
	/// C type : char[8]
	public byte[] __size = new byte[(8)];
	public int __align;
	public pthread_rwlockattr_t() {
		super();
	}
	public pthread_rwlockattr_t(int __align) {
		super();
		this.__align = __align;
		setType(java.lang.Integer.TYPE);
	}
	/// @param __size C type : char[8]
	public pthread_rwlockattr_t(byte __size[]) {
		super();
		if (__size.length != this.__size.length) 
			throw new java.lang.IllegalArgumentException("Wrong array size !");
		this.__size = __size;
		setType(byte[].class);
	}
	protected ByReference newByReference() {
		ByReference s = new ByReference();
		s.useMemory(getPointer());
		write();
		s.read();
		return s;
	}
	protected ByValue newByValue() {
		ByValue s = new ByValue();
		s.useMemory(getPointer());
		write();
		s.read();
		return s;
	}
	protected pthread_rwlockattr_t newInstance() {
		pthread_rwlockattr_t s = new pthread_rwlockattr_t();
		s.useMemory(getPointer());
		write();
		s.read();
		return s;
	}
	public static class ByReference extends pthread_rwlockattr_t implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends pthread_rwlockattr_t implements com.sun.jna.Structure.ByValue {}
}
