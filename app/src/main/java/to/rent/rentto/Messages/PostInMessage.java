package to.rent.rentto.Messages;

public class PostInMessage {
    public String imageURL;
    public String title;
    public String author;
    public String message;
    public String postID;

    public PostInMessage(){

    }

    public PostInMessage(String imageURL, String title, String postID) {
        this.imageURL = imageURL;
        this.title = title;
        this.postID = postID;
    }
}
