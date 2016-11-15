package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

public class Blob {
	private byte[] data;
	private String location;
	
    public byte[] getData() {
        return data;
    }

    public Location getLocation2() {
        return new Location(location);
    }

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
