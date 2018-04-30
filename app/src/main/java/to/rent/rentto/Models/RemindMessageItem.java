package to.rent.rentto.Models;

public class RemindMessageItem {
    public String zip;
    public String itemID;
    public String lender;
    public String borrower;
    public String itemTitle;
    public String itemURL;
    public String reminder;

    public RemindMessageItem(){

    }

    public RemindMessageItem(String zip, String itemID, String lender, String borrower, String reminder) {
        this.zip = zip;
        this.itemID = itemID;
        this.lender = lender;
        this.borrower = borrower;
        this.reminder = reminder;
    }

    public RemindMessageItem(String itemTitle, String lender, String borrower, String itemURL, String reminder, String something){
        this.itemURL = itemURL;
        this.itemTitle = itemTitle;
        this.lender = lender;
        this.borrower = borrower;
        this.reminder = reminder;
    }
}
