


package com.example.guma.beatmaker;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileDialog
{
    //////////////// FileDialog class member start.
    public static final String TAG = "FileDialog";

    public static final int MODE_OPEN = 0;
    public static final int MODE_SAVE = 1;
    public static final int MODE_SELECT = 2;
    public static final int MODE_FOLDER = 3;

    public static final int NOTIFY_CANCELED = 0;
    public static final int NOTIFY_OPEN_FAILED = 1;
    public static final int NOTIFY_FILE_EXCEPTION = 2;
    public static final int NOTIFY_ERROR = 3;

    public static final int KEY_TYPE = 0;
    public static final int KEY_STATE = 1;
    public static final int VALUE_TYPE_FILE = 0;
    public static final int VALUE_TYPE_FOLDER = 1;
    public static final int VALUE_STATE_NORMAL = 0;
    public static final int VALUE_STATE_READONLY = 1;
    public static final int VALUE_STATE_HIDDEN = 2;

    private final String OPEN_STRING = "Open";
    private final String SAVE_STRING = "Save";
    private final String SELECT_STRING = "Select";
    private final String FOLDER_STRING = "Select folder";

    private Context mContext = null;
    private Builder dialog = null;
    private FileDialogLayout fileDialogLayout = null;
    private String currentPath = null;
    private String previousPath = null;
    private String mInputFileName = null;
    private int mMode = 0;
    private DialogInterface.OnClickListener okClickListener = null;
    private DialogInterface.OnClickListener cancelClickListener = null;
    private OnFileDialogListener fileDialogListener = null;

    public FileDialog(Context context, int mode, String path){
        mContext = context;
        mMode = mode;
        currentPath = path;

        if((currentPath == null) || (currentPath.trim().length() < 1))
            currentPath = "/";

        initDialog();
    }

    public FileDialog(Context context, int mode, String path, OnFileDialogListener listener){
        mContext = context;
        mMode = mode;
        currentPath = path;
        fileDialogListener = listener;

        if((currentPath == null) || (currentPath.trim().length() < 1))
            currentPath = "/";

        initDialog();
    }

    public void setListener(OnFileDialogListener listener){
        fileDialogListener = listener;
    }

    private void initDialog(){
        fileDialogLayout = new FileDialogLayout(mContext);
        dialog = new Builder(mContext);
        dialog.setTitle(getModeString());
        dialog.setView(fileDialogLayout);
        

        okClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(fileDialogListener != null){
                    switch(mMode){
                        case MODE_OPEN:
                            mInputFileName = fileDialogLayout.getInputFileName();
                            fileDialogListener.onSelected(currentPath, mInputFileName);
                            break;
                        case MODE_SAVE:
                            mInputFileName = fileDialogLayout.getInputFileName();
                            fileDialogListener.onSelected(currentPath, mInputFileName);
                            break;
                        case MODE_SELECT:
                            fileDialogListener.onSelectedFiles(currentPath, fileDialogLayout.getSelectedFiles());
                            break;
                        case MODE_FOLDER:
                            fileDialogListener.onSelected(currentPath, null);
                            break;
                    }
                }
            }
        };

        cancelClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(fileDialogListener != null)
                    fileDialogListener.OnNotify(NOTIFY_CANCELED);
            }
        };

        if(mMode == MODE_SAVE)
            dialog.setPositiveButton("Save", okClickListener);
        else
            dialog.setPositiveButton("Ok", okClickListener);

        dialog.setNegativeButton("Cancel", cancelClickListener);

    }
    
    public void show(){
        if(dialog != null){
            dialog.show();
        }
    }

    private String getModeString(){
        switch(mMode){
            case MODE_OPEN:
                return OPEN_STRING;
            case MODE_SAVE:
                return SAVE_STRING;
            case MODE_SELECT:
                return SELECT_STRING;
            case MODE_FOLDER:
                return FOLDER_STRING;
            default:
                return "Ocsoosoo File Dialog";
        }
    }

    public int getMode(){
        return mMode;
    }

    public String getCurrentPath(){
        return currentPath;
    }
    
    private void sendNotify(int nityType){
        if(fileDialogListener != null)
            fileDialogListener.OnNotify(nityType);
    }

    //////////////// FileDialog class member end.


    //////////////// Please implement to this Listeners on Activity used FileDialog.
    public interface OnFileDialogListener{
        public void onSelected(String path, String fileName); //FileDialog will returned selected single file's path and name.
        public void onSelectedFiles(String path, ArrayList<String> selectedFiles); //FileDialog will returned multiple selected files's path and names.
        public void OnNotify(int notiType); //FileDialog will returned notyfication's value.
    }
    //////////////// Listener end.

    //////////////// FileInfo class start.
    public class FileInfo{
        private String name=null;
        private boolean isDirectory=false;
        private int fileState=VALUE_STATE_NORMAL;
        public FileInfo(String fileName, boolean isDir, int state){
            name = fileName;
            isDirectory = isDir;
            fileState = state;
        }
        public String getName(){
            return name;
        }
        public boolean isDirectory(){
            return isDirectory;
        }
        public int getState(){
            return fileState;
        }
    }
    //////////////// FileInfo class end.

    //////////////// FileItem class start.
    public class FileItem extends LinearLayout{
        private CheckBox cb = null;
        private Drawable icon = null;
        private ImageView iv = null;
        private TextView tv = null;
        private boolean isShowCB = false;
        private Context mContext=null;
        private FileInfo fileInfo=null;

        public FileItem(Context context, FileInfo fInfo){
            super(context);
            mContext = context;
            fileInfo = fInfo;

            if(getMode() == MODE_SELECT)
                isShowCB = true;
            else
                isShowCB = false;
            initLayout();
        }

        private void initLayout(){
            setOrientation(LinearLayout.HORIZONTAL);

            cb = new CheckBox(mContext);
            cb.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, //width 
                    ViewGroup.LayoutParams.WRAP_CONTENT, //height
                    0.0F
                ));
            if(isShowCB && !fileInfo.isDirectory())
                cb.setVisibility(VISIBLE);
            else
                cb.setVisibility(GONE);
            cb.setFocusable(false);
            cb.setClickable(false);
            addView(cb);

            tv = new TextView(mContext);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    ViewGroup.LayoutParams.FILL_PARENT, //height
                    0.0F
                ));
            tv.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            tv.setTextSize(18);
            tv.setText(fileInfo.getName());
            tv.setPadding(10,16,10,16);
            tv.setFocusable(false);
            tv.setClickable(false);
 
            addView(tv);
        }

        public Drawable getIcon(){
            return icon;
        }

        public void setIcon(Drawable ico){
            icon = ico;
        }

        public void hideIcon(){
            icon.setVisible(false,  false);
            iv.setVisibility(GONE);
        }
        public void showIcon(){
            icon.setVisible(true,  false);
            iv.setVisibility(VISIBLE);
        }

        public void hideCheckbox(){
            cb.setVisibility(GONE);
        }
        public void showCheckbox(){
            cb.setVisibility(VISIBLE);
        }
        public boolean isChecked(){
            if(cb == null)
                Log.e(TAG,"isChecked() cb is null.");
            return cb.isChecked();
        }
        public void setChecked(boolean check){
            Log.d(TAG, "setChecked() - check="+check);
            cb.setChecked(check);
            cb.invalidate();
        }
        public boolean isDirectory(){
            return fileInfo.isDirectory();
        }
        public String getName(){
            return fileInfo.getName();
        }
        
    }
    //////////////// FileItem class end.
    
    //////////////// FileListAdapter class start.
    public class FileListAdapter extends ArrayAdapter{
        private Context mContext = null;
        private ArrayList<FileInfo> mFileInfoList = null;
        private ArrayList<FileItem> mItemList = null;

        public FileListAdapter(Context context, int textViewResourceId, ArrayList<FileInfo> objects){
            super(context, textViewResourceId, objects);
            mContext = context;
            mFileInfoList = objects;
            createFileItemList();
        }

        private void createFileItemList(){
            mItemList = new ArrayList<FileItem>();
            for(int i=0; i<mFileInfoList.size(); i++){
                FileItem item = new FileItem(mContext, mFileInfoList.get(i));
                item.setFocusable(false); //set false. ListView's OnItemClickListener is called when focusable view are not include in ListView. 
                item.setClickable(false); //set false. ListView's OnItemClickListener is called when focusable view are not include in ListView.
                mItemList.add(item);
            }
        }

        public int getItemCount(){
            return mItemList.size();
        }

        public FileItem getItem(int position){
            if(position < mItemList.size())
                return mItemList.get(position);
            return null;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            //Log.d(TAG, "getView() - position ="+position+", mItemList size="+ mItemList.size());
            return mItemList.get(position);
        }
    } //FileListAdapter class
    //////////////// FileListAdapter class end.

    //////////////// FileListView class start.
    public class FileListView extends ListView implements OnItemClickListener{
        private ArrayList<String> totalList = null;
        private ArrayList<String> folderList = null;
        private ArrayList<String> fileList = null;
        private ArrayList<FileInfo> fileInfoList=null;
        private FileListAdapter fileListAdapter = null;
        private static final String UP_FOLDER="[ .. ]";
        private static final String FOLDER_TAG_L="[";
        private static final String FOLDER_TAG_R="]";
        

        public FileListView(Context context){
            super(context);
            initList();
            UpdateFileList(currentPath);
        }

        private void initList(){

            if(totalList==null)
                totalList = new ArrayList<String>();
            if(folderList==null)
                folderList = new ArrayList<String>();
            if(fileList==null)
                fileList = new ArrayList<String>();

            setOnItemClickListener(this);
        }
        
        private boolean UpdateFileList(String path){
            previousPath = currentPath;
            currentPath = path;
            Log.d(TAG, "UpdateFileList() - path="+path);

            if(LoadingFileList()){
                    fileInfoList = new ArrayList<FileInfo>();
                    for(int i=0; i<folderList.size(); i++){
                        FileInfo fi = new FileInfo(folderList.get(i), true, VALUE_STATE_NORMAL);
                        fileInfoList.add(fi);
                    }

                    if(getMode() != MODE_FOLDER){
                        for(int i=0; i<fileList.size(); i++){
                            FileInfo fi = new FileInfo(fileList.get(i), false, VALUE_STATE_NORMAL);
                            fileInfoList.add(fi);
                        }
                    }
                    fileListAdapter = new FileListAdapter(mContext, android.R.layout.simple_list_item_1, fileInfoList);
                    setAdapter(fileListAdapter);
                    if(fileDialogLayout != null){
                        fileDialogLayout.updatePathLayout();
                        fileDialogLayout.setSelectedFileName("");
                    }
                    Log.d(TAG, "UpdateFileList() - succeed.");
                return true;
            }else{
                Log.e(TAG, "UpdateFileList() - failed.");
                currentPath = previousPath;
                Toast.makeText(mContext, path+" folder can not open.", Toast.LENGTH_SHORT).show();
                sendNotify(NOTIFY_OPEN_FAILED);
                return false;
            }
        }

        private boolean LoadingFileList(){
            if((currentPath == null) || (currentPath.trim().length() < 1)){
                Log.e(TAG, "LoadingFileList() - currentPath is null or wrong.");
                return false;
            }

            Log.d(TAG, "LoadingFileList() - currentPath="+currentPath);

            File file = new File(currentPath);
            File[] arrfiles = file.listFiles();

            if((arrfiles == null) || (arrfiles.length < 1)){
                Log.e(TAG, "LoadingFileList() - arrfiles is null or empty.");
                return false;
            }

            totalList.clear();
            folderList.clear();
            fileList.clear();

            if(!currentPath.equalsIgnoreCase("/")){
                folderList.add(UP_FOLDER);
            }

            for (int i=0; i<arrfiles.length; i++){
                if (arrfiles[i].isDirectory()){
                    folderList.add(FOLDER_TAG_L + arrfiles[i].getName() + FOLDER_TAG_R);
                } else {
                    fileList.add(arrfiles[i].getName());
                }
            }

            Collections.sort(folderList);
            Collections.sort(fileList);
            totalList.addAll(folderList);
            totalList.addAll(fileList);
            
            Log.d(TAG, "LoadingFileList() - succeed.");
            return true;
        }

        public String getCurrentPath(){
            return currentPath;
        }

        public int getCount(){
            if(totalList == null)
                return 0;

            return totalList.size();
        }

        public ArrayList<String> getSelectedFiles(){
            ArrayList<String> selFiles = new ArrayList<String>();
            for(int i=0; i<fileListAdapter.getItemCount(); i++){
                FileItem fi = (FileItem) fileListAdapter.getItem(i);
                if((fi != null) && (!fi.isDirectory())){
                    if(fi.isChecked()){
                        selFiles.add(fi.getName());
                    }
                }
            }
            return selFiles;
        }

        public String getSelectedFile(){
            String selFile = null;
            for(int i=0; i<fileListAdapter.getItemCount(); i++){
                FileItem fi = (FileItem) fileListAdapter.getItem(i);
                if((fi != null) && (!fi.isDirectory())){
                    if(fi.isChecked()){
                        selFile = fi.getName();
                    }
                }
            }
            return selFile;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            Log.d(TAG, "onItemClick() - position="+position);
            if(position < folderList.size()){ //move to selected folder
                String targetFolder = null;
                String selFolder = folderList.get(position);
                int len = selFolder.length();

                if(currentPath.equalsIgnoreCase("/")){ //move to child folder.
                    targetFolder = currentPath+selFolder.substring(FOLDER_TAG_L.length(), len-FOLDER_TAG_R.length())+"/";
                }else{
                    if(position == 0){ //move to parent folder
                        targetFolder = currentPath;
                        String currPath = currentPath;
                        String []path = currPath.split("/");
                        targetFolder = "/";

                        for(int i=0; i<path.length-1; i++){
                            if(path[i] == null)
                                continue;
                            if(path[i].trim().length() > 0)
                                targetFolder += path[i]+"/";
                        }
                    }else{ //move to child folder
                        targetFolder = currentPath;

                        if(targetFolder.charAt(targetFolder.length()-1) != '/'){
                            targetFolder += "/";
                        }
                        targetFolder = targetFolder+selFolder.substring(FOLDER_TAG_L.length(), len-FOLDER_TAG_R.length())+"/";
                    }
                }

                if(targetFolder.charAt(targetFolder.length()-1) != '/')
                    targetFolder += "/";
                UpdateFileList(targetFolder);
                return;
            }
            else if(getMode() == MODE_SELECT){
                //FileItem fi = (FileItem)getItemAtPosition(position);
                FileItem fi = (FileItem) fileListAdapter.getItem(position);
                if(fi != null){
                    fi.setChecked(!fi.isChecked());
                    fileListAdapter.notifyDataSetChanged();
                }
            }
            else if(getMode() == MODE_OPEN){
                //String fileName = getItemAtPosition(position).toString();
                String fileName = totalList.get(position);
                if(fileDialogLayout != null)
                    fileDialogLayout.setSelectedFileName(fileName);
            }
            else if(getMode() == MODE_FOLDER){
            }
        }
    }//FileListView class
    //////////////// FileListView class end.



    //////////////// FileUtils class start.
    public class FileUtils{
        public FileUtils(){
            
        }
    } //FileUtils class
    //////////////// FileUtils class end.



    //////////////// FileDialogLayout class start.
    public class FileDialogLayout extends LinearLayout{
        private HorizontalScrollView scv = null;
        private LinearLayout topLayout = null;
        private LinearLayout fileLayout = null;
        private LinearLayout space = null;
        private TextView tv_locale = null;
        private FileListView filelistView= null;
        private EditText et_inputFileName = null;
        private OnClickListener pathChangeListener = null;
        private final int PATH_BUTTON_ID_BASE = 0x70000000;
        private String []pathButtonString = null;

        public FileDialogLayout(Context context){
            super(context);
            initLayout();
        }

        public FileDialogLayout(Context context, AttributeSet attrs){
            super(context, attrs);
            initLayout();
        }

        public void setAttribute(AttributeSet att){
        }

        private void initLayout(){
            Log.d(TAG, "initLayout()");
            setOrientation(LinearLayout.VERTICAL);
            setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    ViewGroup.LayoutParams.FILL_PARENT,  //height
                    0.0F
                ));

            space = new LinearLayout(mContext);
            space.setOrientation(LinearLayout.HORIZONTAL);
            space.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, //width 
                        2,  //height
                        0.0F
                    ));
            space.setBackgroundColor(Color.parseColor("#ffffff"));

            crateTopLayout();
            addView(space);
            crateFileLayout();
            crateBottomLayout();

            updatePathLayout();
            setFocusable(true);
            setFocusableInTouchMode(true);

            pathChangeListener = new OnClickListener(){ //Path change.
                @Override
                public void onClick(View v){
                    int selIndex = v.getId()-PATH_BUTTON_ID_BASE;
                    if(filelistView.UpdateFileList(getSelectedRealPath(selIndex)))
                        updatePathLayout();
                }
            };
        }

        private void crateTopLayout(){
            Log.d(TAG, "crateTopLayout()");
            scv = new HorizontalScrollView(mContext);
            scv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    70, //ViewGroup.LayoutParams.WRAP_CONTENT,  //height
                    0.0F
                ));
            topLayout = new LinearLayout(mContext);
            topLayout.setOrientation(LinearLayout.HORIZONTAL);
            topLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, //width 
                        ViewGroup.LayoutParams.FILL_PARENT, //height
                        0.0F
                    ));
            topLayout.setHorizontalScrollBarEnabled(true);
            topLayout.setBackgroundColor(Color.parseColor("#101010"));
            scv.addView(topLayout);
            addView(scv);
