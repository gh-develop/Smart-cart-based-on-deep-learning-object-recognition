package com.example.JustCart_ver4;

public class PersonalData {

    private String Name;
    private String Price;
    private String Desc;
    private String Location;
    private String Image;
    //private Bitmap bmImg;

    public String getName() {
        return Name;
    }

    public String getPrice() {
        return Price;
    }

    public String getDesc() {
        return Desc;
    }

    public String getLocation() {
        return Location;
    }

    public String getImage() {
        return Image;
    }

    /*public Bitmap getImageView() {
        try{
            URL url = new URL(getImage());
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();

            bmImg = BitmapFactory.decodeStream(is);


        }catch(IOException e){
            e.printStackTrace();
        }
        return bmImg;
    }*/

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setPrice(String Price) {
        this.Price = Price;
    }

    public void setDesc(String Desc) {
        this.Desc = Desc;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    /*public void setImageView(Bitmap bmImg) {
        this.bmImg = bmImg;
    }*/

}
