package to.rent.rentto.Models;

/**
 * Created by allencho on 2/27/18.
 */

public class UserAccountSettings {

    public String description;
    public String display_name;
    public long posts;
    public String profile_photo;
    public String username;
    public String website;

    public UserAccountSettings(String description, String display_name,
                               long posts, String profile_photo, String username, String website) {
        this.description = description;
        this.display_name = display_name;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.username = username;
        this.website = website;
    }
    public UserAccountSettings() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getPosts() {
        return 1;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return "https://firebasestorage.googleapis.com/v0/b/rentto-f0093.appspot.com/o/chrome_2018-03-05_21-10-33.png?alt=media&token=ed0b7cd0-0eb3-41ea-a082-5cb3c3f862fd";
        //return "https://firebasestorage.googleapis.com/v0/b/rentto-f0093.appspot.com/o/flat%2C800x800%2C075%2Cf.u1.jpg?alt=media&token=d77b3c5b-7355-4e77-a3f9-6b5c92a1b167";
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }


    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}