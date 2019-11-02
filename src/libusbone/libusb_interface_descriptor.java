package libusbone;
/**
 * \ingroup desc<br>
 * A structure representing the standard USB interface descriptor. This<br>
 * descriptor is documented in section 9.6.5 of the USB 2.0 specification.<br>
 * All multiple-byte fields are represented in host-endian format.<br>
 * <i>native declaration : /usr/include/limits.h:480</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class libusb_interface_descriptor extends com.sun.jna.Structure {
	/// Size of this descriptor (in bytes)
	public byte bLength;
	/**
	 * Descriptor type. Will have value<br>
	 * \ref libusb_descriptor_type::LIBUSB_DT_INTERFACE LIBUSB_DT_INTERFACE<br>
	 * in this context.
	 */
	public byte bDescriptorType;
	/// Number of this interface
	public byte bInterfaceNumber;
	/// Value used to select this alternate setting for this interface
	public byte bAlternateSetting;
	/**
	 * Number of endpoints used by this interface (excluding the control<br>
	 * endpoint).
	 */
	public byte bNumEndpoints;
	/// USB-IF class code for this interface. See \ref libusb_class_code.
	public byte bInterfaceClass;
	/**
	 * USB-IF subclass code for this interface, qualified by the<br>
	 * bInterfaceClass value
	 */
	public byte bInterfaceSubClass;
	/**
	 * USB-IF protocol code for this interface, qualified by the<br>
	 * bInterfaceClass and bInterfaceSubClass values
	 */
	public byte bInterfaceProtocol;
	/// Index of string descriptor describing this interface
	public byte iInterface;
	/**
	 * Array of endpoint descriptors. This length of this array is determined<br>
	 * by the bNumEndpoints field.<br>
	 * C type : libusb_endpoint_descriptor*
	 */
	public libusbone.libusb_endpoint_descriptor.ByReference endpoint;
	/**
	 * Extra descriptors. If libusb encounters unknown interface descriptors,<br>
	 * it will store them here, should you wish to parse them.<br>
	 * C type : const unsigned char*
	 */
	public com.sun.jna.Pointer extra;
	/// Length of the extra descriptors, in bytes.
	public int extra_length;
	public libusb_interface_descriptor() {
		super();
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
	protected libusb_interface_descriptor newInstance() {
		libusb_interface_descriptor s = new libusb_interface_descriptor();
		s.useMemory(getPointer());
		write();
		s.read();
		return s;
	}
	public static class ByReference extends libusb_interface_descriptor implements com.sun.jna.Structure.ByReference {}
	public static class ByValue extends libusb_interface_descriptor implements com.sun.jna.Structure.ByValue {}
}