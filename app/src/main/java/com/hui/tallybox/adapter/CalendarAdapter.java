package com.hui.tallybox.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hui.tallybox.R;

import java.util.ArrayList;
import java.util.List;

/**
 * CalendarAdapter - 用于历史账单页面中，日历选择对话框内月份GridView的适配器。
 *
 * 作用：
 * 这个适配器的功能非常专一：为用户提供一个12个月份的网格视图，用于快速选择要查询的月份。
 * 它负责生成并展示从 "YYYY/1" 到 "YYYY/12" 的文本。
 * 同时，它内部维护了一个选中位置 (selPos)，并根据这个位置来高亮显示当前被选中的月份，提供了清晰的视觉反馈。
 */
public class CalendarAdapter extends BaseAdapter {
    // === 成员变量声明 ===

    Context context;            // 上下文对象，用于访问系统资源
    List<String> mDatas;        // 数据源，存储了"YYYY/MM"格式的月份字符串
    public int year;            // 当前适配器所展示的年份
    public int selPos = -1;     // 记录当前被选中项目的位置(position)。-1表示默认没有任何项被选中。

    /**
     * 构造函数
     * @param context 上下文
     * @param year    需要显示的年份，适配器将根据此年份生成12个月的数据
     */
    public CalendarAdapter(Context context, int year) {
        this.context = context;
        this.year = year;
        // 初始化数据列表
        mDatas = new ArrayList<>();
        // 调用内部方法，加载当前年份的12个月份数据
        loadDatas(year);
    }

    /**
     * 公开方法，用于在外部动态地改变当前适配器展示的年份。
     * 当用户在UI上切换年份时，会调用此方法来更新整个GridView的内容。
     * @param year 新的年份
     */
    public void setYear(int year) {
        this.year = year;
        // 1. 清空旧的年份数据
        mDatas.clear();
        // 2. 加载新年份的12个月数据
        loadDatas(year);
        // 3. 通知适配器数据已发生变化，这会触发GridView重新渲染所有项目。
        //    这是刷新UI的关键步骤。
        notifyDataSetChanged();
    }

    /**
     * 内部私有方法，负责生成指定年份的月份数据。
     * @param year 要生成数据的年份
     */
    private void loadDatas(int year) {
        // 循环12次，生成 "year/1", "year/2", ..., "year/12" 的字符串
        for (int i = 1; i < 13; i++) {
            String data = year + "/" + i;
            mDatas.add(data);
        }
    }

    /**
     * 返回数据项的总数，即12个月。
     */
    @Override
    public int getCount() {
        return mDatas.size();
    }

    /**
     * 根据位置获取对应的数据项（月份字符串）。
     */
    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    /**
     * 返回项目的稳定ID，这里使用位置索引。
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 核心方法，为GridView的每一个网格项创建并配置视图。
     * 这个方法没有使用ViewHolder模式，因为网格项非常少（固定12个）且布局简单，
     * 性能影响微乎其微，直接加载在可读性上更佳。
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. 加载或复用视图
        // 每次都从XML加载新的视图，对于固定12项的简单GridView来说，性能影响可以忽略。
        convertView = LayoutInflater.from(context).inflate(R.layout.item_dialogcal_gv, parent, false);
        // 2. 获取视图中的TextView控件
        TextView tv = convertView.findViewById(R.id.item_dialogcal_gv_tv);
        // 3. 设置显示的文本内容，例如 "2025/7"
        tv.setText(mDatas.get(position));

        // 4. 设置默认的显示样式（未选中状态）
        tv.setBackgroundResource(R.color.grey_f3f3f3); // 灰色背景
        tv.setTextColor(Color.BLACK);                  // 黑色文字

        // 5. 【核心逻辑】根据选中状态，设置高亮样式
        //    判断当前正在渲染的项的位置(position)是否与记录的选中位置(selPos)相等。
        if (position == selPos) {
            // 如果是，说明这就是被选中的那一项，应用特殊的高亮样式。
            tv.setBackgroundResource(R.color.blue_1A5599); // 蓝色背景
            tv.setTextColor(Color.WHITE);                  // 白色文字
        }

        // 6. 返回配置好的视图
        return convertView;
    }
}