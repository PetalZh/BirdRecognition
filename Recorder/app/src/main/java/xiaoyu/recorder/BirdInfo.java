package xiaoyu.recorder;

/**
 * Created by Xiaoyu on 10/7/2016.
 */
public class BirdInfo {
    private int id;
    private String name;
    private String date;
    private Double confident;
    private String description;
    private boolean recognised;

    public BirdInfo(int id, String name, String date, Double confident, String description, boolean recognised) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.confident = confident;
        this.description = description;
        this.recognised = recognised;
    }

    public BirdInfo(int id, String name) {
        //Get the image according to id
        this.id = id;
        this.name = name;
    }
    //empty constructor
    public  BirdInfo(){}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getConfident() {
        return confident;
    }

    public void setConfident(Double confident) {
        this.confident = confident;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRecognised() {
        return recognised;
    }

    public void setRecognised(boolean recognised) {
        this.recognised = recognised;
    }
}
