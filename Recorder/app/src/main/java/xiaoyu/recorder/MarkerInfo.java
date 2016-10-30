package xiaoyu.recorder;

/**
 * Created by Xiaoyu on 10/14/2016.
 */
public class MarkerInfo {
    private Double longitude;
    private Double latitude;
    private String birdName;
    private int birdId;

    public MarkerInfo(Double longitude, Double latitude, String birdName, int birdId) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.birdName = birdName;
        this.birdId = birdId;
    }
    public MarkerInfo(){}

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getBirdName() {
        return birdName;
    }

    public void setBirdName(String birdName) {
        this.birdName = birdName;
    }

    public int getBirdId() {
        return birdId;
    }

    public void setBirdId(int birdId) {
        this.birdId = birdId;
    }
}
