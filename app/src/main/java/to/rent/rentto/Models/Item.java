package to.rent.rentto.Models;

/**
 * Created by Quan Nguyen on 2/22/2018.
 */

public class Item {
    public String photo_path;
    public Long price;
    public String description;
    public String item_name;
    public Item() {

    }

    public Item(String photo_path, Long price, String description, String item_name) {
        this.photo_path = photo_path;
        this.price = price;
        this.description = description;
        this.item_name = item_name;
    }
}
