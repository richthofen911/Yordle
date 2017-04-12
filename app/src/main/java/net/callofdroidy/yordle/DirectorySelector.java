package net.callofdroidy.yordle;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yli on 12/04/17.
 */

public class DirectorySelector {
    private String sdcardDirectory = "";
    private Context context;
    private TextView titleView;

    private String mDir = "";
    private List<String> mSubDirs = null;
    private DirectorySelectCallback mDirectorySelectCallback = null;
    private ArrayAdapter<String> mListAdapter = null;


    public interface DirectorySelectCallback {
        void onDirSelected(String chosenDir);
    }

    public DirectorySelector(Context context, DirectorySelectCallback directorySelectCallback) {
        this.context = context;
        sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        mDirectorySelectCallback = directorySelectCallback;

        try {
            sdcardDirectory = new File(sdcardDirectory).getCanonicalPath();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // chooseDirectory() - load directory chooser dialog for initial default sdcard directory
    public void chooseDirectory() {
        // Initial directory is sdcard directory
        chooseDirectory(sdcardDirectory);
    }

    // chooseDirectory(String dir) - load directory chooser dialog for initial input 'dir' directory
    public void chooseDirectory(String dir) {
        File dirFile = new File(dir);
        if (! dirFile.exists() || ! dirFile.isDirectory())
            dir = sdcardDirectory;

        try {
            dir = new File(dir).getCanonicalPath();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        mDir = dir;
        mSubDirs = getDirectories(dir);

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, mSubDirs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Navigate into the sub-directory
                mDir += "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(which);
                updateDirectory();
            }
        });

        final AlertDialog selectDirDialog = dialogBuilder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Current directory chosen
                                if (mDirectorySelectCallback != null) {
                                    // Call registered listener supplied with the chosen directory
                                    mDirectorySelectCallback.onDirSelected(mDir);
                                }
                            }
                        })
                        .setNeutralButton("New Folder", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText input = new EditText(context);

                                // Show new folder name input dialog
                                new AlertDialog.Builder(context)
                                        .setTitle("New folder name")
                                        .setView(input)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Editable newDir = input.getText();
                                                String newDirName = newDir.toString();
                                                // Create new directory
                                                if ( createSubDir(mDir + "/" + newDirName) ) {
                                                    // Navigate into the new directory
                                                    mDir += "/" + newDirName;
                                                    updateDirectory();
                                                }
                                                else
                                                    Toast.makeText(context, "Failed to create '" + newDirName + "' folder", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

        selectDirDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Back button pressed
                    selectDirDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        // Show directory chooser dialog
        selectDirDialog.show();
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists() )
            return newDirFile.mkdir();
        return false;
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();

        try {
            File dirFile = new File(dir);
            if (! dirFile.exists() || ! dirFile.isDirectory())
                return dirs;

            for (File file : dirFile.listFiles()) {
                if ( file.isDirectory() )
                    dirs.add( file.getName() );
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        titleView = new TextView(context);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        titleView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault);
        titleView.setTextColor(context.getResources().getColor(android.R.color.white));
        titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        titleView.setText(title);

        Button newDirButton = new Button(context);
        newDirButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        newDirButton.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        newDirButton.setText("./Upper Level");
        newDirButton.setGravity(Gravity.LEFT);
        newDirButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if ( mDir.equals(sdcardDirectory) ) {
                    // The very top level directory, do nothing
                    Toast.makeText(context, "This is the top level", Toast.LENGTH_SHORT).show();
                } else {
                    // Navigate back to an upper directory
                    mDir = new File(mDir).getParent();
                    updateDirectory();
                }
            }
        });

        titleLayout.addView(titleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        mListAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(mListAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }

    private void updateDirectory() {
        mSubDirs.clear();
        mSubDirs.addAll(getDirectories(mDir));
        titleView.setText(mDir);

        mListAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @Override
            public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}
