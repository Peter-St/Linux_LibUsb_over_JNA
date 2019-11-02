package libusbone;

public enum UsbEndpointType {
	CONTROL,
	ISOCHRONOUS,
	BULK,
	INTERRUPT;
	
	
	public static UsbEndpointType from(byte addressByte) {
		return UsbEndpointType.values()[addressByte & 3];
	}
}
