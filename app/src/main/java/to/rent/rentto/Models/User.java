package to.rent.rentto.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Quan Nguyen on 2/14/2018.
 */

public class User implements Parcelable {

    private String user_id;
    private String phone_number;
    private String email;
    private String username;
    private Double rating;
    private int totalRating;

    public User(String user_id, String phone_number, String email, String username, Double rating, int totalRating) {
        this.user_id = user_id;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
        this.rating = rating;
        this.totalRating = totalRating;
    }

    public User() {

    }


    protected User(Parcel in) {
        user_id = in.readString();
        phone_number = in.readString();
        email = in.readString();
        username = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getTotalRating() {return totalRating;
    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public double getRating() {return this.rating;}

    public void setRating(double rating) {this.rating = rating;}


    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(phone_number);
        dest.writeString(email);
        dest.writeString(username);
    }
}