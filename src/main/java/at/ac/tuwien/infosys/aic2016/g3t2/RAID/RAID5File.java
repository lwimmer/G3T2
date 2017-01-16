package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;

public class RAID5File implements Comparable<RAID5File> {
    
    protected static final byte[] HEADER = "AIC2016G3T2V1R5".getBytes(StandardCharsets.US_ASCII);
    protected static final int DIGEST_BYTES = 256 / 8;
    protected Location location;
    protected final long fileSize;
    protected final int numParts;
    protected final int partNum;
    protected final boolean isParity;
    protected final byte[] data;
    
    public RAID5File(long fileSize, int numParts, int partNum, boolean isParity, byte[] data) {
        this.location = null;
        this.fileSize = fileSize;
        this.numParts = numParts;
        this.partNum = partNum;
        this.isParity = isParity;
        this.data = data;
    }

    public RAID5File(Location location, byte[] file) throws IOException {
        this.location = location;
        final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(file));
        final byte[] header = new byte[HEADER.length];
        if (dis.read(header) != HEADER.length || ! Arrays.equals(HEADER, header))
            throw new IOException("Illegal header");
        this.fileSize = dis.readLong();
        this.numParts = dis.readInt();
        this.partNum = dis.readInt();
        this.isParity = dis.readBoolean();
        final byte[] hash = new byte[DIGEST_BYTES];
        if (dis.read(hash) != hash.length)
            throw new IOException("could not read hash");
        this.data = new byte[dis.available()];
        if (dis.read(this.data) != this.data.length)
            throw new IOException("could not read data");
        if (! MessageDigest.isEqual(hash, DigestUtils.sha256(data)))
            throw new IOException("data corrupted");
    }

    public byte[] encode() {
        ByteArrayOutputStream baos;
        try {
            baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);
            dos.write(HEADER);
            dos.writeLong(fileSize);
            dos.writeInt(numParts);
            dos.writeInt(partNum);
            dos.writeBoolean(isParity);
            dos.write(DigestUtils.sha256(data));
            dos.write(data);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public long getFileSize() {
        return fileSize;
    }

    public int getNumParts() {
        return numParts;
    }

    public int getPartNum() {
        return partNum;
    }

    public boolean isParity() {
        return isParity;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(RAID5File o) {
        return Integer.compare(partNum, o.getPartNum());
    }
}
