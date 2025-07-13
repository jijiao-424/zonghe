package com.hui.tallybox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.hui.tallybox.db.AccountBean;
import com.hui.tallybox.db.DBManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AIActivity extends AppCompatActivity {
    private TextInputEditText etQuestion;
    private Button btnAsk;
    private TextView tvAnswer;
    private ProgressBar pbLoading;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        etQuestion = findViewById(R.id.et_question);
        btnAsk = findViewById(R.id.btn_ask);
        tvAnswer = findViewById(R.id.tv_answer);
        pbLoading = findViewById(R.id.pb_loading);

        btnAsk.setOnClickListener(v -> handleAskButtonClick());
    }

    private void handleAskButtonClick() {
        String question = etQuestion.getText().toString().trim();
        if (question.isEmpty()) {
            Toast.makeText(this, "请输入您的问题", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        List<AccountBean> list = DBManager.getDayAccountFromAccounttb(year, month, day);
        if (list.isEmpty()) {
            tvAnswer.setText("分析失败：今天没有任何账单记录。");
            return;
        }

        StringBuilder context = new StringBuilder();
        context.append("你是一个专业的个人财务助理。这是我今天的支出记录，请基于这些数据简洁地回答我的问题。\n");
        context.append("--- 支出数据开始 ---\n");
        for (AccountBean item : list) {
            String remark = item.getRemark();
            String remarkText = (remark == null || remark.isEmpty()) ? "无" : remark;
            context.append("类型：").append(item.getTypename())
                    .append("，金额：").append(item.getMoney())
                    .append("元，备注：").append(remarkText)
                    .append("。\n");
        }
        context.append("--- 支出数据结束 ---\n");
        context.append("我的问题是：").append(question);

        updateUIForLoading(true, "AI 正在思考中...");
        askAliQianWen(context.toString());
    }

    private void askAliQianWen(String prompt) {
        if (BuildConfig.ALIYUN_API_KEY == null || BuildConfig.ALIYUN_API_KEY.isEmpty() || "null".equals(BuildConfig.ALIYUN_API_KEY)) {
            runOnUiThread(() -> {
                updateUIForLoading(false, "");
                tvAnswer.setText("错误：API Key 未配置。请在 local.properties 文件中设置 ALIYUN_API_KEY。");
            });
            return;
        }

        JSONObject body = new JSONObject();
        try {
            JSONObject input = new JSONObject();
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.put(userMsg);
            input.put("messages", messages);
            body.put("model", "qwen-turbo");
            body.put("input", input);
        } catch (Exception e) {
            runOnUiThread(() -> {
                updateUIForLoading(false, "");
                tvAnswer.setText("构建请求失败：" + e.getMessage());
            });
            return;
        }

        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + BuildConfig.ALIYUN_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    updateUIForLoading(false, "");
                    tvAnswer.setText("请求失败，请检查您的网络连接。\n错误信息：" + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String[] resStr = {""};
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        final String errorBody = responseBody != null ? responseBody.string() : "无详细信息";
                        runOnUiThread(() -> {
                            updateUIForLoading(false, "");
                            tvAnswer.setText("请求失败，响应码：" + response.code() + "\n错误信息：" + errorBody);
                        });
                        return;
                    }

                    if (responseBody == null) {
                        runOnUiThread(() -> {
                            updateUIForLoading(false, "");
                            tvAnswer.setText("解析响应失败：响应体为空。");
                        });
                        return;
                    }

                    resStr[0] = responseBody.string();
                    JSONObject resJson = new JSONObject(resStr[0]);

                    if (resJson.has("code")) {
                        String errorCode = resJson.getString("code");
                        String errorMessage = resJson.getString("message");
                        runOnUiThread(() -> {
                            updateUIForLoading(false, "");
                            tvAnswer.setText("API返回错误：\n代码: " + errorCode + "\n信息: " + errorMessage);
                        });
                        return;
                    }

                    // 【最终修复】我们直接从 output -> text 中获取AI的回答！
                    String answer = resJson
                            .getJSONObject("output")
                            .getString("text");

                    runOnUiThread(() -> {
                        updateUIForLoading(false, answer);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Log.e("AI_RESPONSE_ERROR", "服务器返回的原始数据: " + resStr[0]);
                        updateUIForLoading(false, "");
                        tvAnswer.setText("解析响应数据失败，请稍后重试。\n错误信息：" + e.getMessage());
                    });
                }
            }
        });
    }

    private void updateUIForLoading(boolean isLoading, String answerText) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
            tvAnswer.setVisibility(View.GONE);
            btnAsk.setEnabled(false);
        } else {
            pbLoading.setVisibility(View.GONE);
            tvAnswer.setVisibility(View.VISIBLE);
            btnAsk.setEnabled(true);
        }
        tvAnswer.setText(answerText);
    }
}