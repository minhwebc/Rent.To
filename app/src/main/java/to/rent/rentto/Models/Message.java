package to.rent.rentto.Models;

/**
 * Created by Quan Nguyen on 3/29/2018.
 */

public class Message {
    public String authorID;
    public String author;
    public String text;
    public String date;
    public boolean belongsToCurrentUser; // is this message sent by us?


    public Message(String author, String text, String date, boolean belongsToCurrentUser, String authorID) {
        this.author = author;
        this.text = text;
        this.date = date;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.authorID = authorID;
    }

    public Message(){

    }

    public String getAuthorID(){
        return this.authorID;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getText() {
        return text;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

}
