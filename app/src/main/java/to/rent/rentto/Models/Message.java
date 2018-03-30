package to.rent.rentto.Models;

/**
 * Created by Quan Nguyen on 3/29/2018.
 */

public class Message {
    public String author;
    public String text;
    public String date;

    public Message(String author, String text, String date) {
        this.author = author;
        this.text = text;
        this.date = date;
    }

    public Message(){

    }

}