/*
            tv_locale = new TextView(mContext);
            tv_locale.setText("Location : ");
            tv_locale.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, //width 
                    ViewGroup.LayoutParams.WRAP_CONTENT, //height
                    0.0F
                ));
            topLayout.addView(tv_locale);
*/
        }

        private void crateFileLayout(){
            Log.d(TAG, "crateFileLayout()");
            fileLayout = new LinearLayout(mContext);
            fileLayout.setOrientation(LinearLayout.HORIZONTAL);
            fileLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    400,  //height
                    0.0F
                ));

            filelistView = new FileListView(mContext);
            filelistView.setItemsCanFocus(false);
            if(getMode() == MODE_SELECT){
                filelistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            filelistView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    400,  //height
                    0.0F
                ));

            fileLayout.addView(filelistView);
            addView(fileLayout);
        }

        private void crateBottomLayout(){
            Log.d(TAG, "crateBottomLayout()");
            et_inputFileName = new EditText(mContext);
            et_inputFileName.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, //width 
                    70, //height
                    0.0F
                ));
            if(getMode() == MODE_SAVE){
                et_inputFileName.setEnabled(true);
                et_inputFileName.setClickable(true);
                et_inputFileName.setFocusable(true);            
            }else{
                et_inputFileName.setEnabled(true);
                et_inputFileName.setClickable(false);
                et_inputFileName.setFocusable(false);            
            }
            addView(et_inputFileName);
        }

        private void makePathButtonLayout(View view){
            view.setLayoutParams(
                    new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, //width
                        ViewGroup.LayoutParams.WRAP_CONTENT, //height
                        0.0F
                        ));
        }

        protected void updatePathLayout(){
            Log.d(TAG, "updatePathLayout()");
            String currPath = filelistView.getCurrentPath();
            pathButtonString = currPath.split("/");
            Log.d(TAG, "updatePathLayout() - currPath="+currPath);

            topLayout.removeAllViews();
            //topLayout.addView(tv_locale);

            Button b = new Button(mContext);
            b.setText("/");
            makePathButtonLayout(b);
            b.setOnClickListener(pathChangeListener);
            topLayout.addView(b);

            if(currPath.trim().length() < 1){
                return;
            }else{
                for(int i=0; i<pathButtonString.length; i++){
                    Log.d(TAG,"updatePathLayout() path="+pathButtonString[i]);
                    if((pathButtonString[i] == null) || (pathButtonString[i].trim().length() < 1))
                        continue;
                    Button bv = new Button(mContext);
                    bv.setText(pathButtonString[i]);
                    bv.setGravity(Gravity.CENTER);
                    makePathButtonLayout(bv);
                    bv.setOnClickListener(pathChangeListener);
                    bv.setId(PATH_BUTTON_ID_BASE+i);
                    topLayout.addView(bv);
                }
            }
        }

        private String getSelectedRealPath(int index){
            if(index >= pathButtonString.length)
                return "/";
            if(index == 0)
                return "/";

            String path="/";

            for(int i=1; i<=index; i++){
                if(path.charAt(path.length()-1) != '/')
                    path += "/";
                path += pathButtonString[i];
            }
            Log.d(TAG, "getSelectedRealPath()-index="+index+", path="+path);

            return path;
        }

        public ArrayList<String> getSelectedFiles(){
            return filelistView.getSelectedFiles();
        }

        public String getSelectedFile(){
            return filelistView.getSelectedFile();
        }
        public void setSelectedFileName(String name){
            et_inputFileName.setText(name);
        }
        public String getInputFileName(){
            return et_inputFileName.getText().toString();
        }
    } //FileDialogLayout class
    //////////////// FileDialogLayout class end.

    
} //FileDialog class
//////////////// FileDialog class end.

