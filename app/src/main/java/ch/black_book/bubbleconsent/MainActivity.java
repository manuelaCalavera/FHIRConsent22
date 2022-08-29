package ch.black_book.bubbleconsent;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

import org.hl7.fhir.dstu3.model.Contract;
import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.result.TaskResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ch.black_book.bubbleconsent.LabelReader.OcrCaptureActivity;
import ch.black_book.bubbleconsent.LabelReader.PatientRecord;
import ch.usz.c3pro.c3_pro_android_framework.consent.ViewConsentTaskActivity;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ConsentTaskOptions;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.CreateConsentPDF;

public class MainActivity extends AppCompatActivity {

    public static final String logTag = "MY_LOG";

    private static final int MY_PERMISSION = 1000;
    private static int GET_CONSENT = 1;
    private static int RC_OCR_CAPTURE = 2;
    private static int CHECK_INFO = 3;
    private static int FINAL_SCREEN = 4;
    private ConsentTaskOptions consentTaskOptions;
    private String contractFilePath = "json-appendix/contract.json";
    private String consentPDFContent = "html-appendix/consentPDFcontent.html";
    private String consentReviewContent = "html-appendix/consent.html";
    private String fileFolder = "/BUBBLEConsent";
    private String filePrefix = "Bubble_";
    private PatientRecord patientRecord;

    // screenshot
    private static final int REQUEST_CODE = 974;
    private boolean isrecording = false;

    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;

    private LinearLayout layout_fab1;
    private LinearLayout layout_fab2;
    private LinearLayout layout_fab3;

    private boolean isFABOpen;

    //bubble
    private BubblesManager bubblesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Bubble
        initBubble();
        isrecording = false;

