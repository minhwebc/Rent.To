package to.rent.rentto.Models;

public class MessagePost {
    private String imageURL;
    private String postID;
    private String title;
    private String userUID;

    public MessagePost(String imageURL, String postID, String title, String userUID) {
        this.imageURL = imageURL;
        this.postID = postID;
        this.title = title;
        this.userUID = userUID;
    }

    public MessagePost() {

    }

    public String getImageURL() {
        return this.imageURL;
    }

    public String getPostID() {
        return this.postID;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUserUID() {
        return this.userUID;
    }
}
