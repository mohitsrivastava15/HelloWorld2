package item;

import android.graphics.Bitmap;

/**
 * Created by mohit on 8/9/16.
 */
public class QuickSearchItem {
    private String txtGrain;
    private Bitmap imgGrain;

    public QuickSearchItem(String txtGrain, Bitmap imgGrain) {
        this.txtGrain = txtGrain;
        this.imgGrain = imgGrain;
    }

    public String getTxtGrain() {
        return txtGrain;
    }

    public void setTxtGrain(String txtGrain) {
        this.txtGrain = txtGrain;
    }

    public Bitmap getImgGrain() {
        return imgGrain;
    }

    public void setImgGrain(Bitmap imgGrain) {
        this.imgGrain = imgGrain;
    }
}
