package com.hui.tallybox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton; // 【重要】确保导入这个类
import com.hui.tallybox.adapter.AccountAdapter;
import com.hui.tallybox.db.AccountBean;
import com.hui.tallybox.db.DBManager;
import com.hui.tallybox.utils.BudgeDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ListView todayLV;
    ImageView searchIv;
    Button editBtn;
    ImageButton moreBtn;
    FloatingActionButton aiFab; // 【修改】将AI按钮声明为成员变量

    List<AccountBean> mData;
    AccountAdapter adapter;

    int year, month, day;

    View headView;
    TextView topOutTv, topInTv, topemoneyTv, topAllTv;
    ImageView budgetIv;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ 初始化数据库
        DBManager.initDB(this);

        // 初始化时间
        initTime();

        // 初始化控件
        initView();

        // SharedPreferences
        sharedPreferences = getSharedPreferences("budget", Context.MODE_PRIVATE);

        // 添加头布局（必须在 ListView 设置适配器前）
        addLVHead();

        // 初始化数据与适配器
        mData = new ArrayList<>();
        adapter = new AccountAdapter(this, mData);
        todayLV.setAdapter(adapter);

        // 设置 ListView 长按事件
        setLVLongClickListener();
    }

    private void initView() {
        todayLV = findViewById(R.id.main_lv);
        editBtn = findViewById(R.id.main_btn_edit);
        moreBtn = findViewById(R.id.main_bt_more);
        searchIv = findViewById(R.id.mian_iv_box);
        aiFab = findViewById(R.id.main_fab_ai); // 【新增】找到新的悬浮按钮

        editBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        searchIv.setOnClickListener(this);
        aiFab.setOnClickListener(this); // 【新增】为新的悬浮按钮设置监听
    }

    private void addLVHead() {
        headView = getLayoutInflater().inflate(R.layout.item_mainlv_top, null);
        todayLV.addHeaderView(headView);

        topOutTv = headView.findViewById(R.id.item_mainlv_iv_top_out);
        topInTv = headView.findViewById(R.id.item_mainlv_iv_top_in);
        budgetIv = headView.findViewById(R.id.item_mainlv_iv_top_budget);
        topemoneyTv = headView.findViewById(R.id.item_mainlv_iv_top_emoney);
        topAllTv = headView.findViewById(R.id.item_mainlv_iv_top_all);

        budgetIv.setOnClickListener(this);
        headView.setOnClickListener(this);
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDBData();
        setTopTvShow();
    }

    private void loadDBData() {
        List<AccountBean> list = DBManager.getDayAccountFromAccounttb(year, month, day);
        mData.clear();
        mData.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void setTopTvShow() {
        float incomeday = DBManager.getSumDayAccount(year, month, day, 1);
        float outcomeday = DBManager.getSumDayAccount(year, month, day, 0);
        String dayInfo = "今日支出 " + outcomeday + " 今日收入 " + incomeday;
        topAllTv.setText(dayInfo);

        float incomeMonth = DBManager.getSumMonthAccount(year, month, 1);
        float outcomeMonth = DBManager.getSumMonthAccount(year, month, 0);
        topInTv.setText("￥" + incomeMonth);
        topOutTv.setText("￥" + outcomeMonth);

        float bmoney = sharedPreferences.getFloat("bmoney", 0);
        float emoney = bmoney - outcomeMonth;
        topemoneyTv.setText("￥" + emoney);
    }

    private void setLVLongClickListener() {
        todayLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - 1; // 因为有头布局
                if (pos >= 0 && pos < mData.size()) {
                    AccountBean clickBean = mData.get(pos);
                    showDeleteDialog(clickBean);
                }
                return true;
            }
        });
    }

    private void showDeleteDialog(AccountBean clickBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("删除后无法恢复，是否删除？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int click_id = clickBean.getId();
                        DBManager.deleteFromAccounttbByid(click_id);
                        mData.remove(clickBean);
                        Toast.makeText(MainActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        setTopTvShow();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        // 【修改】将所有点击事件统一在 switch 中处理，代码更清晰
        int viewId = view.getId();
        if (viewId == R.id.mian_iv_box) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (viewId == R.id.main_btn_edit) {
            startActivity(new Intent(this, RecordActivity.class));
        } else if (viewId == R.id.main_bt_more) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (viewId == R.id.item_mainlv_iv_top_budget) {
            showBudgeDialog();
        } else if (viewId == R.id.main_fab_ai) { // 【新增】处理AI悬浮按钮的点击事件
            startActivity(new Intent(this, AIActivity.class));
        }

        // 如果点击的是头布局，跳转到月统计图表
        if (view == headView) {
            startActivity(new Intent(this, MonthChartActivity.class));
        }
    }

    private void showBudgeDialog() {
        BudgeDialog dialog = new BudgeDialog(this);
        dialog.show();
        dialog.setDialogSize();
        dialog.setOnEnsureListener(new BudgeDialog.OnEnsureListener() {
            @Override
            public void onEnsure(float money) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("bmoney", money);
                editor.apply();

                float outcomeMonth = DBManager.getSumMonthAccount(year, month, 0);
                float emoney = money - outcomeMonth;
                topemoneyTv.setText("￥" + emoney);
            }
        });
    }
}