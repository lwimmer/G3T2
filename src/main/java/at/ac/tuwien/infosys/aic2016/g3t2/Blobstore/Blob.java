package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Blob {
	private byte[] data;
	private String location;
	
    public byte[] getData() {
        throw new NotImplementedException();
    }

    public Location getLocation2() {
        throw new NotImplementedException();
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
