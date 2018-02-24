package to.rent.rentto.Models;

/**
 * Created by Quan Nguyen on 2/22/2018.
 */

public class Item {
    public String imageURL;
    public String rate;
    public String title;
    public String category;
    public String condition;
    public String description;
    public String userUID;
    public Item() {

    }

    public Item(String imageURL, String rate, String description, String title, String category, String condition, String userUID) {
        this.rate = rate;
        this.imageURL = imageURL;
        this.description = description;
        this.title = title;
        this.category = category;
        this.condition = condition;
        this.userUID = userUID;
    }
}