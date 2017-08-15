package wycliffe.com.imager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    String userChoosenTask;
    ImageView ivImage;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Button btnLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoader = (Button) findViewById(R.id.btnSelectPhoto);
        btnLoader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage();
            }
        });

        ivImage = (ImageView) findViewById(R.id.ivImage);
    }//end onCreate.


    //2) Selecting the image, this is even better ============================================================================================
    //dialog box with the three options
    private void selectImage(){

        final CharSequence[] items = {"Take Photo" ,"Choose from Library",
                "Cancel" };

        //Building the dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
               boolean result=Utility.checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }

        });
        builder.show();

    } // end SelectImage


    //Check for CAMERA Request.
    private void cameraIntent()
    {
        //We are calling Implicit Intent to open the camera application on user's phone.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    //Check For Gallery Request
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*"); //select only images from the media storage.
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);

//        if you want to get images as well as videos, you can use following code
//        intent.setType("image/* video/*");
    }


    //   GRANT PERMISSION AT RUNTIME.
//    If permission has not been granted, then checkPermssion() method will display a dialog to user with two options.
//    Allow the permission request or
//    Deny the permission request
//    onRequestPermissionsResult() is inbuilt method which receives a callback of this dialog action for the particular
//     activity from which checkPermssion() has been called
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
        case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if permission has been granted, then I am calling method for specific Intent according to value of userChoosenTask variable.
                if(userChoosenTask.equals("Take Photo"))
                    cameraIntent();
                else if(userChoosenTask.equals("Choose from Library"))
                    galleryIntent();
            }

            else {
                //code for deny
            }
            break;
    }
}

//===========================================================================================================================================

//3) Now Handling the obtained image ===========================================================================================================================


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

  // If it is from gallery
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ivImage.setImageBitmap(bm);
    }

   // If it is from camera capture
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        // storing the file we have created into External Storage.==
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //===========

        // Setting the image to the view
        ivImage.setImageBitmap(thumbnail);
    }
}// End Class


