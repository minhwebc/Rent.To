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
        String defaultPic = "https://firebasestorage.googleapis.com/v0/b/rentto-f0093.appspot.com/o/600px-Default_profile_picture_(male)_on_Facebook.jpg?alt=media&token=166b9b02-1528-448a-ba2c-5ee8c7cb4522";
        return (profile_photo == null) ? defaultPic : profile_photo;
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