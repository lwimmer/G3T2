package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

public class Blob {
	private byte[] data;

	public Blob(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
        return data;
    }
}
