package com.example.finalserver.fragment;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalserver.MainMenuActivity;
import com.example.finalserver.R;
import com.example.finalserver.network.NfcActivity;
import com.example.finalserver.network.PreferenceHelper;
import com.example.finalserver.network.RetrofitClient;
import com.example.finalserver.network.ServiceEntList;
import com.example.finalserver.network.ServiceLoginApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewListFragment extends Fragment {
    private EditText showUserName;
    private PreferenceHelper preferenceHelper;
    private ServiceEntList serviceEnt;
    ListView list;
    String[][] item_list;
    CustomList adapter;


    public UserViewListFragment(){};                  //UserViewListFragment 생성자

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceHelper = new PreferenceHelper(getActivity());

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) { //뒤로 가기 방지
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_list_view, container, false);  //동적 인플레이트 생성

        MainMenuActivity mainMenuActivity = (MainMenuActivity) getActivity();
        CustomList adapter = new CustomList(mainMenuActivity);
        list = view.findViewById(R.id.listView);
        list.setAdapter(adapter);

        downloadFromServerData();
        showUserName = (EditText) view.findViewById(R.id.showUserName); //이름에 데이터 넣기


        //showUserName.setText();


        return view;
    }


    private void downloadFromServerData(){
        final String str_userEmail = preferenceHelper.getEmail();
        Toast.makeText(getContext(), str_userEmail, Toast.LENGTH_SHORT).show();
        serviceEnt = RetrofitClient.loginConfig().create(ServiceEntList.class); //ServiceLoginApi 인터페이스 구현
        //호출하여 서버에 요청하고 받을 수 있도록 인스턴스를 생성함
        Call<String> calls = serviceEnt.request_onlyUser_list(str_userEmail); //ServiceLoginApi 인터페이스를 호출하여 이 인터페이스의 userLogin 메소드를 호출

        calls.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
               // String jsonTagResult = response.body();
                Log.e("UserViewListFragment", "response성공");
                try {
                   parseUserTag(response.body());
                    Log.e("tttt", response.body());
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> calls, Throwable t) {
                Toast.makeText(getContext(), "에러 발생 404", Toast.LENGTH_SHORT).show();
                Log.e("통신 불가", t.getMessage());

            }
        });
    }


    private void parseUserTag(String response) throws JSONException{
        try{
            JSONObject jsonObject = new JSONObject(response);   //JSON object 객체 생성
            Log.d("parseUserTagDatas", "Flow Check");

            if(jsonObject.getString("message").equals("found")) {  //성공일 시
               showUserName.setText(jsonObject.getString("userNameData")); //userName 출력
                Log.d("parseUserTag", "ccccc");
                Log.d("pase!!!!!", jsonObject.toString());

                JSONArray dataArray = jsonObject.getJSONArray("data");
                for(int i = 0; i < dataArray.length(); i++){
                    JSONObject dataObj = dataArray.getJSONObject(i);  //장소 이름, 시간, 입실 퇴실 여부를 뽑아냄(한 데이터 튜플을 하나씩 파싱)
                    Log.d("read", "error??");
                    item_list[i][0] = dataObj.optString("placeName");
                    item_list[i][1] = dataObj.optString("current_time");
                    item_list[i][2] = dataObj.optString("status_exists");
                }

            }else if(jsonObject.getString("message").equals("UID NODATA")){
                Toast.makeText(getActivity(), "UID에 해당되는 데이터가 없습니다", Toast.LENGTH_SHORT).show();
            }else if(jsonObject.getString("message").equals("NOUSER")){
                Toast.makeText(getActivity(), "저장된 유저 정보가 없습니다", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "parseUserTagDatas", Toast.LENGTH_SHORT).show();
                Log.d("parseUserTagDatas", "error");
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }





    public class CustomList extends ArrayAdapter<String>{
        private Activity context;

        public CustomList(Activity context){
            super(context, R.layout.user_list_item);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.user_list_item, null, true);
            TextView placeName = (TextView) rowView.findViewById(R.id.textPlaceName);
            TextView time = (TextView) rowView.findViewById(R.id.textTime);
            TextView exists = (TextView) rowView.findViewById(R.id.textExists);
            placeName.setText(item_list[position][0]);
            time.setText(item_list[position][1]);
            exists.setText(item_list[position][2]);

            return rowView;
        }
    }




}
