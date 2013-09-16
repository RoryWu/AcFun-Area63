package tv.acfun.a63;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import tv.acfun.a63.api.entity.User;
import tv.acfun.a63.db.DB;
import tv.acfun.a63.util.ActionBarUtil;
import tv.acfun.a63.util.MemberUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.alibaba.fastjson.JSONException;

public class SigninActivity extends SherlockActivity {
    public static final int REQUEST_SIGN_IN = 1;
    private static final String TAG = "SigninActivity";
    private EditText mNameView;
    private EditText mPwdView;

    public static Intent createIntent(Context pkgContext) {
        return new Intent(pkgContext, SigninActivity.class);
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().setTitle(R.string.signin);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(),false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        mNameView = (EditText) findViewById(R.id.user_name);
        
        mPwdView = (EditText) findViewById(R.id.password);
        final View btn = findViewById(R.id.btn_signin);
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(mNameView.getText().length() == 0 || mPwdView.getText().length() == 0){
                    mNameView.setError("用户名和密码不能为空！");
                }else
                    new LoginTask().execute();
//                setResult(RESULT_OK);
//                finish();
            }
        });
        mPwdView.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    btn.performClick();
                    return true;
                }
                return false;
            }
        });
        
    }
    private class LoginTask extends AsyncTask<Void, Void, Boolean>{
        private String response;
        private User user;
        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            
            dialog = new ProgressDialog(SigninActivity.this);
            dialog.setMessage("登录...");
            dialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            
            try {
                HashMap<String, Object> map = MemberUtils.login(mNameView.getText().toString(), mPwdView.getText().toString());
                if((Boolean)map.get("success")){
                    user = (User) map.get("user");
                    new DB(getApplicationContext()).saveUser(user);
                    return true;
                }else{
                    response ="错误: "+map.get("result").toString();
                }
            } catch (HttpException e) {
                e.printStackTrace();
                response = "错误: 请检查网络连接";
            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "错误: 请检查网络是否通畅";
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                response = "错误: 登录失败";
            } 
            
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result.booleanValue()){
                Intent data = new Intent();
                data.putExtra("user", user);
                setResult(RESULT_OK, data);
                finish();
            }else{
                mNameView.setError(response);
                AcApp.showToast(response);
            }
            dialog.dismiss();
        }
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