        if(Build.VERSION.SDK_INT>=23){
            if(!Settings.canDrawOverlays(MainActivity.this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent, MY_PERMISSION);
            }
            else{
                Intent intent = new Intent (MainActivity.this, Service.class);
                startService(intent);
            }
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                4568);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        666);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            777);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.FOREGROUND_SERVICE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.FOREGROUND_SERVICE},
                            777);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }

        //create folder if not exist
        File folder = new File(Environment.getExternalStorageDirectory() + fileFolder);
        boolean success = true;
        if (!folder.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = folder.mkdir();
        }


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab_3);

        layout_fab1 = (LinearLayout) findViewById(R.id._layout_fab_1);
        layout_fab2 = (LinearLayout) findViewById(R.id._layout_fab_2);
        layout_fab3 = (LinearLayout) findViewById(R.id._layout_fab_3);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
                addNewBubble();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
                startNewPatient();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
                readLabel();
            }
        });
    }

    private void initBubble() {
        bubblesManager = new BubblesManager.Builder(this)
                .setTrashLayout(R.layout.bubble_trash_layout)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {
                        //addNewBubble();
                    }
                }).build();
        bubblesManager.initialize();;
    }

    private void addNewBubble() {
        BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.bubble_layout, null);
        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble){
                Toast.makeText(MainActivity.this, "Removed", Toast.LENGTH_SHORT).show();
            }
        });

        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                Toast.makeText(MainActivity.this, "Bubble Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton bubble_fab = (FloatingActionButton) bubbleView.findViewById(R.id.bubble_fab);
        bubble_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startNewPatient();
                //take screenshot here
                if(!isrecording){
                    isrecording = true;
                    Toast.makeText(MainActivity.this, "starting projection", Toast.LENGTH_SHORT).show();
                    startProjection();
                }else{
                    isrecording = false;
                    Toast.makeText(MainActivity.this, "stopping projection", Toast.LENGTH_SHORT).show();
                    stopProjection();
                }

                //MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                //startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_RECORD);


            }
        });

        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, 10,200);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
        // Check which request it is that we're responding to
        if (requestCode == GET_CONSENT) {
            if (resultCode == RESULT_OK) {

                // create the PDF
                TaskResult result = (TaskResult) data.getSerializableExtra(ViewConsentTaskActivity.EXTRA_TASK_RESULT);
                createPDF(result);
                finalScreen();
            }
        }
        if (requestCode == RC_OCR_CAPTURE) {
            Log.d(logTag, "BOTH MATCHED: request code matched");
            if (resultCode == RESULT_OK) {
                Log.d(logTag, "BOTH MATCHED: result ok");

                PatientRecord rec = new PatientRecord();

                rec.FirstName = data.getStringExtra("FIRST_NAME");
                rec.LastName = data.getStringExtra("LAST_NAME");
                rec.Code = data.getStringExtra("CODE");
                rec.DateString = data.getStringExtra("DOB");
                rec.DateString = data.getStringExtra("DOB");
                Log.d(logTag, "I received the patient from OCR_CAPTURE!");
                patientRecord = rec;
                Log.d(logTag, patientRecord.FirstName + " " + patientRecord.LastName + " " + patientRecord.DateString + " " + patientRecord.Code);

                checkInfo();
            }
        }
        if (requestCode == CHECK_INFO) {
            if (resultCode == RESULT_OK) {

                PatientRecord rec = new PatientRecord();

                rec.FirstName = data.getStringExtra("FIRST_NAME");
                rec.LastName = data.getStringExtra("LAST_NAME");
                rec.Code = data.getStringExtra("CODE");
                rec.DateString = data.getStringExtra("DOB");
                patientRecord = rec;
                Log.d(logTag, "I checked the patient!");
                Log.d(logTag, patientRecord.FirstName + " " + patientRecord.LastName + " " + patientRecord.DateString + " " + patientRecord.Code);

                startConsent();
            }
        }
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "starting service", Toast.LENGTH_SHORT).show();
                startService(ch.black_book.bubbleconsent.BubbleStart.ScreenCaptureService.getStartIntent(this, resultCode, data));
            }
        }
    }

    private void createPDF(TaskResult result) {
        // create consent pdf

        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String today = formatter.format(now);
        formatter = new SimpleDateFormat("yyyyMMdd");
        String file_date = formatter.format (now);

        String contractString = ResourcePathManager.getResourceAsString(getApplicationContext(), consentPDFContent);
        contractString = contractString.replace("$probenentnahme$", "Testprobe");
        contractString = contractString.replace("$institution$", "USZ");
        contractString = contractString.replace("$nachname$", patientRecord.LastName);
        contractString = contractString.replace("$vorname$", patientRecord.FirstName);
        contractString = contractString.replace("$dob$", patientRecord.DateString);
        contractString = contractString.replace("$code$", patientRecord.Code);
        contractString = contractString.replace("$datum$", today);

        CreateConsentPDF.createPDFfromHTML(this, contractString, result, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + fileFolder + "/" + filePrefix + patientRecord.Code+"_"+file_date+".pdf");
        Toast toast = Toast.makeText(getApplicationContext(),
                "PDF gespeichert!",
                Toast.LENGTH_LONG);

        toast.show();
    }

    private void readLabel() {
        Intent intent = new Intent(this, OcrCaptureActivity.class);
        intent.putExtra(OcrCaptureActivity.AutoFocus, true);
        intent.putExtra(OcrCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_OCR_CAPTURE);
    }

    private void checkInfo() {
        Intent intent = new Intent(this, CheckInfoActivity.class);
        intent.putExtra("FIRST_NAME", patientRecord.FirstName);
        intent.putExtra("LAST_NAME", patientRecord.LastName);
        intent.putExtra("CODE", patientRecord.Code);
        intent.putExtra("DOB", patientRecord.DateString);

        startActivityForResult(intent, CHECK_INFO);
    }

    private void startNewPatient() {
        Intent intent = new Intent(this, CheckInfoActivity.class);
        intent.putExtra("FIRST_NAME", "");
        intent.putExtra("LAST_NAME", "");
        intent.putExtra("CODE", "");
        intent.putExtra("DOB", "01.01.2000");

        startActivityForResult(intent, CHECK_INFO);
    }

    private void startConsent() {
        String contractString = ResourcePathManager.getResourceAsString(getApplicationContext(), contractFilePath);
        //contractString = contractString.replace("$probenentnahme$", probenTyp);
        //contractString = contractString.replace("$institution$", "USZ");
        final Contract contract = Pyro.getFhirContext().newJsonParser().parseResource(Contract.class, contractString);

        consentTaskOptions = new ConsentTaskOptions();
        consentTaskOptions.setRequiresBirthday(false);
        consentTaskOptions.setRequiresName(false);
        consentTaskOptions.setReviewConsentDocument(consentReviewContent);
        consentTaskOptions.setAskForSharing(false);

        Intent intent = ViewConsentTaskActivity.newIntent(getApplicationContext(), contract, consentTaskOptions);

        startActivityForResult(intent, GET_CONSENT);
    }

    private void finalScreen() {
        Intent intent = new Intent(this, FinalActivity.class);
        startActivityForResult(intent, FINAL_SCREEN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 666: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 777: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showFABMenu() {
        isFABOpen = true;

        Animation showButton = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_button);
        fab.startAnimation(showButton);

        Animation showLayout1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_layout1);
        Animation showLayout2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_layout2);
        Animation showLayout3 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_layout3);
        layout_fab1.startAnimation(showLayout1);
        layout_fab2.startAnimation(showLayout2);
        layout_fab3.startAnimation(showLayout3);
        layout_fab1.setVisibility(View.VISIBLE);
        layout_fab2.setVisibility(View.VISIBLE);
        layout_fab3.setVisibility(View.VISIBLE);

    }

    private void closeFABMenu() {
        isFABOpen = false;

        Animation hideButton = AnimationUtils.loadAnimation(MainActivity.this, R.anim.hide_button);
        fab.startAnimation(hideButton);

        Animation hideLayout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.hide_layout);
        layout_fab1.startAnimation(hideLayout);
        layout_fab2.startAnimation(hideLayout);
        layout_fab3.startAnimation(hideLayout);
        layout_fab1.setVisibility(View.GONE);
        layout_fab2.setVisibility(View.GONE);
        layout_fab3.setVisibility(View.GONE);
    }

    protected void onDestroy(){
        super.onDestroy();
        bubblesManager.recycle();
    }

    private void startProjection() {
        Toast.makeText(MainActivity.this, "start Projection", Toast.LENGTH_SHORT).show();
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        Toast.makeText(MainActivity.this, "stop projection", Toast.LENGTH_SHORT).show();
        startService(ch.black_book.bubbleconsent.BubbleStart.ScreenCaptureService.getStopIntent(this));
    }
}
