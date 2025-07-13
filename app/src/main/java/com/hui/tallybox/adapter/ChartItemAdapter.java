package com.hui.tallybox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hui.tallybox.R;
import com.hui.tallybox.utils.FloatUtils;
import com.hui.tallybox.db.ChartItemBean;

import java.util.List;

/**
 * ChartItemAdapter - 用于图表详情页面ListView的适配器
 *
 * 作用：
 * 这个适配器专门用于展示月度或年度收支统计的详细列表。
 * 列表中的每一项都代表一个消费或收入类别（如“餐饮”、“工资”等），并显示该类别的总金额及其在总收支中所占的百分比。
 * 它负责将后台计算好的 `ChartItemBean` 数据对象，优雅地渲染成用户可见的列表项。
 * 同样，为了保证列表滚动的流畅性，此适配器也高效地运用了ViewHolder模式。
 */
public class ChartItemAdapter extends BaseAdapter {
    // === 成员变量声明 ===

    Context context;                    // 上下文对象，用于获取系统服务，如LayoutInflater
    List<ChartItemBean> mDatas;         // 数据源，一个包含所有图表项信息的列表
    LayoutInflater inflater;            // 布局加载器，用于将XML布局转换为View对象

    /**
     * 构造函数
     * 在创建ChartItemAdapter实例时调用，用于初始化必要的上下文和数据。
     * @param context 上下文环境，通常是调用此适配器的Activity或Fragment
     * @param mDatas  包含所有图表项（ChartItemBean）的数据列表
     */
    public ChartItemAdapter(Context context, List<ChartItemBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        // 在构造函数中预先初始化LayoutInflater，避免在getView中重复获取，是标准的优化实践。
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * 返回数据源中的项目总数。
     */
    @Override
    public int getCount() {
        return mDatas.size();
    }

    /**
     * 根据位置索引返回对应的数据对象。
     */
    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    /**
     * 返回项目的稳定ID，此处简单地使用其位置索引。
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Adapter的核心方法，负责创建和填充每一个列表项的视图。
     *
     * @param position    当前正在处理的列表项的位置
     * @param convertView 可复用的旧视图对象，用于性能优化
     * @param parent      该视图将被添加到的父容器，即ListView
     * @return 一个完整配置了数据的View，用于显示在屏幕上
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. 高效地获取ViewHolder实例
        ViewHolder holder = null;
        if (convertView == null) {
            // 当没有可复用的视图时，创建一个新的
            // a. 加载XML布局文件
            convertView = inflater.inflate(R.layout.item_chartfrag_lv, parent, false);
            // b. 创建ViewHolder并查找子控件
            holder = new ViewHolder(convertView);
            // c. 将ViewHolder作为“标签”附加到视图上，以便后续复用
            convertView.setTag(holder);
        } else {
            // 如果有可复用的视图，直接从“标签”中取出ViewHolder，避免了findViewById的开销
            holder = (ViewHolder) convertView.getTag();
        }

        // 2. 从数据源中获取当前位置的数据模型
        ChartItemBean bean = mDatas.get(position);

        // 3. 将数据模型中的信息绑定到ViewHolder持有的UI控件上
        holder.iv.setImageResource(bean.getsImageId()); // 设置分类图标
        holder.typeTv.setText(bean.getType());           // 设置分类名称

        // 4. 【核心业务逻辑】处理并显示百分比
        // a. 从bean中获取原始的浮点数比例（例如 0.253）
        float ratio = bean.getRatio();
        // b. 调用工具类方法，将浮点数转换为格式化的百分比字符串（例如 "25.3%"）
        String pert = FloatUtils.ratioToPercent(ratio);
        // c. 将格式化后的字符串设置到TextView上
        holder.ratioTv.setText(pert);

        // 5. 显示总金额
        holder.totalTv.setText("￥ " + bean.getTotalMoney()); // 设置总金额，并添加货币符号和空格，增强可读性

        // 6. 返回最终配置完成的视图
        return convertView;
    }

    /**
     * ViewHolder (视图持有者) 静态内部类。
     *
     * 作用：
     * 作为一个轻量级的数据结构，它缓存了item_chartfrag_lv.xml布局中所有需要动态更新的视图引用。
     * 通过这种方式，我们只需在视图第一次创建时调用findViewById，后续的复用将直接通过holder访问，
     * 这是优化ListView性能、保证流畅滚动的关键所在。
     */
    class ViewHolder {
        // 声明与XML布局中控件ID对应的成员变量
        TextView typeTv, ratioTv, totalTv;
        ImageView iv;

        /**
         * ViewHolder的构造函数，在视图第一次被创建时调用。
         * @param view 一个刚刚被实例化的、代表单行列表项的根视图
         */
        public ViewHolder(View view) {
            // 一次性完成所有findViewById的查找操作
            typeTv = view.findViewById(R.id.item_chartfrag_tv_type);
            ratioTv = view.findViewById(R.id.item_chartfrag_tv_pert);
            totalTv = view.findViewById(R.id.item_chartfrag_tv_sum);
            iv = view.findViewById(R.id.item_chartfrag_iv);
        }
    }
}